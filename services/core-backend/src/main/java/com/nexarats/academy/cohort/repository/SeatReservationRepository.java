package com.nexarats.academy.cohort.repository;

import com.nexarats.academy.cohort.model.SeatReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeatReservationRepository extends JpaRepository<SeatReservation, UUID> {

    Optional<SeatReservation> findByReservationToken(String reservationToken);

    Optional<SeatReservation> findByOfferId(UUID offerId);

    @Query("SELECT COUNT(r) FROM SeatReservation r WHERE r.cohortId = :cohortId AND r.status = 'RESERVED' AND r.expiresAt > :now")
    long countActiveReservationsByCohortId(@Param("cohortId") UUID cohortId, @Param("now") Instant now);

    @Query("SELECT r FROM SeatReservation r WHERE r.status = 'RESERVED' AND r.expiresAt <= :now")
    List<SeatReservation> findExpiredReservations(@Param("now") Instant now);
}
