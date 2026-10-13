package com.nexarats.academy.academic.service;

import com.nexarats.academy.academic.dto.PaginatedResponseDto;
import com.nexarats.academy.academic.dto.ProgramCategoryDto;
import com.nexarats.academy.academic.dto.ProgramDetailDto;
import com.nexarats.academy.academic.dto.ProgramSummaryDto;
import com.nexarats.academy.academic.model.Program;
import com.nexarats.academy.academic.model.ProgramCategory;
import com.nexarats.academy.academic.repository.ProgramCategoryRepository;
import com.nexarats.academy.academic.repository.ProgramRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProgramCatalogService {

    private static final Logger log = LoggerFactory.getLogger(ProgramCatalogService.class);

    private final ProgramRepository programRepository;
    private final ProgramCategoryRepository categoryRepository;
    private final ProgramCatalogCacheService cacheService;

    public ProgramCatalogService(
            ProgramRepository programRepository,
            ProgramCategoryRepository categoryRepository,
            ProgramCatalogCacheService cacheService
    ) {
        this.programRepository = programRepository;
        this.categoryRepository = categoryRepository;
        this.cacheService = cacheService;
    }

    public List<ProgramCategoryDto> getCategories() {
        Optional<List<ProgramCategoryDto>> cached = cacheService.getCachedCategories();
        if (cached.isPresent()) {
            return cached.get();
        }

        List<ProgramCategoryDto> categories = categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::mapToCategoryDto)
                .toList();

        cacheService.cacheCategories(categories);
        return categories;
    }

    public PaginatedResponseDto<ProgramSummaryDto> getPublishedPrograms(
            String category,
            Integer duration,
            Boolean featured,
            String search,
            int page,
            int size,
            String sortBy
    ) {
        // Validation & bounds enforcement
        if (page < 0) {
            page = 0;
        }
        if (size <= 0 || size > 100) {
            size = 20;
        }

        String sanitizedCategory = (category != null && !category.isBlank()) ? category.trim() : null;
        String sanitizedSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        if (sanitizedSearch != null && sanitizedSearch.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search query exceeds maximum length of 100 characters");
        }

        UUID tenantId = com.nexarats.academy.common.tenant.TenantContext.getTenantId();
        String tenantKey = tenantId != null ? tenantId.toString() : "global";

        // Deterministic Sort Configuration
        Sort sort = Sort.by(Sort.Direction.ASC, "displayOrder");
        if ("rating".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "rating").and(Sort.by(Sort.Direction.ASC, "displayOrder"));
        } else if ("title".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.ASC, "title");
        }

        String cacheKey = String.format("t=%s:cat=%s:dur=%s:feat=%s:q=%s:p=%d:s=%d:sort=%s",
                tenantKey,
                sanitizedCategory != null ? sanitizedCategory : "all",
                duration != null ? duration : "any",
                featured != null ? featured : "all",
                sanitizedSearch != null ? sanitizedSearch.toLowerCase() : "",
                page,
                size,
                sortBy != null ? sortBy.toLowerCase() : "default"
        );

        Optional<PaginatedResponseDto<ProgramSummaryDto>> cached = cacheService.getCachedPrograms(cacheKey);
        if (cached.isPresent()) {
            return cached.get();
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Program> programPage = programRepository.findPublishedWithFiltersPaged(
                tenantId,
                sanitizedCategory,
                duration,
                featured,
                sanitizedSearch,
                pageable
        );

        List<ProgramSummaryDto> items = programPage.getContent().stream()
                .map(this::mapToSummaryDto)
                .toList();

        PaginatedResponseDto<ProgramSummaryDto> response = PaginatedResponseDto.of(
                items,
                programPage.getTotalElements(),
                page,
                size
        );

        cacheService.cachePrograms(cacheKey, response);
        return response;
    }

    public ProgramDetailDto getProgramBySlug(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Program identifier cannot be empty");
        }

        UUID tenantId = com.nexarats.academy.common.tenant.TenantContext.getTenantId();
        String tenantKey = tenantId != null ? tenantId.toString() : "global";

        String sanitized = identifier.trim().toLowerCase();
        String detailCacheKey = (tenantId != null) ? ("t=" + tenantKey + ":" + sanitized) : sanitized;
        Optional<ProgramDetailDto> cached = cacheService.getCachedProgramDetail(detailCacheKey);
        if (cached.isPresent()) {
            return cached.get();
        }

        // Primary lookup: by stable slug
        Optional<Program> programOpt = programRepository.findBySlugAndStatus(sanitized, "PUBLISHED");

        // Secondary lookup: by UUID if slug didn't match directly
        if (programOpt.isEmpty()) {
            try {
                UUID uuid = UUID.fromString(identifier.trim());
                programOpt = programRepository.findById(uuid)
                        .filter(p -> "PUBLISHED".equals(p.getStatus()));
            } catch (IllegalArgumentException ignored) {
                // Not a valid UUID, so slug match was the only candidate
            }
        }

        Program program = programOpt.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Published program not found with identifier: " + identifier)
        );

        // Enforce tenant isolation: private tenant program cannot be viewed without matching tenant context
        if (program.getTenantId() != null && (tenantId == null || !tenantId.equals(program.getTenantId()))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Published program not found with identifier: " + identifier);
        }

        ProgramDetailDto detailDto = mapToDetailDto(program);
        cacheService.cacheProgramDetail(detailCacheKey, detailDto);
        return detailDto;
    }

    private ProgramCategoryDto mapToCategoryDto(ProgramCategory category) {
        return new ProgramCategoryDto(
                category.getId(),
                category.getSlug(),
                category.getName(),
                category.getDescription(),
                category.getDisplayOrder()
        );
    }

    private ProgramSummaryDto mapToSummaryDto(Program program) {
        return new ProgramSummaryDto(
                program.getId(),
                program.getSlug(),
                program.getTitle(),
                program.getShortDescription(),
                program.getImageAssetKey(),
                program.getDurationValue(),
                program.getDurationUnit(),
                program.getDurationLabel(),
                program.getLevel(),
                program.getBadge(),
                program.getRating(),
                program.getCategory() != null ? program.getCategory().getSlug() : null,
                program.getCategory() != null ? program.getCategory().getName() : null,
                program.isFeatured(),
                program.getDisplayOrder()
        );
    }

    private ProgramDetailDto mapToDetailDto(Program program) {
        return new ProgramDetailDto(
                program.getId(),
                program.getSlug(),
                program.getTitle(),
                program.getShortDescription(),
                program.getDetailedDescription(),
                program.getImageAssetKey(),
                program.getDurationValue(),
                program.getDurationUnit(),
                program.getDurationLabel(),
                program.getLevel(),
                program.getBadge(),
                program.getRating(),
                program.getCategory() != null ? program.getCategory().getSlug() : null,
                program.getCategory() != null ? program.getCategory().getName() : null,
                program.getCurriculumOverview(),
                program.getCareerOutcomes(),
                program.isFeatured(),
                program.getDisplayOrder(),
                program.getPublishedAt()
        );
    }
}
