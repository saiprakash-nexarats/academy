import React, { useState } from 'react'
import { Search, Sparkles, Filter, Clock, AlertCircle, RefreshCw } from 'lucide-react'
import { ProgramCard } from '@/sections/programs/ProgramCard'
import { ProgramCardSkeleton } from '@/sections/programs/ProgramCardSkeleton'
import { usePrograms, useCategories } from '@/lib/catalogApi'
import { resolveProgramImage } from '@/lib/programAssets'
import { Badge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'

export const ProgramsPage: React.FC = () => {
  const [activeCategory, setActiveCategory] = useState<string>('all')
  const [activeDuration, setActiveDuration] = useState<string>('all')
  const [searchQuery, setSearchQuery] = useState<string>('')

  // 1. Fetch dynamic categories from the backend
  const { data: remoteCategories } = useCategories()

  const categories = [
    { id: 'all', label: 'All Programs' },
    ...(remoteCategories?.map((c) => ({
      id: c.slug,
      label: c.name,
    })) || [
      { id: 'software-engineering', label: 'Software Engineering' },
      { id: 'artificial-intelligence', label: 'AI & Data Science' },
      { id: 'design', label: 'UI/UX Design' },
      { id: 'marketing', label: 'Digital Marketing' },
    ]),
  ]

  const durationFilters = [
    { id: 'all', label: 'All Durations' },
    { id: '4', label: '4 Months' },
    { id: '5', label: '5 Months' },
    { id: '6', label: '6 Months' },
  ]

  // 2. Fetch published programs dynamically via TanStack Query
  const durationParam = activeDuration !== 'all' ? parseInt(activeDuration, 10) : undefined
  const categoryParam = activeCategory !== 'all' ? activeCategory : undefined
  const searchParam = searchQuery.trim() ? searchQuery.trim() : undefined

  const {
    data: programsData,
    isLoading,
    isError,
    error,
    refetch,
  } = usePrograms({
    category: categoryParam,
    duration: durationParam,
    search: searchParam,
  })

  const programs = programsData?.items || []

  const handleResetFilters = () => {
    setActiveCategory('all')
    setActiveDuration('all')
    setSearchQuery('')
  }

  return (
    <div className="py-12 sm:py-16">
      <div className="academy-container space-y-10">
        {/* Page Header */}
        <div className="max-w-3xl space-y-4">
          <Badge variant="primary" icon={<Sparkles className="w-3.5 h-3.5 text-brand-600" />}>
            Career-Aligned Curriculum
          </Badge>
          <h1 className="font-heading font-extrabold text-3xl sm:text-4xl lg:text-5xl text-slate-900 tracking-tight">
            Industry-Approved <span className="text-brand-600">Career Programs</span>
          </h1>
          <p className="text-sm sm:text-base text-slate-600 leading-relaxed">
            Gain mastery in high-demand technologies through rigorous live cohorts, real-world portfolio projects, and personalized mentor evaluations.
          </p>
        </div>

        {/* Filter Controls & Search */}
        <div className="space-y-3 p-4 rounded-2xl bg-white border border-slate-200/90 shadow-sm">
          <div className="flex flex-col md:flex-row items-center justify-between gap-4">
            {/* Category Tabs */}
            <div className="flex items-center gap-1.5 overflow-x-auto no-scrollbar w-full md:w-auto pb-1 md:pb-0">
              <Filter className="w-4 h-4 text-slate-400 mr-1 shrink-0 hidden sm:inline" />
              {categories.map((c) => (
                <button
                  key={c.id}
                  type="button"
                  onClick={() => setActiveCategory(c.id)}
                  className={`px-3.5 py-2 rounded-xl text-xs font-bold transition-all shrink-0 cursor-pointer ${
                    activeCategory === c.id
                      ? 'bg-brand-600 text-white shadow-sm'
                      : 'bg-slate-50 text-slate-600 hover:bg-slate-100 hover:text-slate-900'
                  }`}
                >
                  {c.label}
                </button>
              ))}
            </div>

            {/* Search Bar */}
            <div className="relative w-full md:w-72">
              <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
              <input
                type="text"
                placeholder="Search programs..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-9 pr-3.5 py-2 text-xs bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-brand-500"
              />
            </div>
          </div>

          {/* Secondary Duration Filter Bar */}
          <div className="flex items-center gap-2 pt-2 border-t border-slate-100 overflow-x-auto no-scrollbar text-xs">
            <span className="text-slate-400 flex items-center gap-1 shrink-0 font-medium mr-1">
              <Clock className="w-3.5 h-3.5" />
              <span>Duration:</span>
            </span>
            {durationFilters.map((d) => (
              <button
                key={d.id}
                type="button"
                onClick={() => setActiveDuration(d.id)}
                className={`px-2.5 py-1 rounded-lg text-xs font-semibold transition-colors shrink-0 cursor-pointer ${
                  activeDuration === d.id
                    ? 'bg-slate-900 text-white'
                    : 'bg-slate-100/70 text-slate-600 hover:bg-slate-200/80 hover:text-slate-900'
                }`}
              >
                {d.label}
              </button>
            ))}
          </div>
        </div>

        {/* Loading State: Skeletons */}
        {isLoading && (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
            {[0, 1, 2, 3, 4, 5, 6, 7].map((idx) => (
              <ProgramCardSkeleton key={idx} />
            ))}
          </div>
        )}

        {/* Error State: Alert with Retry */}
        {isError && (
          <div className="p-8 rounded-3xl bg-white border border-slate-200/90 shadow-card text-center max-w-lg mx-auto space-y-4">
            <div className="w-12 h-12 rounded-2xl bg-rose-50 text-rose-600 border border-rose-200 flex items-center justify-center mx-auto">
              <AlertCircle className="w-6 h-6" />
            </div>
            <div className="space-y-1">
              <h3 className="font-heading font-bold text-lg text-slate-900">
                Failed to Load Catalog Programs
              </h3>
              <p className="text-xs text-slate-500">
                {error instanceof Error ? error.message : 'A network or server error occurred.'}
              </p>
            </div>
            <Button
              variant="primary"
              onClick={() => refetch()}
              iconLeft={<RefreshCw className="w-3.5 h-3.5" />}
              className="text-xs font-bold"
            >
              Retry Connection
            </Button>
          </div>
        )}

        {/* Empty State */}
        {!isLoading && !isError && programs.length === 0 && (
          <div className="p-10 rounded-3xl bg-white border border-slate-200/90 shadow-card text-center max-w-md mx-auto space-y-4">
            <div className="w-12 h-12 rounded-2xl bg-slate-50 text-slate-400 border border-slate-200 flex items-center justify-center mx-auto">
              <Filter className="w-6 h-6" />
            </div>
            <div className="space-y-1">
              <h3 className="font-heading font-bold text-base text-slate-900">
                No Programs Found
              </h3>
              <p className="text-xs text-slate-500">
                No programs match your current filter criteria or search query.
              </p>
            </div>
            <Button
              variant="secondary"
              onClick={handleResetFilters}
              className="text-xs font-bold"
            >
              Reset All Filters
            </Button>
          </div>
        )}

        {/* Programs Grid */}
        {!isLoading && !isError && programs.length > 0 && (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
            {programs.map((program) => (
              <ProgramCard
                key={program.id}
                id={program.id}
                slug={program.slug}
                title={program.title}
                description={program.shortDescription}
                image={resolveProgramImage(program.imageAssetKey)}
                duration={program.durationLabel}
                level={program.level}
                badge={program.badge ?? undefined}
                category={program.categoryName}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
