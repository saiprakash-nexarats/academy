package com.nexarats.academy.academic.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexarats.academy.academic.dto.PaginatedResponseDto;
import com.nexarats.academy.academic.dto.ProgramCategoryDto;
import com.nexarats.academy.academic.dto.ProgramDetailDto;
import com.nexarats.academy.academic.dto.ProgramSummaryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * High-performance Redis caching service for the public academic program catalog.
 * Implements resilient caching with graceful fallback when Redis is unreachable,
 * strict isolation from authentication/session caches, and bounded key cardinality.
 */
@Service
public class ProgramCatalogCacheService {

    private static final Logger log = LoggerFactory.getLogger(ProgramCatalogCacheService.class);

    private static final String KEY_PREFIX = "catalog:";
    private static final String CATEGORIES_KEY = KEY_PREFIX + "categories:all";
    private static final String PROGRAM_PREFIX = KEY_PREFIX + "program:";
    private static final String LIST_PREFIX = KEY_PREFIX + "list:";

    private static final Duration DEFAULT_TTL = Duration.ofHours(1);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public ProgramCatalogCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper.copy()
                .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public Optional<List<ProgramCategoryDto>> getCachedCategories() {
        try {
            String json = redisTemplate.opsForValue().get(CATEGORIES_KEY);
            if (json != null) {
                List<ProgramCategoryDto> categories = objectMapper.readValue(
                        json,
                        new TypeReference<List<ProgramCategoryDto>>() {}
                );
                log.debug("Redis Cache HIT: {}", CATEGORIES_KEY);
                return Optional.of(categories);
            }
        } catch (Exception ex) {
            log.warn("Redis read failed for categories, falling back to database: {}", ex.getMessage());
        }
        return Optional.empty();
    }

    public void cacheCategories(List<ProgramCategoryDto> categories) {
        try {
            String json = objectMapper.writeValueAsString(categories);
            redisTemplate.opsForValue().set(CATEGORIES_KEY, json, DEFAULT_TTL);
            log.debug("Redis Cache SET: {}", CATEGORIES_KEY);
        } catch (Exception ex) {
            log.warn("Redis write failed for categories: {}", ex.getMessage());
        }
    }

    public Optional<PaginatedResponseDto<ProgramSummaryDto>> getCachedPrograms(String filterCacheKey) {
        try {
            String fullKey = LIST_PREFIX + filterCacheKey;
            String json = redisTemplate.opsForValue().get(fullKey);
            if (json != null) {
                PaginatedResponseDto<ProgramSummaryDto> result = objectMapper.readValue(
                        json,
                        new TypeReference<PaginatedResponseDto<ProgramSummaryDto>>() {}
                );
                log.debug("Redis Cache HIT: {}", fullKey);
                return Optional.of(result);
            }
        } catch (Exception ex) {
            log.warn("Redis read failed for program list, falling back to database: {}", ex.getMessage());
        }
        return Optional.empty();
    }

    public void cachePrograms(String filterCacheKey, PaginatedResponseDto<ProgramSummaryDto> response) {
        // Enforce bounded cache cardinality: skip caching excessively long search keys
        if (filterCacheKey.length() > 200) {
            return;
        }
        try {
            String fullKey = LIST_PREFIX + filterCacheKey;
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(fullKey, json, DEFAULT_TTL);
            log.debug("Redis Cache SET: {}", fullKey);
        } catch (Exception ex) {
            log.warn("Redis write failed for program list: {}", ex.getMessage());
        }
    }

    public Optional<ProgramDetailDto> getCachedProgramDetail(String slug) {
        try {
            String fullKey = PROGRAM_PREFIX + slug.toLowerCase().trim();
            String json = redisTemplate.opsForValue().get(fullKey);
            if (json != null) {
                ProgramDetailDto result = objectMapper.readValue(json, ProgramDetailDto.class);
                log.debug("Redis Cache HIT: {}", fullKey);
                return Optional.of(result);
            }
        } catch (Exception ex) {
            log.warn("Redis read failed for program detail [slug: {}]: {}", slug, ex.getMessage());
        }
        return Optional.empty();
    }

    public void cacheProgramDetail(String slug, ProgramDetailDto detail) {
        try {
            String fullKey = PROGRAM_PREFIX + slug.toLowerCase().trim();
            String json = objectMapper.writeValueAsString(detail);
            redisTemplate.opsForValue().set(fullKey, json, DEFAULT_TTL);
            log.debug("Redis Cache SET: {}", fullKey);
        } catch (Exception ex) {
            log.warn("Redis write failed for program detail [slug: {}]: {}", slug, ex.getMessage());
        }
    }

    public void evictProgramDetail(String slug) {
        try {
            String fullKey = PROGRAM_PREFIX + slug.toLowerCase().trim();
            redisTemplate.delete(fullKey);
            log.debug("Redis Cache EVICT: {}", fullKey);
        } catch (Exception ex) {
            log.warn("Redis eviction failed for slug {}: {}", slug, ex.getMessage());
        }
    }

    public void evictAllCatalogCache() {
        try {
            Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Redis Catalog Cache fully invalidated ({} keys deleted)", keys.size());
            }
        } catch (Exception ex) {
            log.warn("Redis catalog cache eviction failed: {}", ex.getMessage());
        }
    }
}
