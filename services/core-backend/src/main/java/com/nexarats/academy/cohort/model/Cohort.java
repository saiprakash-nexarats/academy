package com.nexarats.academy.cohort.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "cohorts")
public class Cohort {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "program_id")
    private UUID programId;

    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "cohort_code", nullable = false, unique = true, length = 64)
    private String cohortCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "enrollment_start_date", nullable = false)
    private Instant enrollmentStartDate = Instant.now();

    @Column(name = "enrollment_end_date", nullable = false)
    private Instant enrollmentEndDate;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "occupied_seats", nullable = false)
    private int occupiedSeats = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private CohortStatus status = CohortStatus.OPEN_FOR_ENROLLMENT;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Cohort() {
    }

    public Cohort(String cohortCode, String name, LocalDate startDate, Instant enrollmentEndDate, int capacity) {
        this.cohortCode = cohortCode;
        this.name = name;
        this.startDate = startDate;
        this.enrollmentEndDate = enrollmentEndDate;
        this.capacity = capacity;
        this.status = CohortStatus.OPEN_FOR_ENROLLMENT;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getProgramId() {
        return programId;
    }

    public void setProgramId(UUID programId) {
        this.programId = programId;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public void setCourseId(UUID courseId) {
        this.courseId = courseId;
    }

    public String getCohortCode() {
        return cohortCode;
    }

    public void setCohortCode(String cohortCode) {
        this.cohortCode = cohortCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Instant getEnrollmentStartDate() {
        return enrollmentStartDate;
    }

    public void setEnrollmentStartDate(Instant enrollmentStartDate) {
        this.enrollmentStartDate = enrollmentStartDate;
    }

    public Instant getEnrollmentEndDate() {
        return enrollmentEndDate;
    }

    public void setEnrollmentEndDate(Instant enrollmentEndDate) {
        this.enrollmentEndDate = enrollmentEndDate;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getOccupiedSeats() {
        return occupiedSeats;
    }

    public void setOccupiedSeats(int occupiedSeats) {
        this.occupiedSeats = occupiedSeats;
    }

    public CohortStatus getStatus() {
        return status;
    }

    public void setStatus(CohortStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
