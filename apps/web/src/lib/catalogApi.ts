import { useQuery } from '@tanstack/react-query'
import type {
  ProgramSummaryDto,
  ProgramDetailDto,
  ProgramCategoryDto,
  PaginatedProgramsResponse,
  ProgramFilterParams,
  CourseSummaryDto,
  CourseDetailDto,
  CourseInstructorDto,
  CourseProgramRefDto,
  LessonSummaryDto,
  CurriculumModuleDto,
  CurriculumSyllabusDto,
  LessonPreviewDto,
  PaginatedCoursesResponse,
  CourseFilterParams,
} from '@nexarats/api-contracts'

export type {
  ProgramSummaryDto,
  ProgramDetailDto,
  ProgramCategoryDto,
  PaginatedProgramsResponse,
  ProgramFilterParams,
  CourseSummaryDto,
  CourseDetailDto,
  CourseInstructorDto,
  CourseProgramRefDto,
  LessonSummaryDto,
  CurriculumModuleDto,
  CurriculumSyllabusDto,
  LessonPreviewDto,
  PaginatedCoursesResponse,
  CourseFilterParams,
}

export const programQueryKeys = {
  all: ['programs'] as const,
  lists: () => [...programQueryKeys.all, 'list'] as const,
  list: (filters: ProgramFilterParams) => [...programQueryKeys.lists(), filters] as const,
  details: () => [...programQueryKeys.all, 'detail'] as const,
  detail: (slug: string) => [...programQueryKeys.details(), slug] as const,
  categories: () => ['program-categories'] as const,
}

export const courseQueryKeys = {
  all: ['courses'] as const,
  lists: () => [...courseQueryKeys.all, 'list'] as const,
  list: (filters: CourseFilterParams) => [...courseQueryKeys.lists(), filters] as const,
  details: () => [...courseQueryKeys.all, 'detail'] as const,
  detail: (slug: string) => [...courseQueryKeys.details(), slug] as const,
  syllabuses: () => [...courseQueryKeys.all, 'syllabus'] as const,
  syllabus: (slug: string) => [...courseQueryKeys.syllabuses(), slug] as const,
  previews: () => [...courseQueryKeys.all, 'preview'] as const,
  preview: (slug: string, lessonId: string) => [...courseQueryKeys.previews(), slug, lessonId] as const,
}

export async function fetchPrograms(params: ProgramFilterParams = {}): Promise<PaginatedProgramsResponse> {
  const query = new URLSearchParams()
  if (params.category && params.category !== 'all') query.set('category', params.category)
  if (params.duration !== undefined && params.duration !== null) query.set('duration', params.duration.toString())
  if (params.featured !== undefined && params.featured !== null) query.set('featured', params.featured.toString())
  if (params.search) query.set('search', params.search)
  if (params.q) query.set('q', params.q)
  if (params.page !== undefined) query.set('page', params.page.toString())
  if (params.size !== undefined) query.set('size', params.size.toString())
  if (params.sort) query.set('sort', params.sort)

  const url = `/api/v1/academic/programs${query.toString() ? `?${query.toString()}` : ''}`
  const response = await fetch(url, {
    headers: {
      Accept: 'application/json',
    },
  })

  if (!response.ok) {
    throw new Error(`Failed to load programs: ${response.status} ${response.statusText}`)
  }

  return response.json()
}

export async function fetchProgramDetail(slug: string): Promise<ProgramDetailDto> {
  const response = await fetch(`/api/v1/academic/programs/${encodeURIComponent(slug)}`, {
    headers: {
      Accept: 'application/json',
    },
  })

  if (!response.ok) {
    if (response.status === 404) {
      throw new Error(`Program not found (${slug})`)
    }
    throw new Error(`Failed to load program details: ${response.status} ${response.statusText}`)
  }

  return response.json()
}

export async function fetchCategories(): Promise<ProgramCategoryDto[]> {
  const response = await fetch('/api/v1/academic/program-categories', {
    headers: {
      Accept: 'application/json',
    },
  })

  if (!response.ok) {
    throw new Error(`Failed to load categories: ${response.status} ${response.statusText}`)
  }

  return response.json()
}

export function usePrograms(params: ProgramFilterParams = {}) {
  return useQuery({
    queryKey: programQueryKeys.list(params),
    queryFn: () => fetchPrograms(params),
    staleTime: 5 * 60 * 1000, // 5 minutes
    retry: 1,
  })
}

