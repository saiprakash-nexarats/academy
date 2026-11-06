export interface ContinueLearningTarget {
  courseId: string;
  courseSlug: string;
  courseTitle: string;
  lessonId: string;
  lessonTitle: string;
  lastPlaybackPositionSeconds: number;
  durationMinutes: number;
}

export interface EnrolledCourseSummary {
  courseId: string;
  slug: string;
  title: string;
  category: string;
  level: string;
  imageAssetKey?: string;
  instructorName?: string;
  instructorCompany?: string;
  enrollmentNumber: string;
  enrollmentStatus: string;
  completionPercentage: number;
  completedLessonsCount: number;
  totalLessonsCount: number;
  currentLessonId?: string | null;
  currentLessonTitle?: string | null;
  lastActivityAt?: string;
}

export interface EnrolledProgramSummary {
  programId: string;
  slug: string;
  title: string;
  duration: string;
  enrollmentNumber: string;
  cohortCode?: string | null;
  completionPercentage: number;
  totalCoursesCount: number;
  completedCoursesCount: number;
}

export interface LearnerDashboardResponse {
  learnerUserId: string;
  fullName: string;
  email: string;
  enrolledCoursesCount: number;
  completedCoursesCount: number;
  totalWatchTimeMinutes: number;
  continueLearningTarget?: ContinueLearningTarget | null;
  enrolledCourses: EnrolledCourseSummary[];
  enrolledPrograms: EnrolledProgramSummary[];
}

export interface LearnerLessonDto {
  lessonId: string;
  lessonTitle: string;
  summary?: string;
  lessonType: string;
  durationMinutes: number;
  displayOrder: number;
  previewable: boolean;
  locked: boolean;
  completed: boolean;
  lastPlaybackPositionSeconds: number;
  watchTimeSeconds: number;
}

export interface LearnerModuleDto {
  moduleId: string;
  moduleNumber: string;
  title: string;
  durationLabel: string;
  displayOrder: number;
  lessons: LearnerLessonDto[];
}

export interface LearnerSyllabusResponse {
  courseId: string;
  courseTitle: string;
  versionLabel: string;
  totalLessonsCount: number;
  completedLessonsCount: number;
  overallProgressPercentage: number;
  modules: LearnerModuleDto[];
}

export interface LessonDetailResponse {
  lessonId: string;
  courseId: string;
  courseTitle: string;
  moduleId: string;
  moduleTitle: string;
  lessonTitle: string;
  summary?: string;
  lessonType: string;
  durationMinutes: number;
  previewable: boolean;
  locked: boolean;
  completed: boolean;
  lastPlaybackPositionSeconds: number;
  watchTimeSeconds: number;
  contentBody?: string;
  previousLessonId?: string | null;
  nextLessonId?: string | null;
  hasMediaStream: boolean;
}

export interface PlaybackSessionResponse {
  sessionToken: string;
  streamUrl: string;
  lessonId: string;
  durationSeconds: number;
  lastPlaybackPositionSeconds: number;
  expiresAt: string;
}

export interface PlaybackCheckpointRequest {
  playbackPositionSeconds: number;
  watchTimeDeltaSeconds: number;
  idempotencyKey?: string;
  sessionToken?: string;
}

export interface PlaybackCheckpointResponse {
  lessonId: string;
  courseId: string;
  lastPlaybackPositionSeconds: number;
  watchTimeSeconds: number;
  lessonCompletionPercentage: number;
  isLessonCompleted: boolean;
  courseCompletionPercentage: number;
  message?: string;
}

export interface CourseProgressResponse {
  courseId: string;
  courseTitle: string;
  enrollmentStatus: string;
  completionPercentage: number;
  completedLessonsCount: number;
  totalLessonsCount: number;
  currentLessonId?: string | null;
  status: string;
  completedAt?: string | null;
  lessonProgresses: LearnerLessonDto[];
}
