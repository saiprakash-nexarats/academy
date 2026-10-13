package com.nexarats.academy.learning.repository;

import com.nexarats.academy.learning.model.LessonMediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LessonMediaAssetRepository extends JpaRepository<LessonMediaAsset, UUID> {

    Optional<LessonMediaAsset> findByLessonIdAndAssetType(UUID lessonId, String assetType);

    Optional<LessonMediaAsset> findFirstByLessonId(UUID lessonId);
}
