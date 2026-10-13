package com.nexarats.academy.academic.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "short_description", nullable = false, length = 500)
    private String shortDescription;

    @Column(name = "detailed_description", columnDefinition = "TEXT")
    private String detailedDescription;

    @Column(name = "image_asset_key", nullable = false, length = 100)
    private String imageAssetKey;

    @Column(nullable = false, length = 50)
    private String level;

    @Column(name = "duration_weeks", nullable = false)
    private int durationWeeks = 8;

    @Column(name = "lessons_count", nullable = false)
    private int lessonsCount = 0;

    @Column(name = "projects_count", nullable = false)
    private int projectsCount = 0;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.valueOf(4.90);

    @Column(name = "reviews_count", nullable = false)
    private int reviewsCount = 0;

    @Column(name = "enrolled_students", nullable = false, length = 50)
    private String enrolledStudents = "1,000+";

    @Column(name = "instructor_name", nullable = false, length = 100)
    private String instructorName;

    @Column(name = "instructor_company", nullable = false, length = 100)
    private String instructorCompany;

    @Column(length = 50)
    private String badge;

    @Column(columnDefinition = "TEXT")
    private String prerequisites;

    @Column(name = "tech_stack", nullable = false, columnDefinition = "TEXT")
    private String techStack = "[]";

    @Column(name = "learning_outcomes", nullable = false, columnDefinition = "TEXT")
    private String learningOutcomes = "[]";

    @Column(nullable = false, length = 30)
    private String status = "PUBLISHED";

    @Column(name = "published_version_id")
    private UUID publishedVersionId;

    @Column(name = "is_featured", nullable = false)
    private boolean isFeatured = false;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    @Column(name = "published_at")
    private Instant publishedAt = Instant.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @ManyToMany
    @JoinTable(
            name = "program_courses",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "program_id")
    )
    private List<Program> programs = new ArrayList<>();

    @OneToMany(mappedBy = "course")
    private List<CurriculumVersion> curriculumVersions = new ArrayList<>();

    public Course() {
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDetailedDescription() {
        return detailedDescription;
    }

    public void setDetailedDescription(String detailedDescription) {
        this.detailedDescription = detailedDescription;
    }

    public String getImageAssetKey() {
        return imageAssetKey;
    }

    public void setImageAssetKey(String imageAssetKey) {
        this.imageAssetKey = imageAssetKey;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public int getDurationWeeks() {
        return durationWeeks;
    }

    public void setDurationWeeks(int durationWeeks) {
        this.durationWeeks = durationWeeks;
    }

    public int getLessonsCount() {
        return lessonsCount;
    }

    public void setLessonsCount(int lessonsCount) {
        this.lessonsCount = lessonsCount;
    }

    public int getProjectsCount() {
        return projectsCount;
    }

    public void setProjectsCount(int projectsCount) {
        this.projectsCount = projectsCount;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public int getReviewsCount() {
        return reviewsCount;
    }

    public void setReviewsCount(int reviewsCount) {
        this.reviewsCount = reviewsCount;
    }

    public String getEnrolledStudents() {
        return enrolledStudents;
    }

    public void setEnrolledStudents(String enrolledStudents) {
        this.enrolledStudents = enrolledStudents;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }

    public String getInstructorCompany() {
        return instructorCompany;
    }

    public void setInstructorCompany(String instructorCompany) {
        this.instructorCompany = instructorCompany;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public String getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(String prerequisites) {
        this.prerequisites = prerequisites;
    }

    public String getTechStack() {
        return techStack;
    }

    public void setTechStack(String techStack) {
        this.techStack = techStack;
    }

    public String getLearningOutcomes() {
        return learningOutcomes;
    }

    public void setLearningOutcomes(String learningOutcomes) {
        this.learningOutcomes = learningOutcomes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getPublishedVersionId() {
        return publishedVersionId;
    }

    public void setPublishedVersionId(UUID publishedVersionId) {
        this.publishedVersionId = publishedVersionId;
    }

    public boolean isFeatured() {
        return isFeatured;
    }

    public void setFeatured(boolean featured) {
        isFeatured = featured;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Program> getPrograms() {
        return programs;
    }

    public void setPrograms(List<Program> programs) {
        this.programs = programs;
    }

    public List<CurriculumVersion> getCurriculumVersions() {
        return curriculumVersions;
    }

    public void setCurriculumVersions(List<CurriculumVersion> curriculumVersions) {
        this.curriculumVersions = curriculumVersions;
    }

    public CurriculumVersion getPublishedVersion() {
        if (publishedVersionId != null && curriculumVersions != null) {
            for (CurriculumVersion v : curriculumVersions) {
                if (publishedVersionId.equals(v.getId())) {
                    return v;
                }
            }
        }
        if (curriculumVersions != null) {
            for (CurriculumVersion v : curriculumVersions) {
                if ("PUBLISHED".equals(v.getStatus())) {
                    return v;
                }
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return Objects.equals(id, course.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
