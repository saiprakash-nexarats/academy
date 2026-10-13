package com.nexarats.academy.cohort.service;

import com.nexarats.academy.cohort.model.Cohort;
import com.nexarats.academy.cohort.model.CohortStatus;
import com.nexarats.academy.cohort.model.SeatReservation;
import com.nexarats.academy.cohort.model.SeatReservationStatus;
import com.nexarats.academy.cohort.repository.CohortRepository;
import com.nexarats.academy.cohort.repository.SeatReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class CohortSeatService {

    private static final Logger log = LoggerFactory.getLogger(CohortSeatService.class);

    private final CohortRepository cohortRepository;
    private final SeatReservationRepository seatReservationRepository;

    public CohortSeatService(CohortRepository cohortRepository,
                             SeatReservationRepository seatReservationRepository) {
        this.cohortRepository = cohortRepository;
        this.seatReservationRepository = seatReservationRepository;
    }

    /**
     * Atomically reserves a seat in a cohort using pessimistic row locking to prevent overselling.
     */
    @Transactional
    public SeatReservation reserveSeat(UUID cohortId, UUID applicantUserId, UUID applicationId, UUID offerId, int durationMinutes) {
        Cohort cohort = cohortRepository.findByIdForUpdate(cohortId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cohort not found: " + cohortId));

        if (cohort.getStatus() != CohortStatus.OPEN_FOR_ENROLLMENT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cohort is not open for enrollment. Current status: " + cohort.getStatus());
        }

        Instant now = Instant.now();
        long activeReservations = seatReservationRepository.countActiveReservationsByCohortId(cohortId, now);
        long occupiedSeats = activeReservations + cohort.getOccupiedSeats();

        if (occupiedSeats >= cohort.getCapacity()) {
            cohort.setStatus(CohortStatus.FULL);
            cohortRepository.save(cohort);
            log.warn("Cohort {} capacity ({}) exceeded. Occupied seats: {}", cohort.getCohortCode(), cohort.getCapacity(), occupiedSeats);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No seats available in cohort: " + cohort.getCohortCode());
        }

        Instant expiresAt = now.plus(durationMinutes > 0 ? durationMinutes : 15, ChronoUnit.MINUTES);
        String token = "RES-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();

        SeatReservation reservation = new SeatReservation(cohortId, token, expiresAt);
        reservation.setApplicantUserId(applicantUserId);
        reservation.setApplicationId(applicationId);
        reservation.setOfferId(offerId);

        SeatReservation saved = seatReservationRepository.save(reservation);
        log.info("Reserved seat {} in cohort {} for applicant {}. Expires at {}", token, cohort.getCohortCode(), applicantUserId, expiresAt);
        return saved;
    }

    @Transactional
    public void confirmReservation(UUID reservationId) {
        if (reservationId == null) return;
        seatReservationRepository.findById(reservationId).ifPresent(reservation -> {
            reservation.setStatus(SeatReservationStatus.CONFIRMED);
            seatReservationRepository.save(reservation);

            cohortRepository.findByIdForUpdate(reservation.getCohortId()).ifPresent(cohort -> {
                cohort.setOccupiedSeats(cohort.getOccupiedSeats() + 1);
                if (cohort.getOccupiedSeats() >= cohort.getCapacity()) {
                    cohort.setStatus(CohortStatus.FULL);
                }
                cohortRepository.save(cohort);
                log.info("Confirmed seat reservation: {}. Cohort {} occupied: {}/{}",
                        reservation.getReservationToken(), cohort.getCohortCode(), cohort.getOccupiedSeats(), cohort.getCapacity());
            });
        });
    }

    @Transactional
    public void recordDirectEnrollment(UUID cohortId) {
        if (cohortId == null) return;
        cohortRepository.findByIdForUpdate(cohortId).ifPresent(cohort -> {
            cohort.setOccupiedSeats(cohort.getOccupiedSeats() + 1);
            if (cohort.getOccupiedSeats() >= cohort.getCapacity()) {
                cohort.setStatus(CohortStatus.FULL);
            }
            cohortRepository.save(cohort);
            log.info("Direct enrollment recorded in cohort {}. Occupied: {}/{}", cohort.getCohortCode(), cohort.getOccupiedSeats(), cohort.getCapacity());
        });
    }

    @Transactional
    public void cancelReservation(UUID reservationId) {
        if (reservationId == null) return;
        seatReservationRepository.findById(reservationId).ifPresent(reservation -> {
            reservation.setStatus(SeatReservationStatus.CANCELLED);
            seatReservationRepository.save(reservation);
            log.info("Cancelled seat reservation: {}", reservation.getReservationToken());
        });
    }

    @Transactional(readOnly = true)
    public int getAvailableSeats(UUID cohortId) {
        Cohort cohort = cohortRepository.findById(cohortId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cohort not found: " + cohortId));
        Instant now = Instant.now();
        long activeReservations = seatReservationRepository.countActiveReservationsByCohortId(cohortId, now);
        int available = (int) (cohort.getCapacity() - (activeReservations + cohort.getOccupiedSeats()));
        return Math.max(0, available);
    }

    @Transactional
    public int releaseExpiredReservations() {
        Instant now = Instant.now();
        List<SeatReservation> expired = seatReservationRepository.findExpiredReservations(now);
        for (SeatReservation r : expired) {
            r.setStatus(SeatReservationStatus.EXPIRED);
            seatReservationRepository.save(r);
        }
        if (!expired.isEmpty()) {
            log.info("Released {} expired seat reservations", expired.size());
        }
        return expired.size();
    }

    @Transactional(readOnly = true)
    public Cohort getCohort(UUID cohortId) {
        return cohortRepository.findById(cohortId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cohort not found: " + cohortId));
    }
}
