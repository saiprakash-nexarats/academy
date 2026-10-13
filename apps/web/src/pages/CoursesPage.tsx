import React, { useState, useEffect } from 'react'
import {
  BookOpen,
  Clock,
  ArrowRight,
  Search,
  Filter,
  Star,
  CheckCircle2,
  Layers,
  Code2,
  X,
  Award,
  ChevronDown,
  ChevronUp,
  HelpCircle,
  Laptop,
  GraduationCap,
  AlertCircle,
  RefreshCw,
  Lock,
  PlayCircle,
  Eye,
  Loader2,
} from 'lucide-react'
import { motion, AnimatePresence } from 'framer-motion'
import { Badge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { useModals } from '@/context'
import {
  useCourses,
  useCourseDetail,
  useCourseSyllabus,
  useLessonPreview,
  type CourseSummaryDto,
  type LessonSummaryDto,
} from '@/lib/catalogApi'
import { resolveCourseImage } from '@/lib/programAssets'

export const CoursesPage: React.FC = () => {
  const { openEnrollModal, openDemoModal } = useModals()

  const [selectedCategory, setSelectedCategory] = useState<string>('all')
  const [selectedLevel, setSelectedLevel] = useState<string>('all')
  const [searchQuery, setSearchQuery] = useState<string>('')
  const [activeCourseModal, setActiveCourseModal] = useState<CourseSummaryDto | null>(null)
  const [expandedModule, setExpandedModule] = useState<string | null>(null)
  const [previewLesson, setPreviewLesson] = useState<LessonSummaryDto | null>(null)

  const categories = [
    { id: 'all', label: 'All Courses' },
    { id: 'Web & Full Stack', label: 'Web & Full Stack' },
    { id: 'AI & Machine Learning', label: 'AI & Machine Learning' },
    { id: 'UI/UX Design', label: 'UI/UX Design' },
    { id: 'Cloud & DevOps', label: 'Cloud & DevOps' },
    { id: 'Data & Analytics', label: 'Data & Analytics' },
    { id: 'Marketing', label: 'Marketing' },
    { id: 'Cyber Security', label: 'Cyber Security' },
  ]

  const levels = [
    { id: 'all', label: 'All Levels' },
    { id: 'Beginner', label: 'Beginner' },
    { id: 'Intermediate', label: 'Intermediate' },
    { id: 'Advanced', label: 'Advanced' },
  ]

  // Query live courses catalog via TanStack Query
  const {
    data: coursesData,
    isLoading: isCoursesLoading,
    isError: isCoursesError,
    error: coursesError,
    refetch: refetchCourses,
  } = useCourses({
    category: selectedCategory === 'all' ? undefined : selectedCategory,
    level: selectedLevel === 'all' ? undefined : selectedLevel,
    search: searchQuery.trim() || undefined,
  })

  const courses = coursesData?.items || []

  // Active course queries for dynamic syllabus and detailed descriptions
  const {
    data: activeCourseDetail,
  } = useCourseDetail(activeCourseModal?.slug)

  const {
    data: activeSyllabus,
    isLoading: isSyllabusLoading,
    isError: isSyllabusError,
    refetch: refetchSyllabus,
  } = useCourseSyllabus(activeCourseModal?.slug)

  // Active lesson preview query
  const {
    data: previewData,
    isLoading: isPreviewLoading,
    isError: isPreviewError,
  } = useLessonPreview(activeCourseModal?.slug, previewLesson?.id)

  // Effective expanded module (default to first module if not explicitly toggled)
  const activeExpandedNumber =
    expandedModule !== null
      ? expandedModule
      : (activeSyllabus?.modules?.[0]?.number ?? null)


  // Escape key support for modals
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        if (previewLesson) {
          setPreviewLesson(null)
        } else if (activeCourseModal) {
          setActiveCourseModal(null)
        }
      }
    }
    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [previewLesson, activeCourseModal])

  const courseFaqs = [
    {
      q: 'Do I get lifetime access to course videos and code repositories?',
      a: 'Yes! Once enrolled in any skill course, you retain lifetime access to all recorded sessions, curriculum updates, GitHub starter repositories, and Discord discussion channels.',
    },
    {
      q: 'Are the course certificates accredited and verifiable?',
      a: 'Yes, upon passing module assessments and the capstone project defense, you receive an accredited digital credential with a public URL and verification ID recognized by tech employers.',
    },
    {
      q: 'What is the difference between a Course and a Career Program?',
      a: 'Courses are focused 5-10 week technical deep dives into specific technologies (e.g. Next.js 15 or LangChain). Career Programs are 6-month comprehensive cohorts with dedicated 1:1 mentors and guaranteed placement support.',
    },
    {
      q: 'Can I get help if I get stuck on a coding assignment?',
      a: 'Absolutely! Every course includes daily access to teaching assistant code reviews, private cohort Slack channels, and weekly live instructor Q&A office hours.',
    },
  ]

  const handleOpenSyllabus = (course: CourseSummaryDto) => {
    setActiveCourseModal(course)
    setExpandedModule(null)
    setPreviewLesson(null)
  }

  return (
    <div className="py-10 sm:py-16">
      <div className="academy-container space-y-12 sm:space-y-16">
        {/* 1. Header Hero */}
        <div className="max-w-3xl space-y-4">
          <Badge variant="primary" icon={<BookOpen className="w-3.5 h-3.5 text-brand-600" />}>
            Modular Technical Course Library
          </Badge>
          <h1 className="font-heading font-extrabold text-3xl sm:text-4xl lg:text-5xl text-slate-900 tracking-tight leading-tight">
            Specialized Technical Courses. <br className="hidden sm:inline" />
            <span className="text-brand-600">Built for Modern Engineers.</span>
          </h1>
          <p className="text-sm sm:text-base text-slate-600 leading-relaxed">
            Targeted deep dives with production-grade capstone projects, hands-on code laboratories, verifiable cryptographic certificates, and live mentor office hours.
          </p>
        </div>

        {/* 2. Course Features Value Strip */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 p-5 sm:p-6 rounded-3xl bg-blue-50/70 text-slate-800 shadow-sm border border-blue-200/80">
          <div className="flex items-center gap-3 p-2">
            <div className="p-2.5 rounded-xl bg-white text-brand-600 border border-blue-200/70 shadow-sm shrink-0">
              <Laptop className="w-5 h-5" />
            </div>
            <div>
              <h4 className="font-bold text-xs sm:text-sm text-slate-900">Hands-on Labs</h4>
              <p className="text-[11px] text-slate-600">Interactive code environments</p>
            </div>
          </div>

          <div className="flex items-center gap-3 p-2 border-l border-blue-200/60">
            <div className="p-2.5 rounded-xl bg-white text-brand-600 border border-blue-200/70 shadow-sm shrink-0">
              <Code2 className="w-5 h-5" />
            </div>
            <div>
              <h4 className="font-bold text-xs sm:text-sm text-slate-900">Production Projects</h4>
              <p className="text-[11px] text-slate-600">Real enterprise capstones</p>
            </div>
          </div>

          <div className="flex items-center gap-3 p-2 border-l border-blue-200/60">
            <div className="p-2.5 rounded-xl bg-white text-brand-600 border border-blue-200/70 shadow-sm shrink-0">
              <Award className="w-5 h-5" />
            </div>
            <div>
              <h4 className="font-bold text-xs sm:text-sm text-slate-900">Verifiable Certificate</h4>
              <p className="text-[11px] text-slate-600">Accredited digital credentials</p>
            </div>
          </div>

          <div className="flex items-center gap-3 p-2 border-l border-blue-200/60">
            <div className="p-2.5 rounded-xl bg-white text-brand-600 border border-blue-200/70 shadow-sm shrink-0">
              <GraduationCap className="w-5 h-5" />
            </div>
            <div>
              <h4 className="font-bold text-xs sm:text-sm text-slate-900">Mentor Q&A Support</h4>
              <p className="text-[11px] text-slate-600">Weekly live office hours</p>
            </div>
          </div>
        </div>

        {/* 3. Search & Filter Bar */}
        <div className="space-y-4">
          <div className="flex flex-col md:flex-row items-center justify-between gap-4 p-4 rounded-2xl bg-white border border-slate-200/90 shadow-sm">
            {/* Category Tabs */}
            <div className="flex items-center gap-1.5 overflow-x-auto no-scrollbar w-full md:w-auto pb-1 md:pb-0">
              <Filter className="w-4 h-4 text-slate-400 mr-1 shrink-0 hidden sm:inline" />
              {categories.map((c) => (
                <button
                  key={c.id}
                  type="button"
                  onClick={() => setSelectedCategory(c.id)}
                  className={`px-3.5 py-2 rounded-xl text-xs font-bold transition-all shrink-0 cursor-pointer ${
                    selectedCategory === c.id
                      ? 'bg-brand-600 text-white shadow-sm'
                      : 'bg-slate-50 text-slate-600 hover:bg-slate-100 hover:text-slate-900'
                  }`}
                >
                  {c.label}
                </button>
              ))}
            </div>

            {/* Search Input */}
            <div className="relative w-full md:w-72">
              <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
              <input
                type="text"
                placeholder="Search courses, technologies, instructors..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-9 pr-3.5 py-2 text-xs bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-brand-500"
              />
            </div>
          </div>

          {/* Sub-Filter: Difficulty Level */}
          <div className="flex items-center gap-2 overflow-x-auto no-scrollbar px-1">
            <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider shrink-0 mr-1">
              Level:
            </span>
            {levels.map((lvl) => (
              <button
                key={lvl.id}
                type="button"
                onClick={() => setSelectedLevel(lvl.id)}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all shrink-0 cursor-pointer ${
                  selectedLevel === lvl.id
                    ? 'bg-slate-800 text-white'
                    : 'bg-white border border-slate-200/80 text-slate-600 hover:bg-slate-50'
                }`}
              >
                {lvl.label}
              </button>
            ))}
          </div>
        </div>

        {/* 4. Course Cards Grid / Loading / Error */}
        {isCoursesLoading && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 animate-pulse">
            {[...Array(8)].map((_, idx) => (
              <div
                key={idx}
                className="bg-white rounded-2xl border border-slate-200 overflow-hidden flex flex-col justify-between"
              >
                <div>
                  <div className="aspect-[16/10] bg-slate-200" />
                  <div className="p-5 space-y-3">
                    <div className="h-4 bg-slate-200 rounded w-1/3" />
                    <div className="h-5 bg-slate-200 rounded w-3/4" />
                    <div className="h-4 bg-slate-100 rounded w-1/2" />
                    <div className="space-y-1.5">
                      <div className="h-3 bg-slate-100 rounded w-full" />
                      <div className="h-3 bg-slate-100 rounded w-4/5" />
                    </div>
                  </div>
                </div>
                <div className="p-5 pt-0 space-y-3">
                  <div className="h-8 bg-slate-100 rounded-xl w-full" />
                </div>
              </div>
            ))}
          </div>
        )}

        {isCoursesError && (
          <div className="p-8 text-center bg-white rounded-3xl border border-red-200 space-y-4 max-w-lg mx-auto shadow-sm">
            <div className="w-12 h-12 rounded-2xl bg-red-50 text-red-600 flex items-center justify-center mx-auto">
              <AlertCircle className="w-6 h-6" />
            </div>
            <div className="space-y-1">
              <h3 className="font-heading font-bold text-lg text-slate-900">
                Unable to Load Course Catalog
              </h3>
              <p className="text-xs text-slate-500">
                {(coursesError as Error)?.message || 'A network error occurred while connecting to the core academic catalog service.'}
              </p>
            </div>
            <button
              type="button"
              onClick={() => refetchCourses()}
              className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-brand-600 text-white font-bold text-xs hover:bg-brand-700 cursor-pointer shadow-sm"
            >
              <RefreshCw className="w-3.5 h-3.5" />
              Try Again
            </button>
          </div>
        )}

        {!isCoursesLoading && !isCoursesError && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {courses.map((course) => (
              <motion.div
                key={course.id}
                initial={{ opacity: 0, y: 15 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.3 }}
                className="group bg-white rounded-2xl border border-slate-200/90 shadow-card hover:shadow-card-hover hover:border-brand-300 transition-all flex flex-col justify-between overflow-hidden"
              >
                <div>
                  {/* Course Thumbnail Image */}
                  <div className="relative aspect-[16/10] overflow-hidden bg-slate-100">
                    <img
                      src={resolveCourseImage(course.imageAssetKey)}
                      alt={course.title}
                      className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                    />

                    {/* Badge */}
                    {course.badge && (
                      <div className="absolute top-2.5 left-2.5">
                        <span className="px-2.5 py-0.5 rounded-md text-[10px] font-bold tracking-wider uppercase bg-brand-600 text-white shadow-sm">
                          {course.badge}
                        </span>
                      </div>
                    )}

                    {/* Level Pill */}
                    <div className="absolute bottom-2.5 right-2.5">
                      <span className="px-2 py-0.5 rounded-md text-[10px] font-bold bg-white/95 backdrop-blur-sm text-slate-800 shadow-xs border border-white/60">
                        {course.level}
                      </span>
                    </div>
                  </div>

                  {/* Card Content */}
                  <div className="p-5 space-y-3">
                    {/* Rating & Review Count */}
                    <div className="flex items-center justify-between text-xs">
                      <div className="flex items-center gap-1 text-amber-500 font-bold">
                        <Star className="w-3.5 h-3.5 fill-amber-400 text-amber-400" />
                        <span>{course.rating.toFixed(2)}</span>
                        <span className="text-slate-400 font-normal text-[11px]">
                          ({course.reviewsCount})
                        </span>
                      </div>
                      <span className="text-[11px] text-slate-500 font-medium">
                        {course.enrolledStudents}
                      </span>
                    </div>

                    {/* Course Title */}
                    <h3 className="font-heading font-bold text-base text-slate-900 group-hover:text-brand-600 transition-colors leading-snug line-clamp-2 min-h-[2.5rem]">
                      {course.title}
                    </h3>

                    {/* Instructor Badge */}
                    <div className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-blue-50/60 border border-blue-100/70 text-xs text-brand-700">
                      <span className="font-medium text-[11px]">Taught by</span>
                      <strong className="font-bold text-[11px]">{course.instructor.name}</strong>
                      <span className="text-slate-400 text-[10px]">@{course.instructor.company}</span>
                    </div>

                    {/* Description */}
                    <p className="text-xs text-slate-500 leading-relaxed line-clamp-2">
                      {course.shortDescription}
                    </p>

                    {/* Tech Stack Chips */}
                    <div className="flex flex-wrap gap-1 pt-1">
                      {course.techStack.slice(0, 3).map((tech) => (
                        <span
                          key={tech}
                          className="px-2 py-0.5 rounded-md bg-slate-100 text-slate-600 text-[10px] font-medium"
                        >
                          {tech}
                        </span>
                      ))}
                      {course.techStack.length > 3 && (
                        <span className="px-1.5 py-0.5 rounded-md bg-slate-100 text-slate-400 text-[10px] font-medium">
                          +{course.techStack.length - 3}
                        </span>
                      )}
                    </div>
                  </div>
                </div>

                {/* Card Footer: Meta Info & Actions */}
                <div className="p-5 pt-0 space-y-3">
                  <div className="pt-3 border-t border-slate-100 flex items-center justify-between text-xs text-slate-500">
                    <span className="flex items-center gap-1">
                      <Clock className="w-3.5 h-3.5 text-slate-400" />
                      <span>{course.durationWeeks} Weeks</span>
                    </span>
                    <span className="flex items-center gap-1">
                      <Layers className="w-3.5 h-3.5 text-slate-400" />
                      <span>{course.lessonsCount} Lessons</span>
                    </span>
                    <span className="flex items-center gap-1">
                      <Code2 className="w-3.5 h-3.5 text-slate-400" />
                      <span>{course.projectsCount} Projects</span>
                    </span>
                  </div>

                  <div className="grid grid-cols-2 gap-2">
                    <button
                      type="button"
                      onClick={() => handleOpenSyllabus(course)}
                      className="w-full py-2 px-3 rounded-xl border border-slate-300 hover:border-brand-500 hover:bg-slate-50 text-slate-700 hover:text-brand-600 font-bold text-xs transition-all cursor-pointer text-center"
                    >
                      View Syllabus
                    </button>
                    <Button
                      variant="primary"
                      size="sm"
                      onClick={() => openEnrollModal(course.id)}
                      className="w-full justify-center bg-brand-600 hover:bg-brand-700 font-bold text-xs rounded-xl shadow-button-glow cursor-pointer"
                    >
                      Enroll Now
                    </Button>
                  </div>
                </div>
              </motion.div>
            ))}
          </div>
        )}

        {/* Empty State */}
        {!isCoursesLoading && !isCoursesError && courses.length === 0 && (
          <div className="p-12 text-center bg-white rounded-3xl border border-slate-200/90 space-y-3">
            <Search className="w-8 h-8 text-slate-400 mx-auto" />
            <h3 className="font-heading font-bold text-lg text-slate-900">
              No courses found matching your criteria
            </h3>
            <p className="text-xs text-slate-500 max-w-sm mx-auto">
              Try adjusting your category tabs or search query to explore other subjects.
            </p>
            <button
              type="button"
              onClick={() => {
                setSelectedCategory('all')
                setSelectedLevel('all')
                setSearchQuery('')
              }}
              className="px-4 py-2 rounded-xl bg-brand-50 text-brand-600 font-bold text-xs hover:bg-brand-100 cursor-pointer"
            >
              Reset Filters
            </button>
          </div>
        )}

        {/* 5. Course FAQs */}
        <div className="space-y-6 pt-6">
          <div className="flex items-center gap-2">
            <HelpCircle className="w-5 h-5 text-brand-600" />
            <h2 className="text-2xl sm:text-3xl font-heading font-extrabold text-slate-900">
              Frequently Asked Questions About Courses
            </h2>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            {courseFaqs.map((faq, i) => (
              <div
                key={i}
                className="p-5 rounded-2xl bg-white border border-slate-200/90 shadow-sm space-y-2"
              >
                <h4 className="font-bold text-sm text-slate-900">{faq.q}</h4>
                <p className="text-xs sm:text-sm text-slate-600 leading-relaxed">{faq.a}</p>
              </div>
            ))}
          </div>
        </div>

        {/* 6. Bottom Banner */}
        <div className="p-8 sm:p-12 rounded-3xl bg-brand-50 border border-brand-200/80 flex flex-col sm:flex-row items-center justify-between gap-6 text-center sm:text-left">
          <div className="space-y-1 max-w-xl">
            <h2 className="text-xl sm:text-2xl font-heading font-extrabold text-slate-900">
              Looking for full career placement cohorts instead?
            </h2>
            <p className="text-xs sm:text-sm text-slate-600">
              Check out our 6-month intensive Career Programs with dedicated 1-on-1 industry mentors and 95% placement support.
            </p>
          </div>
          <div className="flex flex-wrap items-center gap-3 shrink-0">
            <Button
              variant="primary"
              size="lg"
              onClick={() => openEnrollModal()}
              iconRight={<ArrowRight className="w-4 h-4" />}
              className="bg-brand-600 hover:bg-brand-700 font-bold rounded-xl shadow-button-glow"
            >
              Explore Career Programs
            </Button>
            <button
              type="button"
              onClick={() => openDemoModal()}
              className="px-6 py-3.5 rounded-xl border border-slate-300 bg-white hover:bg-slate-50 font-bold text-sm text-slate-700 transition-colors cursor-pointer"
            >
              Book a Demo
            </button>
          </div>
        </div>
      </div>

      {/* 7. Interactive Course Syllabus Modal */}
      <AnimatePresence>
        {activeCourseModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-slate-900/60 backdrop-blur-sm">
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              className="bg-white rounded-3xl max-w-2xl w-full p-6 sm:p-8 shadow-2xl border border-slate-200 relative overflow-hidden max-h-[90vh] overflow-y-auto"
            >
              <button
                type="button"
                onClick={() => {
                  setActiveCourseModal(null)
                  setPreviewLesson(null)
                }}
                className="absolute top-5 right-5 p-2 rounded-full hover:bg-slate-100 text-slate-400 hover:text-slate-700 cursor-pointer z-10"
              >
                <X className="w-5 h-5" />
              </button>

              <div className="space-y-6">
                {/* Header */}
                <div className="pb-5 border-b border-slate-100 pr-8">
                  <div className="flex items-center gap-2 mb-2">
                    <span className="px-2.5 py-0.5 rounded-md bg-brand-50 text-brand-700 font-bold text-xs">
                      {activeCourseModal.category}
                    </span>
                    <span className="px-2.5 py-0.5 rounded-md bg-slate-100 text-slate-700 font-bold text-xs">
                      {activeCourseModal.level}
                    </span>
                    {activeSyllabus?.versionLabel && (
                      <span className="px-2 py-0.5 rounded-md bg-emerald-50 text-emerald-700 font-semibold text-[10px]">
                        {activeSyllabus.versionLabel} (Published)
                      </span>
                    )}
                  </div>
                  <h3 className="font-heading font-black text-xl sm:text-2xl text-slate-900 leading-snug">
                    {activeCourseModal.title}
                  </h3>
                  <p className="text-xs sm:text-sm text-slate-600 mt-2 leading-relaxed">
                    {activeCourseDetail?.detailedDescription || activeCourseModal.shortDescription}
                  </p>
                </div>

                {/* Course Meta Grid */}
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 p-4 rounded-2xl bg-slate-50 border border-slate-100 text-center">
                  <div>
                    <span className="text-[10px] text-slate-400 uppercase font-bold block">Duration</span>
                    <span className="font-bold text-sm text-slate-900">{activeCourseModal.durationWeeks} Weeks</span>
                  </div>
                  <div>
                    <span className="text-[10px] text-slate-400 uppercase font-bold block">Curriculum</span>
                    <span className="font-bold text-sm text-slate-900">
                      {activeSyllabus?.totalModules ?? activeCourseModal.lessonsCount} Modules
                    </span>
                  </div>
                  <div>
                    <span className="text-[10px] text-slate-400 uppercase font-bold block">Projects</span>
                    <span className="font-bold text-sm text-slate-900">{activeCourseModal.projectsCount} Capstones</span>
                  </div>
                  <div>
                    <span className="text-[10px] text-slate-400 uppercase font-bold block">Certificate</span>
                    <span className="font-bold text-sm text-emerald-700">Verifiable</span>
                  </div>
                </div>

                {/* Learning Outcomes */}
                {activeCourseDetail?.learningOutcomes && activeCourseDetail.learningOutcomes.length > 0 && (
                  <div className="space-y-2">
                    <h4 className="font-heading font-bold text-sm text-slate-900 uppercase tracking-wider text-brand-600">
                      What You Will Master & Build
                    </h4>
                    <div className="space-y-2">
                      {activeCourseDetail.learningOutcomes.map((outcome, i) => (
                        <div key={i} className="flex items-start gap-2.5 text-xs text-slate-700">
                          <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0 mt-0.5" />
                          <span>{outcome}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* Modules & Syllabus Accordion */}
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <h4 className="font-heading font-bold text-sm text-slate-900 uppercase tracking-wider text-brand-600">
                      Curriculum Modules & Lessons Breakdown
                    </h4>
                    {activeSyllabus?.totalLessons && (
                      <span className="text-[11px] font-medium text-slate-500">
                        {activeSyllabus.totalLessons} structured lessons
                      </span>
                    )}
                  </div>

                  {isSyllabusLoading && (
                    <div className="p-8 text-center bg-slate-50 rounded-2xl flex flex-col items-center justify-center gap-2">
                      <Loader2 className="w-6 h-6 animate-spin text-brand-600" />
                      <span className="text-xs text-slate-500 font-medium">
                        Loading published curriculum syllabus...
                      </span>
                    </div>
                  )}

                  {isSyllabusError && (
                    <div className="p-4 rounded-2xl bg-amber-50 border border-amber-200 text-xs text-amber-800 flex items-center justify-between">
                      <span>Unable to load curriculum version details.</span>
                      <button
                        type="button"
                        onClick={() => refetchSyllabus()}
                        className="font-bold underline cursor-pointer"
                      >
                        Retry
                      </button>
                    </div>
                  )}

                  {!isSyllabusLoading && !isSyllabusError && activeSyllabus?.modules && (
                    <div className="space-y-2">
                      {activeSyllabus.modules.map((mod) => {
                        const isExpanded = activeExpandedNumber === mod.number
                        return (
                          <div
                            key={mod.id || mod.number}
                            className="rounded-2xl border border-slate-200/90 overflow-hidden bg-white"
                          >
                            <button
                              type="button"
                              onClick={() => setExpandedModule(isExpanded ? '' : mod.number)}
                              className="w-full p-4 text-left flex items-center justify-between gap-4 hover:bg-slate-50 transition-colors cursor-pointer"
                            >
                              <div className="flex items-center gap-3">
                                <span className="w-7 h-7 rounded-lg bg-blue-50 text-brand-700 font-black text-xs flex items-center justify-center shrink-0">
                                  {mod.number}
                                </span>
                                <div>
                                  <h5 className="font-bold text-xs sm:text-sm text-slate-900">{mod.title}</h5>
                                  <span className="text-[10px] text-slate-400">{mod.duration}</span>
                                </div>
                              </div>
                              {isExpanded ? (
                                <ChevronUp className="w-4 h-4 text-slate-400 shrink-0" />
                              ) : (
                                <ChevronDown className="w-4 h-4 text-slate-400 shrink-0" />
                              )}
                            </button>

                            {isExpanded && (
                              <div className="p-4 pt-0 border-t border-slate-100 bg-slate-50/50 space-y-3">
                                {/* Topics Covered Chips */}
                                {mod.topics && mod.topics.length > 0 && (
                                  <div>
                                    <div className="text-[10px] font-bold uppercase text-slate-400 mb-1.5 mt-2">
                                      Key Topics:
                                    </div>
                                    <ul className="grid grid-cols-1 sm:grid-cols-2 gap-1.5 text-xs text-slate-700">
                                      {mod.topics.map((t, idx) => (
                                        <li key={idx} className="flex items-center gap-2">
                                          <span className="w-1.5 h-1.5 rounded-full bg-brand-600 shrink-0" />
                                          <span className="text-[11px]">{t}</span>
                                        </li>
                                      ))}
                                    </ul>
                                  </div>
                                )}

                                {/* Ordered Lessons within this Module */}
                                {mod.lessons && mod.lessons.length > 0 && (
                                  <div>
                                    <div className="text-[10px] font-bold uppercase text-slate-400 mb-1.5 mt-2">
                                      Lessons & Laboratories:
                                    </div>
                                    <div className="space-y-1.5">
                                      {mod.lessons.map((lesson) => (
                                        <div
                                          key={lesson.id}
                                          className="p-2.5 rounded-xl bg-white border border-slate-200/80 flex items-center justify-between gap-3 text-xs"
                                        >
                                          <div className="flex items-center gap-2.5 min-w-0">
                                            {lesson.isPreviewable ? (
                                              <PlayCircle className="w-4 h-4 text-brand-600 shrink-0" />
                                            ) : (
                                              <Lock className="w-4 h-4 text-slate-400 shrink-0" />
                                            )}
                                            <span className="font-medium text-slate-800 truncate text-[11px]">
                                              {lesson.title}
                                            </span>
                                          </div>

                                          <div className="flex items-center gap-2 shrink-0">
                                            <span className="text-[10px] text-slate-400">
                                              {lesson.durationMinutes}m
                                            </span>
                                            {lesson.isPreviewable ? (
                                              <button
                                                type="button"
                                                onClick={() => setPreviewLesson(lesson)}
                                                className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md bg-blue-50 text-brand-700 hover:bg-blue-100 font-bold text-[10px] cursor-pointer"
                                              >
                                                <Eye className="w-3 h-3" />
                                                Preview
                                              </button>
                                            ) : (
                                              <span className="inline-flex items-center gap-1 px-1.5 py-0.5 rounded-md bg-slate-100 text-slate-500 font-medium text-[10px]">
                                                Enrolled
                                              </span>
                                            )}
                                          </div>
                                        </div>
                                      ))}
                                    </div>
                                  </div>
                                )}
                              </div>
                            )}
                          </div>
                        )
                      })}
                    </div>
                  )}
                </div>

                {/* Prerequisites */}
                <div className="p-4 rounded-xl bg-slate-50 border border-slate-100 text-xs">
                  <span className="font-bold text-slate-900 block mb-1">Prerequisites:</span>
                  <span className="text-slate-600">
                    {activeCourseDetail?.prerequisites ||
                      'Basic programming understanding and enthusiasm for enterprise software engineering.'}
                  </span>
                </div>

                {/* Modal Footer CTA */}
                <div className="pt-4 border-t border-slate-100 flex flex-col sm:flex-row items-center gap-3">
                  <Button
                    variant="primary"
                    size="md"
                    onClick={() => {
                      const id = activeCourseModal.id
                      setActiveCourseModal(null)
                      openEnrollModal(id)
                    }}
                    className="w-full sm:flex-1 justify-center bg-brand-600 hover:bg-brand-700 font-bold rounded-xl py-3 shadow-button-glow cursor-pointer"
                  >
                    Enroll in This Course
                  </Button>
                  <button
                    type="button"
                    onClick={() => {
                      setActiveCourseModal(null)
                      setPreviewLesson(null)
                    }}
                    className="w-full sm:w-auto px-6 py-3 rounded-xl border border-slate-300 hover:bg-slate-50 text-slate-700 font-bold text-xs cursor-pointer"
                  >
                    Close
                  </button>
                </div>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* 8. Authorized Public Lesson Preview Sub-Modal */}
      <AnimatePresence>
        {previewLesson && (
          <div className="fixed inset-0 z-60 flex items-center justify-center p-4 bg-slate-950/70 backdrop-blur-sm">
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              className="bg-white rounded-3xl max-w-lg w-full p-6 sm:p-7 shadow-2xl border border-slate-200 relative overflow-hidden"
            >
              <button
                type="button"
                onClick={() => setPreviewLesson(null)}
                className="absolute top-4 right-4 p-2 rounded-full hover:bg-slate-100 text-slate-400 hover:text-slate-700 cursor-pointer"
              >
                <X className="w-5 h-5" />
              </button>

              <div className="space-y-4">
                <div className="flex items-center gap-2">
                  <span className="px-2 py-0.5 rounded-md bg-emerald-50 text-emerald-700 font-bold text-[10px] uppercase">
                    Free Lesson Preview
                  </span>
                  <span className="px-2 py-0.5 rounded-md bg-slate-100 text-slate-600 font-medium text-[10px]">
                    {previewLesson.lessonType}
                  </span>
                  <span className="text-[11px] text-slate-400">
                    {previewLesson.durationMinutes} mins
                  </span>
                </div>

                <div>
                  <h4 className="font-heading font-extrabold text-lg text-slate-900 leading-snug">
                    {previewLesson.title}
                  </h4>
                  {previewLesson.summary && (
                    <p className="text-xs text-slate-600 mt-1 leading-relaxed">
                      {previewLesson.summary}
                    </p>
                  )}
                </div>

                <div className="p-4 rounded-2xl bg-blue-50/50 border border-blue-100 text-slate-800 space-y-2">
                  <span className="text-[10px] font-bold text-brand-700 uppercase tracking-wider block">
                    Preview Content & Starter Materials
                  </span>
                  {isPreviewLoading && (
                    <div className="py-6 flex items-center justify-center gap-2 text-xs text-slate-500">
                      <Loader2 className="w-4 h-4 animate-spin text-brand-600" />
                      Loading sample lesson content...
                    </div>
                  )}
                  {isPreviewError && (
                    <p className="text-xs text-amber-700">
                      Lesson preview content could not be loaded at this time.
                    </p>
                  )}
                  {!isPreviewLoading && !isPreviewError && (
                    <div className="text-xs text-slate-700 leading-relaxed max-h-48 overflow-y-auto pr-1">
                      {previewData?.previewContent ||
                        'Sample starter codebase, architectural overview diagrams, and module guidelines are made available to learners before enrollment.'}
                    </div>
                  )}
                </div>

                <div className="p-3 rounded-xl bg-slate-50 border border-slate-200/80 text-[11px] text-slate-500 flex items-start gap-2">
                  <Lock className="w-4 h-4 text-slate-400 shrink-0 mt-0.5" />
                  <span>
                    Subsequent deep-dive laboratories and graded project defenses in this module are reserved for enrolled students.
                  </span>
                </div>

                <div className="pt-2 flex items-center gap-3">
                  <Button
                    variant="primary"
                    size="sm"
                    onClick={() => {
                      const courseId = activeCourseModal?.id
                      setPreviewLesson(null)
                      setActiveCourseModal(null)
                      if (courseId) openEnrollModal(courseId)
                    }}
                    className="flex-1 justify-center bg-brand-600 hover:bg-brand-700 font-bold text-xs rounded-xl"
                  >
                    Enroll to Unlock All Lessons
                  </Button>
                  <button
                    type="button"
                    onClick={() => setPreviewLesson(null)}
                    className="px-4 py-2 rounded-xl border border-slate-300 hover:bg-slate-50 font-bold text-xs text-slate-700 cursor-pointer"
                  >
                    Back to Syllabus
                  </button>
                </div>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  )
}
