import React, { useState, useEffect, useCallback } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import {
  ChevronLeft,
  ChevronRight,
  CheckCircle2,
  ArrowLeft,
  Loader2,
  AlertCircle,
  Menu,
  X,
} from 'lucide-react'
import { useAuth } from '@/context'
import {
  useLearnerSyllabus,
  useLessonDetail,
  createPlaybackSession,
  recordPlaybackProgress,
  markLessonComplete,
} from '@/lib/learningApi'
import { VideoPlayer } from '@/components/learning/VideoPlayer'
import { SyllabusSidebar } from '@/components/learning/SyllabusSidebar'
import { cn } from '@/lib/utils'
import type { PlaybackSessionResponse } from '@nexarats/api-contracts'

export const CoursePlayerPage: React.FC = () => {
  const { courseId, lessonId: paramLessonId } = useParams<{ courseId: string; lessonId?: string }>()
  const { accessToken, isAuthenticated } = useAuth()
  const navigate = useNavigate()

  // Fetch Syllabus
  const {
    data: syllabus,
    isLoading: isLoadingSyllabus,
    error: syllabusError,
    refetch: refetchSyllabus,
  } = useLearnerSyllabus(courseId || '', accessToken, isAuthenticated)

  const firstLessonId = syllabus?.modules?.[0]?.lessons?.[0]?.lessonId || null
  const activeLessonId = paramLessonId || firstLessonId

  const [session, setSession] = useState<PlaybackSessionResponse | null>(null)
  const [sessionError, setSessionError] = useState<string | null>(null)
  const [mobileSidebarOpen, setMobileSidebarOpen] = useState<boolean>(false)

  const isLoadingSession = Boolean(activeLessonId && accessToken && !session && !sessionError)

  // Navigate to first lesson if none specified in route
  useEffect(() => {
    if (!paramLessonId && firstLessonId && courseId) {
      navigate(`/learning/courses/${courseId}/lessons/${firstLessonId}`, { replace: true })
    }
  }, [paramLessonId, firstLessonId, courseId, navigate])

  // Fetch Lesson Detail
  const {
    data: lessonDetail,
    refetch: refetchLesson,
  } = useLessonDetail(courseId || '', activeLessonId || '', accessToken, !!activeLessonId)

  // Authorize Playback Session
  const initPlaybackSession = useCallback((lessonId: string) => {
    if (!accessToken) return
    setSession(null)
    setSessionError(null)
    createPlaybackSession(lessonId, accessToken)
      .then((sess) => {
        setSession(sess)
      })
      .catch((err) => {
        setSessionError((err as Error).message || 'Failed to authorize secure playback.')
        setSession(null)
      })
  }, [accessToken])

  useEffect(() => {
    let isSubscribed = true
    if (!activeLessonId || !accessToken) {
      return
    }

    createPlaybackSession(activeLessonId, accessToken)
      .then((sess) => {
        if (isSubscribed) {
          setSession(sess)
          setSessionError(null)
        }
      })
      .catch((err) => {
        if (isSubscribed) {
          setSessionError((err as Error).message || 'Failed to authorize secure playback.')
          setSession(null)
        }
      })

    return () => {
      isSubscribed = false
    }
  }, [activeLessonId, accessToken])

  // Checkpoint Handler
  const handleCheckpoint = async (positionSeconds: number, deltaSeconds: number) => {
    if (!activeLessonId || !accessToken) return
    try {
      await recordPlaybackProgress(
        activeLessonId,
        {
          playbackPositionSeconds: positionSeconds,
          watchTimeDeltaSeconds: deltaSeconds,
          idempotencyKey: `chk_${activeLessonId}_${positionSeconds}_${Date.now()}`,
          sessionToken: session?.sessionToken,
        },
        accessToken
      )
      refetchSyllabus()
    } catch (err) {
      console.warn('Error recording playback checkpoint:', err)
    }
  }

  // Complete Handler
  const handleComplete = async () => {
    if (!activeLessonId || !accessToken) return
    try {
      await markLessonComplete(activeLessonId, accessToken)
      refetchSyllabus()
      refetchLesson()
    } catch (err) {
      console.warn('Error marking lesson complete:', err)
    }
  }

  const handleSelectLesson = (lessonId: string) => {
    setMobileSidebarOpen(false)
    navigate(`/learning/courses/${courseId}/lessons/${lessonId}`)
  }

  if (isLoadingSyllabus) {
    return (
      <div className="min-h-screen bg-slate-900 flex items-center justify-center text-white">
        <div className="text-center">
          <Loader2 className="w-10 h-10 animate-spin text-brand-500 mx-auto mb-3" />
          <p className="text-sm font-semibold text-slate-400">Loading course classroom...</p>
        </div>
      </div>
    )
  }

  if (syllabusError || !syllabus) {
    return (
      <div className="min-h-[70vh] flex items-center justify-center academy-container py-16">
        <div className="max-w-md w-full bg-white rounded-3xl p-8 border border-rose-100 shadow-elevated text-center">
          <AlertCircle className="w-12 h-12 text-rose-500 mx-auto mb-3" />
          <h2 className="text-lg font-bold text-slate-900 mb-2">Access Denied or Not Enrolled</h2>
          <p className="text-xs text-slate-500 mb-4">
            {(syllabusError as Error)?.message || 'You must be actively enrolled in this course to access the classroom.'}
          </p>
          <Link
            to="/learning"
            className="inline-flex items-center gap-2 px-5 py-2.5 bg-brand-600 hover:bg-brand-700 text-white font-semibold text-xs rounded-full transition-all"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Return to Dashboard</span>
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-slate-100/70 pb-16">
      {/* Top Classroom Navigation Bar */}
      <header className="sticky top-0 z-30 bg-slate-900 text-white border-b border-slate-800 shadow-md">
        <div className="academy-container">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center gap-3">
              <Link
                to="/learning"
                className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-xl transition-colors cursor-pointer"
                title="Back to Dashboard"
              >
                <ArrowLeft className="w-5 h-5" />
              </Link>
              <div className="h-5 w-px bg-slate-800" />
              <div>
                <h1 className="text-xs sm:text-sm font-bold text-white truncate max-w-[200px] sm:max-w-md">
                  {syllabus.courseTitle}
                </h1>
                <div className="text-[10px] text-slate-400 hidden sm:block">
                  {syllabus.versionLabel} • {syllabus.overallProgressPercentage}% Complete
                </div>
              </div>
            </div>

            {/* Mobile Syllabus Toggle Button */}
            <div className="lg:hidden">
              <button
                onClick={() => setMobileSidebarOpen(!mobileSidebarOpen)}
                className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-white rounded-lg text-xs font-semibold"
              >
                {mobileSidebarOpen ? <X className="w-4 h-4" /> : <Menu className="w-4 h-4" />}
                <span>Syllabus</span>
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="academy-container py-6">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          {/* Main Stage (Player & Lesson Content) */}
          <div className="lg:col-span-8 space-y-6">
            {/* Video Player */}
            <VideoPlayer
              streamUrl={session?.streamUrl || null}
              durationSeconds={session?.durationSeconds || (lessonDetail?.durationMinutes ? lessonDetail.durationMinutes * 60 : 1800)}
              initialPositionSeconds={session?.lastPlaybackPositionSeconds || 0}
              isCompleted={lessonDetail?.completed || false}
              isLoadingSession={isLoadingSession}
              error={sessionError}
              onCheckpoint={handleCheckpoint}
              onComplete={handleComplete}
              onRetrySession={() => activeLessonId && initPlaybackSession(activeLessonId)}
            />

            {/* Lesson Detail & Actions */}
            {lessonDetail && (
              <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200 shadow-sm space-y-6">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-100 pb-5">
                  <div>
                    <div className="flex items-center gap-2 mb-1.5">
                      <span className="px-2.5 py-0.5 rounded-full bg-slate-100 text-slate-700 text-[10px] font-bold uppercase tracking-wider">
                        {lessonDetail.lessonType}
                      </span>
                      <span className="text-xs font-medium text-slate-500">
                        {lessonDetail.durationMinutes} minutes
                      </span>
                    </div>
                    <h2 className="text-xl sm:text-2xl font-bold text-slate-900">
                      {lessonDetail.lessonTitle}
                    </h2>
                  </div>

                  {/* Mark Completed Button */}
                  <button
                    onClick={handleComplete}
                    disabled={lessonDetail.completed}
                    className={cn(
                      'inline-flex items-center gap-2 px-5 py-2.5 rounded-full text-xs font-bold transition-all cursor-pointer select-none shrink-0',
                      lessonDetail.completed
                        ? 'bg-emerald-50 text-emerald-700 border border-emerald-200 cursor-default'
                        : 'bg-brand-600 hover:bg-brand-700 active:bg-brand-800 text-white shadow-button-glow'
                    )}
                  >
                    <CheckCircle2 className="w-4 h-4" />
                    <span>{lessonDetail.completed ? 'Completed' : 'Mark as Completed'}</span>
                  </button>
                </div>

                {/* Lesson Description & Summary */}
                <div className="space-y-3">
                  <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400">
                    Lesson Overview
                  </h4>
                  <p className="text-sm text-slate-700 leading-relaxed">
                    {lessonDetail.contentBody || lessonDetail.summary}
                  </p>
                </div>

                {/* Next / Previous Controls */}
                <div className="flex items-center justify-between pt-6 border-t border-slate-100">
                  {lessonDetail.previousLessonId ? (
                    <button
                      onClick={() => handleSelectLesson(lessonDetail.previousLessonId!)}
                      className="inline-flex items-center gap-1.5 px-4 py-2 border border-slate-200 hover:bg-slate-50 text-slate-700 font-semibold text-xs rounded-xl transition-all cursor-pointer"
                    >
                      <ChevronLeft className="w-4 h-4" />
                      <span>Previous Lesson</span>
                    </button>
                  ) : <div />}

                  {lessonDetail.nextLessonId ? (
                    <button
                      onClick={() => handleSelectLesson(lessonDetail.nextLessonId!)}
                      className="inline-flex items-center gap-1.5 px-5 py-2 bg-brand-600 hover:bg-brand-700 text-white font-bold text-xs rounded-xl shadow-button-glow transition-all cursor-pointer"
                    >
                      <span>Next Lesson</span>
                      <ChevronRight className="w-4 h-4" />
                    </button>
                  ) : <div />}
                </div>
              </div>
            )}
          </div>

          {/* Desktop Sidebar */}
          <div className="hidden lg:block lg:col-span-4 sticky top-24 h-[calc(100vh-7rem)]">
            <SyllabusSidebar
              modules={syllabus.modules}
              activeLessonId={activeLessonId || ''}
              overallProgressPercentage={syllabus.overallProgressPercentage}
              totalLessonsCount={syllabus.totalLessonsCount}
              completedLessonsCount={syllabus.completedLessonsCount}
              onSelectLesson={handleSelectLesson}
            />
          </div>
        </div>

        {/* Mobile Modal Drawer for Sidebar */}
        {mobileSidebarOpen && (
          <div className="fixed inset-0 z-50 lg:hidden flex flex-col bg-white">
            <div className="p-4 border-b border-slate-200 flex items-center justify-between bg-slate-900 text-white">
              <h3 className="text-sm font-bold">Course Syllabus</h3>
              <button
                onClick={() => setMobileSidebarOpen(false)}
                className="p-1 hover:bg-slate-800 rounded-lg"
              >
                <X className="w-5 h-5" />
              </button>
            </div>
            <div className="flex-1 overflow-hidden p-4">
              <SyllabusSidebar
                modules={syllabus.modules}
                activeLessonId={activeLessonId || ''}
                overallProgressPercentage={syllabus.overallProgressPercentage}
                totalLessonsCount={syllabus.totalLessonsCount}
                completedLessonsCount={syllabus.completedLessonsCount}
                onSelectLesson={handleSelectLesson}
              />
            </div>
          </div>
        )}
      </main>
    </div>
  )
}
