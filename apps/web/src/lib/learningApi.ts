import { useQuery } from '@tanstack/react-query'
import type {
  LearnerDashboardResponse,
  EnrolledCourseSummary,
  LearnerSyllabusResponse,
  LessonDetailResponse,
  CourseProgressResponse,
  PlaybackSessionResponse,
  PlaybackCheckpointRequest,
  PlaybackCheckpointResponse,
} from '@nexarats/api-contracts'

function getAuthHeaders(token?: string | null): Record<string, string> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  }
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }
  return headers
}

export async function fetchLearnerDashboard(token?: string | null): Promise<LearnerDashboardResponse> {
  const res = await fetch('/api/v1/learning/dashboard', {
    headers: getAuthHeaders(token),
    credentials: 'include',
  })
  if (!res.ok) {
    throw new Error(`Failed to fetch student dashboard (HTTP ${res.status})`)
  }
  return res.json()
}

export async function fetchEnrolledCourses(token?: string | null): Promise<EnrolledCourseSummary[]> {
  const res = await fetch('/api/v1/learning/my-courses', {
    headers: getAuthHeaders(token),
    credentials: 'include',
  })
  if (!res.ok) {
    throw new Error(`Failed to fetch enrolled courses (HTTP ${res.status})`)
  }
  return res.json()
}

export async function fetchLearnerSyllabus(courseId: string, token?: string | null): Promise<LearnerSyllabusResponse> {
  const res = await fetch(`/api/v1/learning/courses/${courseId}/syllabus`, {
    headers: getAuthHeaders(token),
    credentials: 'include',
  })
  if (!res.ok) {
    throw new Error(`Failed to fetch course syllabus (HTTP ${res.status})`)
  }
  return res.json()
}

export async function fetchLessonDetail(
  courseId: string,
  lessonId: string,
  token?: string | null
): Promise<LessonDetailResponse> {
  const res = await fetch(`/api/v1/learning/courses/${courseId}/lessons/${lessonId}`, {
    headers: getAuthHeaders(token),
    credentials: 'include',
  })
  if (!res.ok) {
    throw new Error(`Failed to fetch lesson detail (HTTP ${res.status})`)
  }
  return res.json()
}

export async function fetchCourseProgress(courseId: string, token?: string | null): Promise<CourseProgressResponse> {
  const res = await fetch(`/api/v1/learning/courses/${courseId}/progress`, {
    headers: getAuthHeaders(token),
    credentials: 'include',
  })
  if (!res.ok) {
    throw new Error(`Failed to fetch course progress (HTTP ${res.status})`)
  }
  return res.json()
}

export async function createPlaybackSession(lessonId: string, token?: string | null): Promise<PlaybackSessionResponse> {
  const res = await fetch(`/api/v1/learning/lessons/${lessonId}/playback-session`, {
    method: 'POST',
    headers: getAuthHeaders(token),
    credentials: 'include',
  })
  if (!res.ok) {
    throw new Error(`Failed to authorize playback session (HTTP ${res.status})`)
  }
  return res.json()
}

export async function recordPlaybackProgress(
  lessonId: string,
  data: PlaybackCheckpointRequest,
  token?: string | null
): Promise<PlaybackCheckpointResponse> {
  const res = await fetch(`/api/v1/learning/lessons/${lessonId}/progress`, {
    method: 'POST',
    headers: getAuthHeaders(token),
    credentials: 'include',
    body: JSON.stringify(data),
  })
  if (!res.ok) {
    throw new Error(`Failed to record progress checkpoint (HTTP ${res.status})`)
  }
  return res.json()
}

export async function markLessonComplete(lessonId: string, token?: string | null): Promise<PlaybackCheckpointResponse> {
  const res = await fetch(`/api/v1/learning/lessons/${lessonId}/complete`, {
    method: 'POST',
    headers: getAuthHeaders(token),
    credentials: 'include',
  })
  if (!res.ok) {
    throw new Error(`Failed to mark lesson complete (HTTP ${res.status})`)
  }
  return res.json()
}

// React Query Hooks

export function useLearnerDashboard(token?: string | null, enabled: boolean = true) {
  return useQuery({
    queryKey: ['learner-dashboard', token],
    queryFn: () => fetchLearnerDashboard(token),
    enabled: enabled && !!token,
    staleTime: 60 * 1000,
  })
}

export function useLearnerSyllabus(courseId: string, token?: string | null, enabled: boolean = true) {
  return useQuery({
    queryKey: ['learner-syllabus', courseId, token],
    queryFn: () => fetchLearnerSyllabus(courseId, token),
    enabled: enabled && !!courseId && !!token,
    staleTime: 60 * 1000,
  })
}

export function useLessonDetail(courseId: string, lessonId: string, token?: string | null, enabled: boolean = true) {
  return useQuery({
    queryKey: ['lesson-detail', courseId, lessonId, token],
    queryFn: () => fetchLessonDetail(courseId, lessonId, token),
    enabled: enabled && !!courseId && !!lessonId && !!token,
    staleTime: 30 * 1000,
  })
}

export function useCourseProgress(courseId: string, token?: string | null, enabled: boolean = true) {
  return useQuery({
    queryKey: ['course-progress', courseId, token],
    queryFn: () => fetchCourseProgress(courseId, token),
    enabled: enabled && !!courseId && !!token,
    staleTime: 30 * 1000,
  })
}
