import React from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import {
  BookOpen,
  Clock,
  ArrowRight,
  PlayCircle,
  GraduationCap,
  Sparkles,
  Lock,
  ChevronRight,
  AlertCircle,
} from 'lucide-react'
import { useAuth, useModals } from '@/context'
import { useLearnerDashboard } from '@/lib/learningApi'
import { fetchOnboardingStatus } from '@/lib/onboardingApi'

export const DashboardPage: React.FC = () => {
  const { isAuthenticated, accessToken } = useAuth()
  const { openLoginModal } = useModals()
  const navigate = useNavigate()

  const { data: dashboard, isLoading, error, refetch } = useLearnerDashboard(accessToken, isAuthenticated)

  const { data: onboardingStatus } = useQuery({
    queryKey: ['onboarding-status', accessToken],
    queryFn: () => fetchOnboardingStatus(accessToken),
    enabled: Boolean(isAuthenticated && accessToken),
  })

  if (!isAuthenticated) {
    return (
      <div className="min-h-[70vh] flex items-center justify-center academy-container py-16">
        <div className="max-w-md w-full bg-white rounded-3xl p-8 border border-slate-200/90 shadow-elevated text-center">
          <div className="w-16 h-16 rounded-2xl bg-brand-50 border border-brand-100 flex items-center justify-center mx-auto mb-4 text-brand-600">
            <Lock className="w-8 h-8" />
          </div>
          <h2 className="text-xl font-bold text-slate-900 mb-2">Student Portal Access</h2>
          <p className="text-xs text-slate-500 mb-6">
            Please log in with your Nexarats Academy credentials to access your enrolled courses, syllabus, and learning progress.
          </p>
          <button
            onClick={openLoginModal}
            className="w-full py-3 bg-brand-600 hover:bg-brand-700 text-white font-bold text-sm rounded-xl transition-all shadow-button-glow cursor-pointer"
          >
            Sign In to Continue
          </button>
        </div>
      </div>
    )
  }

  if (isLoading) {
    return (
      <div className="min-h-[70vh] flex items-center justify-center academy-container py-16">
        <div className="text-center">
          <div className="w-12 h-12 border-4 border-brand-200 border-t-brand-600 rounded-full animate-spin mx-auto mb-4" />
          <p className="text-xs font-semibold text-slate-500">Loading student learning dashboard...</p>
        </div>
      </div>
    )
  }

  if (error || !dashboard) {
    return (
      <div className="min-h-[70vh] flex items-center justify-center academy-container py-16">
        <div className="max-w-md w-full bg-white rounded-3xl p-8 border border-rose-100 shadow-elevated text-center">
          <AlertCircle className="w-12 h-12 text-rose-500 mx-auto mb-3" />
          <h2 className="text-lg font-bold text-slate-900 mb-2">Unable to Load Dashboard</h2>
          <p className="text-xs text-slate-500 mb-4">
            {(error as Error)?.message || 'Failed to retrieve your student enrollment records.'}
          </p>
          <button
            onClick={() => refetch()}
            className="px-5 py-2.5 bg-brand-600 hover:bg-brand-700 text-white font-semibold text-xs rounded-full transition-all cursor-pointer"
          >
            Retry Connection
          </button>
        </div>
      </div>
    )
  }

  const continueTarget = dashboard.continueLearningTarget
  const hasEnrollments = dashboard.enrolledCourses.length > 0 || dashboard.enrolledPrograms.length > 0

  return (
    <div className="min-h-screen bg-slate-50/60 pb-20">
      {/* Top Banner */}
      <section className="bg-gradient-to-b from-brand-900 via-slate-900 to-slate-900 text-white pt-12 pb-16 border-b border-slate-800">
        <div className="academy-container">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
            <div>
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-brand-500/20 border border-brand-400/30 text-brand-300 text-xs font-semibold mb-3">
                <Sparkles className="w-3.5 h-3.5" />
                <span>Learner Portal</span>
              </div>
              <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-white">
                Welcome back, {dashboard.fullName}!
              </h1>
              <p className="text-xs sm:text-sm text-slate-400 mt-1 max-w-xl">
                Track your active course enrollments, pick up where you left off, and complete your learning milestones.
              </p>
            </div>

            {/* Quick Metrics Bar */}
            <div className="grid grid-cols-3 gap-3 sm:gap-4 shrink-0">
              <div className="bg-white/5 backdrop-blur-md border border-white/10 rounded-2xl p-3 sm:p-4 text-center">
                <div className="text-lg sm:text-2xl font-black text-brand-400">
                  {dashboard.enrolledCoursesCount}
                </div>
                <div className="text-[10px] sm:text-xs text-slate-400 font-medium">Enrolled Courses</div>
              </div>

              <div className="bg-white/5 backdrop-blur-md border border-white/10 rounded-2xl p-3 sm:p-4 text-center">
                <div className="text-lg sm:text-2xl font-black text-emerald-400">
                  {dashboard.completedCoursesCount}
                </div>
                <div className="text-[10px] sm:text-xs text-slate-400 font-medium">Completed</div>
              </div>

              <div className="bg-white/5 backdrop-blur-md border border-white/10 rounded-2xl p-3 sm:p-4 text-center">
                <div className="text-lg sm:text-2xl font-black text-amber-400">
                  {Math.round(dashboard.totalWatchTimeMinutes / 60 * 10) / 10}h
                </div>
                <div className="text-[10px] sm:text-xs text-slate-400 font-medium">Learning Time</div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <div className="academy-container -mt-8 space-y-8">
        {/* Onboarding Pending Alert Banner */}
        {onboardingStatus && onboardingStatus.overallStatus !== 'COMPLETED' && (
          <div className="bg-gradient-to-r from-amber-50 to-orange-50 border border-amber-200/90 rounded-3xl p-5 shadow-card flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
            <div className="flex items-center gap-3.5">
              <div className="w-11 h-11 rounded-2xl bg-amber-500/15 border border-amber-300 flex items-center justify-center text-amber-700 shrink-0">
                <GraduationCap className="w-5 h-5" />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <h4 className="text-sm font-bold text-amber-950">
                    Student Academic Onboarding Incomplete
                  </h4>
                  <span className="px-2 py-0.5 text-[10px] font-bold bg-amber-200 text-amber-900 rounded-md">
                    Action Required
                  </span>
                </div>
                <p className="text-xs text-amber-800 mt-0.5">
                  Complete your student profile, upload your verified avatar, and sign the required legal agreements to activate full LMS access.
                </p>
              </div>
            </div>
            <Link
              to="/portal/student/onboarding"
              className="px-5 py-2.5 bg-amber-600 hover:bg-amber-700 text-white font-bold text-xs rounded-xl shadow-button-glow transition-all whitespace-nowrap shrink-0"
            >
              Complete Onboarding &rarr;
            </Link>
          </div>
        )}

        {/* Continue Learning Target Hero Card */}
        {continueTarget && (
          <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200/90 shadow-elevated transition-all hover:shadow-xl">
            <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
              <div className="space-y-2">
                <div className="flex items-center gap-2">
                  <span className="px-2.5 py-0.5 rounded-full bg-brand-50 border border-brand-200 text-brand-700 text-[10px] font-bold uppercase tracking-wider">
                    Next Lesson
                  </span>
                  <span className="text-xs font-semibold text-slate-500">
                    {continueTarget.courseTitle}
                  </span>
                </div>
                <h3 className="text-lg sm:text-xl font-bold text-slate-900">
                  {continueTarget.lessonTitle}
                </h3>
                <div className="flex items-center gap-4 text-xs text-slate-500">
                  <span className="flex items-center gap-1.5">
                    <Clock className="w-3.5 h-3.5 text-slate-400" />
                    {continueTarget.durationMinutes} minutes
                  </span>
                  {continueTarget.lastPlaybackPositionSeconds > 0 && (
                    <span className="text-brand-600 font-medium">
                      Resume at {Math.floor(continueTarget.lastPlaybackPositionSeconds / 60)}m
                    </span>
                  )}
                </div>
              </div>

              <button
                onClick={() => {
                  if (onboardingStatus && onboardingStatus.overallStatus !== 'COMPLETED') {
                    navigate('/portal/student/onboarding')
                  } else {
                    navigate(`/learning/courses/${continueTarget.courseId}/lessons/${continueTarget.lessonId}`)
                  }
                }}
                className="inline-flex items-center gap-2 px-6 py-3 rounded-full bg-brand-600 hover:bg-brand-700 active:bg-brand-800 text-white font-bold text-xs sm:text-sm shadow-button-glow transition-all cursor-pointer shrink-0"
              >
                <PlayCircle className="w-4 h-4 fill-white text-brand-600" />
                <span>Resume Learning</span>
              </button>
            </div>
          </div>
        )}

        {/* Enrolled Programs Section */}
        {dashboard.enrolledPrograms.length > 0 && (
          <div className="space-y-4">
            <h2 className="text-base sm:text-lg font-bold text-slate-900 flex items-center gap-2">
              <GraduationCap className="w-5 h-5 text-brand-600" />
              <span>Career Programs & Tracks</span>
            </h2>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {dashboard.enrolledPrograms.map((prog) => (
                <div
                  key={prog.programId}
                  className="bg-white rounded-2xl p-5 border border-slate-200 shadow-sm flex flex-col justify-between"
                >
                  <div>
                    <div className="flex items-center justify-between text-xs text-slate-500 mb-1">
                      <span className="font-mono text-[10px] bg-slate-100 px-2 py-0.5 rounded">
                        {prog.enrollmentNumber}
                      </span>
                      {prog.cohortCode && (
                        <span className="font-semibold text-brand-600">
                          Cohort {prog.cohortCode}
                        </span>
                      )}
                    </div>
                    <h4 className="text-sm font-bold text-slate-900 mt-1 mb-2">
                      {prog.title}
                    </h4>
                  </div>

                  <div className="mt-4 pt-3 border-t border-slate-100">
                    <div className="flex items-center justify-between text-xs font-semibold mb-1.5">
                      <span className="text-slate-600">Track Progress</span>
                      <span className="text-brand-600">{prog.completionPercentage}%</span>
                    </div>
                    <div className="w-full h-2 bg-slate-100 rounded-full overflow-hidden">
                      <div
                        className="h-full bg-brand-600 rounded-full transition-all duration-500"
                        style={{ width: `${prog.completionPercentage}%` }}
                      />
                    </div>
                    <div className="text-[11px] text-slate-400 mt-2">
                      {prog.completedCoursesCount} of {prog.totalCoursesCount} courses completed
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Enrolled Courses Section */}
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-base sm:text-lg font-bold text-slate-900 flex items-center gap-2">
              <BookOpen className="w-5 h-5 text-brand-600" />
              <span>My Courses ({dashboard.enrolledCourses.length})</span>
            </h2>
            <Link
              to="/courses"
              className="text-xs font-semibold text-brand-600 hover:text-brand-700 flex items-center gap-1"
            >
              <span>Browse Catalog</span>
              <ChevronRight className="w-3.5 h-3.5" />
            </Link>
          </div>

          {!hasEnrollments ? (
            <div className="bg-white rounded-3xl p-12 text-center border border-slate-200/90 shadow-sm">
              <BookOpen className="w-12 h-12 text-slate-300 mx-auto mb-3" />
              <h3 className="text-base font-bold text-slate-800 mb-1">No Active Enrollments Yet</h3>
              <p className="text-xs text-slate-500 max-w-md mx-auto mb-6">
                You haven't enrolled in any courses or career tracks yet. Explore our course catalog to apply and enroll.
              </p>
              <Link
                to="/courses"
                className="inline-flex items-center gap-2 px-6 py-2.5 bg-brand-600 hover:bg-brand-700 text-white font-bold text-xs rounded-full shadow-button-glow transition-all"
              >
                <span>Browse Available Courses</span>
                <ArrowRight className="w-3.5 h-3.5" />
              </Link>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {dashboard.enrolledCourses.map((course) => (
                <div
                  key={course.courseId}
                  className="bg-white rounded-2xl border border-slate-200/90 overflow-hidden shadow-sm hover:shadow-md transition-all flex flex-col justify-between group"
                >
                  <div className="p-5">
                    {/* Top Tags */}
                    <div className="flex items-center justify-between text-[11px] mb-2">
                      <span className="font-semibold px-2.5 py-0.5 rounded-full bg-slate-100 text-slate-600">
                        {course.category}
                      </span>
                      <span className="font-bold text-brand-600">
                        {course.completionPercentage}%
                      </span>
                    </div>

                    <h4 className="text-sm font-bold text-slate-900 group-hover:text-brand-600 transition-colors line-clamp-2 mb-2">
                      {course.title}
                    </h4>

                    {course.instructorName && (
                      <p className="text-xs text-slate-500 mb-4">
                        Instructor: {course.instructorName}
                      </p>
                    )}

                    {/* Progress Bar */}
                    <div className="w-full h-1.5 bg-slate-100 rounded-full overflow-hidden mb-2">
                      <div
                        className="h-full bg-brand-600 rounded-full transition-all duration-500"
                        style={{ width: `${course.completionPercentage}%` }}
                      />
                    </div>
                    <div className="flex items-center justify-between text-[11px] text-slate-500">
                      <span>{course.completedLessonsCount} / {course.totalLessonsCount} lessons</span>
                      <span className="font-medium text-slate-700">{course.level}</span>
                    </div>
                  </div>

                  {/* Card Action Footer */}
                  <div className="p-4 bg-slate-50/70 border-t border-slate-100 flex items-center justify-between">
                    <span className="text-[11px] font-mono text-slate-400">
                      {course.enrollmentNumber}
                    </span>
                    <button
                      onClick={() => {
                        if (onboardingStatus && onboardingStatus.overallStatus !== 'COMPLETED') {
                          navigate('/portal/student/onboarding')
                        } else {
                          navigate(`/learning/courses/${course.courseId}`)
                        }
                      }}
                      className="inline-flex items-center gap-1.5 px-4 py-2 bg-brand-600 hover:bg-brand-700 text-white text-xs font-bold rounded-xl transition-all shadow-sm cursor-pointer"
                    >
                      <span>{course.completionPercentage > 0 ? 'Continue' : 'Start'}</span>
                      <ArrowRight className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
