/**
 * Nexarats Academy - Modular Course Catalog & Syllabus Engine API Contracts
 * Enterprise Type Definitions for Web (React) and Mobile (Flutter/Dart compatible)
 */

export interface CourseInstructorDto {
  name: string
  company: string
}

export interface CourseProgramRefDto {
  id: string
  slug: string
  title: string
}

export interface CourseSummaryDto {
  id: string
  slug: string
  title: string
  category: string
  level: string
  durationWeeks: number
  lessonsCount: number
  projectsCount: number
  rating: number
  reviewsCount: number
  enrolledStudents: string
  instructor: CourseInstructorDto
  imageAssetKey: string
  badge?: string | null
  shortDescription: string
  techStack: string[]
  isFeatured: boolean
  displayOrder: number
}

export interface CourseDetailDto {
  id: string
  slug: string
  title: string
  category: string
  level: string
  durationWeeks: number
  lessonsCount: number
  projectsCount: number
  rating: number
  reviewsCount: number
  enrolledStudents: string
  instructor: CourseInstructorDto
  imageAssetKey: string
  badge?: string | null
  shortDescription: string
  detailedDescription?: string | null
  techStack: string[]
  learningOutcomes: string[]
  prerequisites?: string | null
  publishedVersionLabel: string
  modulesCount: number
  relatedPrograms: CourseProgramRefDto[]
  isFeatured: boolean
  displayOrder: number
  publishedAt?: string | null
}

export interface LessonSummaryDto {
  id: string
  title: string
  summary?: string | null
  lessonType: string
  durationMinutes: number
  isPreviewable: boolean
  displayOrder: number
  isLocked: boolean
}

export interface CurriculumModuleDto {
  id: string
  number: string
  title: string
  duration: string
  displayOrder: number
  lessonsCount: number
  topics: string[]
  lessons: LessonSummaryDto[]
}

export interface CurriculumSyllabusDto {
  courseId: string
  courseSlug: string
  courseTitle: string
  versionLabel: string
  publishedAt?: string | null
  totalModules: number
  totalLessons: number
  modules: CurriculumModuleDto[]
}

export interface LessonPreviewDto {
  lessonId: string
  courseSlug: string
  moduleNumber: string
  title: string
  summary?: string | null
  lessonType: string
  durationMinutes: number
  previewContent?: string | null
}

export interface PaginatedCoursesResponse {
  items: CourseSummaryDto[]
  total: number
  page: number
  size: number
  totalPages: number
}

export interface CourseFilterParams {
  category?: string
  level?: string
  featured?: boolean
  search?: string
  q?: string
  page?: number
  size?: number
  sort?: string
}
