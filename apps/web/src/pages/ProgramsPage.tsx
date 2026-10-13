import React, { useState } from 'react'
import { useParams } from 'react-router-dom'
import { Search, Sparkles, Filter } from 'lucide-react'
import { ProgramCard } from '@/sections/programs/ProgramCard'
import { ALL_PROGRAMS } from '@/data'
import { Badge } from '@/components/ui/Badge'

interface ProgramsPageProps {
  onOpenEnroll?: () => void
}

const getCategoryFromSlug = (slug?: string): string => {
  if (slug === 'full-stack') return 'Software Engineering'
  if (slug === 'data-science') return 'Artificial Intelligence'
  if (slug === 'ui-ux') return 'Design'
  if (slug === 'digital-marketing') return 'Marketing'
  return 'all'
}

export const ProgramsPage: React.FC<ProgramsPageProps> = () => {
  const { programId } = useParams<{ programId?: string }>()
  const defaultCategory = getCategoryFromSlug(programId)
  const [userSelectedCategory, setUserSelectedCategory] = useState<string | null>(null)
  const [searchQuery, setSearchQuery] = useState<string>('')

  const activeCategory = userSelectedCategory ?? defaultCategory

  const categories = [
    { id: 'all', label: 'All Programs' },
    { id: 'Software Engineering', label: 'Software Engineering' },
    { id: 'Artificial Intelligence', label: 'AI & Data Science' },
    { id: 'Design', label: 'UI/UX Design' },
    { id: 'Marketing', label: 'Digital Marketing' },
  ]

  const filteredPrograms = ALL_PROGRAMS.filter((p) => {
    const matchesCategory =
      activeCategory === 'all' ||
      p.category === activeCategory ||
      p.title.toLowerCase().includes(activeCategory.toLowerCase())
    const matchesSearch =
      p.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      p.description.toLowerCase().includes(searchQuery.toLowerCase())
    return matchesCategory && matchesSearch
  })

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
        <div className="flex flex-col md:flex-row items-center justify-between gap-4 p-4 rounded-2xl bg-white border border-slate-200/90 shadow-sm">
          {/* Category Tabs */}
          <div className="flex items-center gap-1.5 overflow-x-auto no-scrollbar w-full md:w-auto pb-1 md:pb-0">
            <Filter className="w-4 h-4 text-slate-400 mr-1 shrink-0 hidden sm:inline" />
            {categories.map((c) => (
              <button
                key={c.id}
                type="button"
                onClick={() => setUserSelectedCategory(c.id)}
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

        {/* Programs Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
          {filteredPrograms.map((program) => (
            <ProgramCard key={program.id} {...program} />
          ))}
        </div>
      </div>
    </div>
  )
}
