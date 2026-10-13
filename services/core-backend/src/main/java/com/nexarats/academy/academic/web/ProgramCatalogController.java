package com.nexarats.academy.academic.web;

import com.nexarats.academy.academic.dto.PaginatedResponseDto;
import com.nexarats.academy.academic.dto.ProgramDetailDto;
import com.nexarats.academy.academic.dto.ProgramSummaryDto;
import com.nexarats.academy.academic.service.ProgramCatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public REST Controller for the Nexarats Academy Program Catalog.
 * Supports canonical /api/v1/academic/programs and compatibility alias /api/v1/programs.
 * Publicly accessible, read-only, cached via Redis.
 */
@RestController
@RequestMapping({"/api/v1/academic/programs", "/api/v1/programs"})
public class ProgramCatalogController {

    private final ProgramCatalogService catalogService;

    public ProgramCatalogController(ProgramCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public ResponseEntity<PaginatedResponseDto<ProgramSummaryDto>> listPrograms(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer duration,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "default") String sort
    ) {
        String searchQuery = (search != null && !search.isBlank()) ? search : q;
        PaginatedResponseDto<ProgramSummaryDto> result = catalogService.getPublishedPrograms(
                category,
                duration,
                featured,
                searchQuery,
                page,
                size,
                sort
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ProgramDetailDto> getProgramBySlug(@PathVariable String slug) {
        ProgramDetailDto detail = catalogService.getProgramBySlug(slug);
        return ResponseEntity.ok(detail);
    }
}
