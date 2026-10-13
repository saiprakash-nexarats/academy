package com.nexarats.academy.cohort.web;

import com.nexarats.academy.cohort.dto.CohortDto;
import com.nexarats.academy.cohort.model.Cohort;
import com.nexarats.academy.cohort.model.CohortStatus;
import com.nexarats.academy.cohort.repository.CohortRepository;
import com.nexarats.academy.cohort.service.CohortSeatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Public REST Controller for Cohorts and Intake Management.
 */
@RestController
@RequestMapping({"/api/v1/admissions/cohorts", "/api/v1/cohorts"})
public class CohortController {

    private final CohortRepository cohortRepository;
    private final CohortSeatService cohortSeatService;

    public CohortController(CohortRepository cohortRepository, CohortSeatService cohortSeatService) {
        this.cohortRepository = cohortRepository;
        this.cohortSeatService = cohortSeatService;
    }

    @GetMapping
    public ResponseEntity<List<CohortDto>> listCohorts(
            @RequestParam(required = false) UUID programId,
            @RequestParam(required = false) UUID courseId,
            @RequestParam(required = false) CohortStatus status) {

        List<Cohort> cohorts;
        if (programId != null) {
            cohorts = cohortRepository.findByProgramId(programId);
        } else if (courseId != null) {
            cohorts = cohortRepository.findByCourseId(courseId);
        } else if (status != null) {
            cohorts = cohortRepository.findByStatus(status);
        } else {
            cohorts = cohortRepository.findAll();
        }

        List<CohortDto> dtos = cohorts.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CohortDto> getCohort(@PathVariable UUID id) {
        Cohort cohort = cohortSeatService.getCohort(id);
        return ResponseEntity.ok(toDto(cohort));
    }

    private CohortDto toDto(Cohort cohort) {
        int availableSeats = cohortSeatService.getAvailableSeats(cohort.getId());
        return new CohortDto(
                cohort.getId(),
                cohort.getCohortCode(),
                cohort.getName(),
                cohort.getProgramId(),
                cohort.getCourseId(),
                cohort.getStartDate(),
                cohort.getEndDate(),
                cohort.getEnrollmentEndDate(),
                cohort.getCapacity(),
                availableSeats,
                cohort.getStatus()
        );
    }
}
