package com.nexarats.academy.finance.repository;

import com.nexarats.academy.finance.model.TuitionPrice;
import com.nexarats.academy.finance.model.TuitionTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TuitionPriceRepository extends JpaRepository<TuitionPrice, UUID> {

    @Query("SELECT tp FROM TuitionPrice tp WHERE tp.programId = :programId AND tp.currency = :currency AND tp.tier = :tier AND tp.isActive = true")
    Optional<TuitionPrice> findByProgramIdAndCurrencyAndTier(
            @Param("programId") UUID programId,
            @Param("currency") String currency,
            @Param("tier") TuitionTier tier);

    @Query("SELECT tp FROM TuitionPrice tp WHERE tp.courseId = :courseId AND tp.currency = :currency AND tp.tier = :tier AND tp.isActive = true")
    Optional<TuitionPrice> findByCourseIdAndCurrencyAndTier(
            @Param("courseId") UUID courseId,
            @Param("currency") String currency,
            @Param("tier") TuitionTier tier);

    List<TuitionPrice> findByProgramId(UUID programId);

    List<TuitionPrice> findByCourseId(UUID courseId);
}
