import React from 'react'
import { Link, useNavigate } from 'react-router-dom'
import {
  ArrowLeft,
  Clock,
  BarChart,
  Star,
  CheckCircle2,
  BookOpen,
  Briefcase,
  GraduationCap,
  AlertCircle,
  RefreshCw,
} from 'lucide-react'
import { useProgramDetail } from '@/lib/catalogApi'
import { resolveProgramImage } from '@/lib/programAssets'
import { useModals } from '@/context'
import { Button } from '@/components/ui/Button'

interface ProgramDetailViewProps {
  slug: string
}

export const ProgramDetailView: React.FC<ProgramDetailViewProps> = ({ slug }) => {
  const navigate = useNavigate()
  const { openEnrollModal } = useModals()
  const { data: program, isLoading, isError, refetch } = useProgramDetail(slug)

  if (isLoading) {
    return (
      <div className="py-12 sm:py-16">
        <div className="academy-container space-y-8 animate-pulse">
          {/* Back button skeleton */}
          <div className="h-5 w-32 bg-slate-200 rounded-md" />

          {/* Hero header skeleton */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            <div className="lg:col-span-2 space-y-4">
              <div className="h-6 w-24 bg-slate-200 rounded-full" />
              <div className="h-10 w-3/4 bg-slate-300 rounded-xl" />
              <div className="h-4 w-full bg-slate-200 rounded-md" />
              <div className="h-4 w-2/3 bg-slate-200 rounded-md" />
              <div className="h-10 w-48 bg-slate-200 rounded-xl" />
            </div>
            <div className="h-64 bg-slate-200 rounded-3xl" />
          </div>
        </div>
      </div>
    )
  }

  if (isError || !program) {
    return (
      <div className="py-16 sm:py-24">
        <div className="academy-container max-w-xl text-center space-y-6">
          <div className="w-16 h-16 rounded-2xl bg-amber-50 text-amber-600 border border-amber-200 flex items-center justify-center mx-auto shadow-sm">
            <AlertCircle className="w-8 h-8" />
          </div>
          <div className="space-y-2">
            <h1 className="font-heading font-extrabold text-2xl sm:text-3xl text-slate-900">
              Program Not Found
            </h1>
            <p className="text-sm text-slate-600 leading-relaxed">
              The program <span className="font-semibold text-slate-800">"{slug}"</span> could not be found or has not yet been published to the catalog.
            </p>
          </div>
          <div className="flex items-center justify-center gap-3 pt-2">
            <Button variant="secondary" onClick={() => navigate('/programs')} iconLeft={<ArrowLeft className="w-4 h-4" />}>
              Back to Catalog
            </Button>
            <Button variant="outline" onClick={() => refetch()} iconLeft={<RefreshCw className="w-4 h-4" />}>
              Retry
            </Button>
          </div>
        </div>
      </div>
    )
  }

  const curriculumModules = program.curriculumOverview
    ? program.curriculumOverview.split(',').map((item) => item.trim())
    : []

  const careerRoles = program.careerOutcomes
    ? program.careerOutcomes.split(',').map((item) => item.trim())
    : []

  return (
    <div className="py-10 sm:py-14">
      <div className="academy-container space-y-8 sm:space-y-12">
        {/* Navigation Breadcrumb / Back Link */}
        <div className="flex items-center justify-between text-xs sm:text-sm">
          <Link
            to="/programs"
            className="inline-flex items-center gap-2 font-semibold text-slate-600 hover:text-brand-600 transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>All Programs</span>
          </Link>

          <span className="text-slate-400 hidden sm:inline">
            Catalog / {program.categoryName} / <span className="text-slate-700 font-medium">{program.title}</span>
          </span>
        </div>

        {/* Hero Section */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-10 items-start">
          {/* Left Column: Details */}
          <div className="lg:col-span-7 xl:col-span-8 space-y-6">
            <div className="flex flex-wrap items-center gap-2.5">
              <span className="px-3 py-1 rounded-full text-xs font-bold bg-brand-50 text-brand-600 border border-brand-200">
                {program.categoryName}
              </span>

              {program.badge && (
                <span className="px-2.5 py-1 rounded-full text-xs font-bold uppercase tracking-wider bg-amber-50 text-amber-700 border border-amber-200">
                  {program.badge}
                </span>
              )}

              <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold bg-emerald-50 text-emerald-700 border border-emerald-200">
                <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" />
                <span>Job Assured</span>
              </span>
            </div>

            <h1 className="font-heading font-extrabold text-3xl sm:text-4xl lg:text-5xl text-slate-900 tracking-tight leading-tight">
              {program.title}
            </h1>

            <p className="text-base sm:text-lg text-slate-600 leading-relaxed">
              {program.shortDescription}
            </p>

            {/* Meta Pill Badges */}
            <div className="flex flex-wrap items-center gap-4 pt-2 text-xs sm:text-sm text-slate-600">
              <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-50 border border-slate-200/90 font-medium">
                <Clock className="w-4 h-4 text-brand-600" />
                <span>Duration: <strong>{program.durationLabel}</strong></span>
              </div>

              <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-50 border border-slate-200/90 font-medium">
                <BarChart className="w-4 h-4 text-brand-600" />
                <span>Level: <strong>{program.level}</strong></span>
              </div>

              <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-50 border border-slate-200/90 font-medium">
                <Star className="w-4 h-4 text-amber-500 fill-amber-500" />
                <span>Rating: <strong>{program.rating.toFixed(1)}/5.0</strong></span>
              </div>
            </div>

            {/* Detailed Description */}
            {program.detailedDescription && (
              <div className="pt-6 border-t border-slate-100 space-y-3">
                <h2 className="font-heading font-bold text-lg sm:text-xl text-slate-900">
                  About this Program
                </h2>
                <p className="text-sm sm:text-base text-slate-600 leading-relaxed whitespace-pre-line">
                  {program.detailedDescription}
                </p>
              </div>
            )}

            {/* Curriculum Overview */}
            {curriculumModules.length > 0 && (
              <div className="pt-6 border-t border-slate-100 space-y-4">
                <div className="flex items-center gap-2">
                  <BookOpen className="w-5 h-5 text-brand-600" />
                  <h2 className="font-heading font-bold text-lg sm:text-xl text-slate-900">
                    Core Curriculum Modules
                  </h2>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  {curriculumModules.map((module, idx) => (
                    <div
                      key={idx}
                      className="p-3.5 rounded-2xl bg-white border border-slate-200/90 shadow-xs flex items-start gap-3"
                    >
                      <div className="w-6 h-6 rounded-lg bg-brand-50 text-brand-600 text-xs font-bold flex items-center justify-center shrink-0 mt-0.5">
                        {idx + 1}
                      </div>
                      <span className="text-xs sm:text-sm font-semibold text-slate-800 leading-snug">
                        {module}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Career Outcomes */}
            {careerRoles.length > 0 && (
              <div className="pt-6 border-t border-slate-100 space-y-4">
                <div className="flex items-center gap-2">
                  <Briefcase className="w-5 h-5 text-brand-600" />
                  <h2 className="font-heading font-bold text-lg sm:text-xl text-slate-900">
                    Target Career Pathways
                  </h2>
                </div>

                <div className="flex flex-wrap gap-2">
                  {careerRoles.map((role, idx) => (
                    <span
                      key={idx}
                      className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-50 border border-slate-200 text-xs sm:text-sm font-medium text-slate-700"
                    >
                      <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
                      <span>{role}</span>
                    </span>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* Right Column: Sticky Enrollment CTA Card */}
          <div className="lg:col-span-5 xl:col-span-4 sticky top-24">
            <div className="bg-white rounded-3xl border border-slate-200/90 shadow-card p-6 space-y-6">
              <div className="relative aspect-[16/10] rounded-2xl overflow-hidden bg-slate-100">
                <img
                  src={resolveProgramImage(program.imageAssetKey)}
                  alt={program.title}
                  className="w-full h-full object-cover"
                />
              </div>

              <div className="space-y-3">
                <h3 className="font-heading font-extrabold text-xl text-slate-900">
                  Ready to Accelerate Your Career?
                </h3>
                <p className="text-xs text-slate-500 leading-relaxed">
                  Join our next cohort. Learn directly from industry senior mentors, complete real-world portfolio capstones, and access dedicated career placement assistance.
                </p>
              </div>

              <div className="space-y-2.5 pt-2 border-t border-slate-100 text-xs text-slate-600">
                <div className="flex items-center gap-2">
                  <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
                  <span>Live interactive mentor-led cohorts</span>
                </div>
                <div className="flex items-center gap-2">
                  <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
                  <span>1-on-1 personalized code reviews</span>
                </div>
                <div className="flex items-center gap-2">
                  <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
                  <span>Comprehensive capstone portfolio</span>
                </div>
                <div className="flex items-center gap-2">
                  <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
                  <span>Dedicated career counseling & referrals</span>
                </div>
              </div>

              <Button
                variant="primary"
                onClick={() => openEnrollModal(program.slug)}
                className="w-full py-3.5 font-bold shadow-button-glow"
                iconRight={<GraduationCap className="w-4 h-4" />}
              >
                Apply for this Cohort
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