export function useProgramDetail(slug?: string) {
  return useQuery({
    queryKey: slug ? programQueryKeys.detail(slug) : ['program-detail', 'none'],
    queryFn: () => fetchProgramDetail(slug!),
    enabled: Boolean(slug),
    staleTime: 5 * 60 * 1000,
    retry: 1,
  })
}

export function useCategories() {
  return useQuery({
    queryKey: programQueryKeys.categories(),
    queryFn: fetchCategories,
    staleTime: 15 * 60 * 1000, // 15 minutes
    retry: 1,
  })
}

/* =========================================================================
 * Course API Client Functions & TanStack Query Hooks
 * ========================================================================= */

export async function fetchCourses(params: CourseFilterParams = {}): Promise<PaginatedCoursesResponse> {
  const query = new URLSearchParams()
  if (params.category && params.category !== 'all') query.set('category', params.category)
  if (params.level && params.level !== 'all') query.set('level', params.level)
  if (params.featured !== undefined && params.featured !== null) query.set('featured', params.featured.toString())
  if (params.search) query.set('search', params.search)
  if (params.q) query.set('q', params.q)
  if (params.page !== undefined) query.set('page', params.page.toString())
  if (params.size !== undefined) query.set('size', params.size.toString())
  if (params.sort) query.set('sort', params.sort)

  const url = `/api/v1/academic/courses${query.toString() ? `?${query.toString()}` : ''}`
  const response = await fetch(url, {
    headers: {
      Accept: 'application/json',
    },
  })

  if (!response.ok) {
    throw new Error(`Failed to load courses: ${response.status} ${response.statusText}`)
  }

  return response.json()
}

export async function fetchCourseDetail(slug: string): Promise<CourseDetailDto> {
  const response = await fetch(`/api/v1/academic/courses/${encodeURIComponent(slug)}`, {
    headers: {
      Accept: 'application/json',
    },
  })

  if (!response.ok) {
    if (response.status === 404) {
      throw new Error(`Course not found (${slug})`)
    }
    throw new Error(`Failed to load course details: ${response.status} ${response.statusText}`)
  }

  return response.json()
}

export async function fetchCourseSyllabus(slug: string): Promise<CurriculumSyllabusDto> {
  const response = await fetch(`/api/v1/academic/courses/${encodeURIComponent(slug)}/syllabus`, {
    headers: {
      Accept: 'application/json',
    },
  })

  if (!response.ok) {
    if (response.status === 404) {
      throw new Error(`Course syllabus not found (${slug})`)
    }
    throw new Error(`Failed to load course syllabus: ${response.status} ${response.statusText}`)
  }

  return response.json()
}

export async function fetchLessonPreview(slug: string, lessonId: string): Promise<LessonPreviewDto> {
  const response = await fetch(
    `/api/v1/academic/courses/${encodeURIComponent(slug)}/lessons/${encodeURIComponent(lessonId)}/preview`,
    {
      headers: {
        Accept: 'application/json',
      },
    }
  )

  if (!response.ok) {
    if (response.status === 404) {
      throw new Error(`Lesson preview not found (${lessonId})`)
    }
    if (response.status === 403) {
      throw new Error('This lesson is locked and requires enrolled access.')
    }
    throw new Error(`Failed to load lesson preview: ${response.status} ${response.statusText}`)
  }

  return response.json()
}

export function useCourses(params: CourseFilterParams = {}) {
  return useQuery({
    queryKey: courseQueryKeys.list(params),
    queryFn: () => fetchCourses(params),
    staleTime: 5 * 60 * 1000, // 5 minutes
    retry: 1,
  })
}

export function useCourseDetail(slug?: string) {
  return useQuery({
    queryKey: slug ? courseQueryKeys.detail(slug) : ['course-detail', 'none'],
    queryFn: () => fetchCourseDetail(slug!),
    enabled: Boolean(slug),
    staleTime: 5 * 60 * 1000,
    retry: 1,
  })
}

export function useCourseSyllabus(slug?: string) {
  return useQuery({
    queryKey: slug ? courseQueryKeys.syllabus(slug) : ['course-syllabus', 'none'],
    queryFn: () => fetchCourseSyllabus(slug!),
    enabled: Boolean(slug),
    staleTime: 5 * 60 * 1000,
    retry: 1,
  })
}

export function useLessonPreview(slug?: string, lessonId?: string) {
  return useQuery({
    queryKey: slug && lessonId ? courseQueryKeys.preview(slug, lessonId) : ['lesson-preview', 'none'],
    queryFn: () => fetchLessonPreview(slug!, lessonId!),
    enabled: Boolean(slug && lessonId),
    staleTime: 5 * 60 * 1000,
    retry: 1,
  })
}
