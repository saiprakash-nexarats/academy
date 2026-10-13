package com.nexarats.academy.academic.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexarats.academy.academic.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * High-performance Redis caching service for the public course catalog,
 * hierarchical curriculum, and syllabus preview engine.
 * Dedicated namespace: catalog:course:*
 */
@Service
public class CourseCatalogCacheService {

    private static final Logger log = LoggerFactory.getLogger(CourseCatalogCacheService.class);

    private static final String KEY_PREFIX = "catalog:course:";
    private static final String LIST_PREFIX = KEY_PREFIX + "list:";
    private static final String DETAIL_PREFIX = KEY_PREFIX + "detail:";
    private static final String SYLLABUS_PREFIX = KEY_PREFIX + "syllabus:";
    private static final String PREVIEW_PREFIX = KEY_PREFIX + "preview:";

    private static final Duration DEFAULT_TTL = Duration.ofHours(1);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public CourseCatalogCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper.copy()
                .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public Optional<PaginatedResponseDto<CourseSummaryDto>> getCachedCourses(String filterCacheKey) {
        try {
            String fullKey = LIST_PREFIX + filterCacheKey;
            String json = redisTemplate.opsForValue().get(fullKey);
            if (json != null) {
                PaginatedResponseDto<CourseSummaryDto> result = objectMapper.readValue(
                        json,
                        new TypeReference<PaginatedResponseDto<CourseSummaryDto>>() {}
                );
                log.debug("Redis Course Cache HIT: {}", fullKey);
                return Optional.of(result);
            }
        } catch (Exception ex) {
            log.warn("Redis read failed for course list, falling back to database: {}", ex.getMessage());
        }
        return Optional.empty();
    }

    public void cacheCourses(String filterCacheKey, PaginatedResponseDto<CourseSummaryDto> response) {
        if (filterCacheKey.length() > 200) {
            return;
        }
        try {
            String fullKey = LIST_PREFIX + filterCacheKey;
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(fullKey, json, DEFAULT_TTL);
            log.debug("Redis Course Cache SET: {}", fullKey);
        } catch (Exception ex) {
            log.warn("Redis write failed for course list: {}", ex.getMessage());
        }
    }

    public Optional<CourseDetailDto> getCachedCourseDetail(String slug) {
        try {
            String fullKey = DETAIL_PREFIX + slug.toLowerCase().trim();
            String json = redisTemplate.opsForValue().get(fullKey);
            if (json != null) {
                CourseDetailDto result = objectMapper.readValue(json, CourseDetailDto.class);
                log.debug("Redis Course Detail Cache HIT: {}", fullKey);
                return Optional.of(result);
            }
        } catch (Exception ex) {
            log.warn("Redis read failed for course detail [slug: {}]: {}", slug, ex.getMessage());
        }
        return Optional.empty();
    }

    public void cacheCourseDetail(String slug, CourseDetailDto detail) {
        try {
            String fullKey = DETAIL_PREFIX + slug.toLowerCase().trim();
            String json = objectMapper.writeValueAsString(detail);
            redisTemplate.opsForValue().set(fullKey, json, DEFAULT_TTL);
            log.debug("Redis Course Detail Cache SET: {}", fullKey);
        } catch (Exception ex) {
            log.warn("Redis write failed for course detail [slug: {}]: {}", slug, ex.getMessage());
        }
    }

    public Optional<CurriculumSyllabusDto> getCachedSyllabus(String slug) {
        try {
            String fullKey = SYLLABUS_PREFIX + slug.toLowerCase().trim();
            String json = redisTemplate.opsForValue().get(fullKey);
            if (json != null) {
                CurriculumSyllabusDto result = objectMapper.readValue(json, CurriculumSyllabusDto.class);
                log.debug("Redis Syllabus Cache HIT: {}", fullKey);
                return Optional.of(result);
            }
        } catch (Exception ex) {
            log.warn("Redis read failed for syllabus [slug: {}]: {}", slug, ex.getMessage());
        }
        return Optional.empty();
    }

    public void cacheSyllabus(String slug, CurriculumSyllabusDto syllabus) {
        try {
            String fullKey = SYLLABUS_PREFIX + slug.toLowerCase().trim();
            String json = objectMapper.writeValueAsString(syllabus);
            redisTemplate.opsForValue().set(fullKey, json, DEFAULT_TTL);
            log.debug("Redis Syllabus Cache SET: {}", fullKey);
        } catch (Exception ex) {
            log.warn("Redis write failed for syllabus [slug: {}]: {}", slug, ex.getMessage());
        }
    }

    public Optional<LessonPreviewDto> getCachedLessonPreview(String slug, UUID lessonId) {
        try {
            String fullKey = PREVIEW_PREFIX + slug.toLowerCase().trim() + ":" + lessonId;
            String json = redisTemplate.opsForValue().get(fullKey);
            if (json != null) {
                LessonPreviewDto result = objectMapper.readValue(json, LessonPreviewDto.class);
                log.debug("Redis Lesson Preview Cache HIT: {}", fullKey);
                return Optional.of(result);
            }
        } catch (Exception ex) {
            log.warn("Redis read failed for lesson preview: {}", ex.getMessage());
        }
        return Optional.empty();
    }

    public void cacheLessonPreview(String slug, UUID lessonId, LessonPreviewDto preview) {
        try {
            String fullKey = PREVIEW_PREFIX + slug.toLowerCase().trim() + ":" + lessonId;
            String json = objectMapper.writeValueAsString(preview);
            redisTemplate.opsForValue().set(fullKey, json, DEFAULT_TTL);
            log.debug("Redis Lesson Preview Cache SET: {}", fullKey);
        } catch (Exception ex) {
            log.warn("Redis write failed for lesson preview: {}", ex.getMessage());
        }
    }

    public void evictCourseBySlug(String slug) {
        try {
            String normalizedSlug = slug.toLowerCase().trim();
            redisTemplate.delete(DETAIL_PREFIX + normalizedSlug);
            redisTemplate.delete(SYLLABUS_PREFIX + normalizedSlug);
            Set<String> previewKeys = redisTemplate.keys(PREVIEW_PREFIX + normalizedSlug + ":*");
            if (previewKeys != null && !previewKeys.isEmpty()) {
                redisTemplate.delete(previewKeys);
            }
            Set<String> listKeys = redisTemplate.keys(LIST_PREFIX + "*");
            if (listKeys != null && !listKeys.isEmpty()) {
                redisTemplate.delete(listKeys);
            }
            log.info("Redis Course Cache evicted for slug: {}", normalizedSlug);
        } catch (Exception ex) {
            log.warn("Redis course cache eviction failed for slug {}: {}", slug, ex.getMessage());
        }
    }

    public void evictAllCourseCache() {
        try {
            Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Redis Course Catalog Cache fully invalidated ({} keys deleted)", keys.size());
            }
        } catch (Exception ex) {
            log.warn("Redis course catalog cache eviction failed: {}", ex.getMessage());
        }
    }
}
