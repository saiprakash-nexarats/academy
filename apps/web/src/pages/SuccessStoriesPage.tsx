import React, { useState } from 'react'
import {
  Star,
  ArrowRight,
  Quote,
  Search,
  Filter,
  Building2,
  Award,
  Sparkles,
  ExternalLink,
  HelpCircle,
  Code2,
  X,
} from 'lucide-react'
import { motion, AnimatePresence } from 'framer-motion'
import {
  ALL_SUCCESS_STORIES,
  PLACEMENT_STATISTICS,
  HIRING_PARTNERS_LIST,
  PLACEMENT_FAQS,
  type SuccessStoryExtended,
} from '@/data'
import { StudentAvatar, CompanyIcon } from '@/components/common'
import { Badge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { useModals } from '@/context'

export const SuccessStoriesPage: React.FC = () => {
  const { openEnrollModal, openDemoModal } = useModals()

  const [selectedDomain, setSelectedDomain] = useState<string>('all')
  const [selectedBackground, setSelectedBackground] = useState<string>('all')
  const [searchQuery, setSearchQuery] = useState<string>('')
  const [activeModalStory, setActiveModalStory] = useState<SuccessStoryExtended | null>(null)

  const domainTabs = [
    { id: 'all', label: 'All Domains' },
    { id: 'Software Engineering', label: 'Software Engineering' },
    { id: 'Artificial Intelligence', label: 'AI & Data Science' },
    { id: 'Design', label: 'UI/UX Design' },
    { id: 'Marketing', label: 'Digital Marketing' },
  ]

  const backgroundFilters = [
    { id: 'all', label: 'All Backgrounds' },
    { id: 'Career Switcher', label: 'Career Switchers' },
    { id: 'College Graduate', label: 'College Graduates' },
    { id: 'Domain Pivot', label: 'Domain Pivots' },
    { id: 'Fast Track Placed', label: 'Fast Track Placed' },
  ]

  const filteredStories = ALL_SUCCESS_STORIES.filter((story) => {
    const matchesDomain =
      selectedDomain === 'all' || story.category === selectedDomain
    const matchesBackground =
      selectedBackground === 'all' || story.storyBadge === selectedBackground
    const matchesSearch =
      story.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      story.company.toLowerCase().includes(searchQuery.toLowerCase()) ||
      story.role.toLowerCase().includes(searchQuery.toLowerCase()) ||
      story.previousRole.toLowerCase().includes(searchQuery.toLowerCase()) ||
      story.capstoneProject.toLowerCase().includes(searchQuery.toLowerCase())

    return matchesDomain && matchesBackground && matchesSearch
  })

  // Top featured spotlight story
  const featuredSpotlight = ALL_SUCCESS_STORIES.find((s) => s.featured) || ALL_SUCCESS_STORIES[0]

  return (
    <div className="py-10 sm:py-16">
      <div className="academy-container space-y-12 sm:space-y-16">
        {/* 1. Header Hero */}
        <div className="max-w-3xl space-y-4">
          <Badge variant="primary" icon={<Sparkles className="w-3.5 h-3.5 text-brand-600" />}>
            Verified Alumni Impact & Career Outcomes
          </Badge>
          <h1 className="font-heading font-extrabold text-3xl sm:text-4xl lg:text-5xl text-slate-900 tracking-tight leading-tight">
            Real Transformations. <br className="hidden sm:inline" />
            <span className="text-brand-600">Backed by Industry Outcomes.</span>
          </h1>
          <p className="text-sm sm:text-base text-slate-600 leading-relaxed">
            See how 50,000+ ambitious learners from non-technical careers, legacy domains, and university programs landed high-impact roles at top tech companies worldwide.
          </p>
        </div>

        {/* 2. Key Placement Statistics Banner */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 p-6 sm:p-8 rounded-3xl bg-blue-50/70 text-slate-800 shadow-sm border border-blue-200/80">
          {PLACEMENT_STATISTICS.map((stat, i) => (
            <div
              key={stat.label}
              className={`p-3 sm:p-4 text-center sm:text-left ${
                i !== PLACEMENT_STATISTICS.length - 1 ? 'lg:border-r lg:border-blue-200/60' : ''
              }`}
            >
              <div className="font-heading font-black text-2xl sm:text-3xl lg:text-4xl text-brand-600">
                {stat.value}
              </div>
              <div className="font-bold text-xs sm:text-sm text-slate-900 mt-1">
                {stat.label}
              </div>
              <div className="text-[11px] text-slate-600 mt-0.5 leading-snug">
                {stat.sub}
              </div>
            </div>
          ))}
        </div>

        {/* 3. Hiring Partners Strip */}
        <div className="p-6 rounded-2xl bg-white border border-slate-200/90 shadow-sm space-y-3">
          <div className="flex items-center justify-between text-xs font-bold text-slate-400 uppercase tracking-wider">
            <span className="flex items-center gap-1.5">
              <Building2 className="w-3.5 h-3.5 text-brand-600" />
              Where Our Graduates Get Hired
            </span>
            <span className="text-brand-600 hidden sm:inline">500+ Partner Tech Companies</span>
          </div>
          <div className="flex flex-wrap items-center gap-3 pt-1">
            {HIRING_PARTNERS_LIST.map((partner) => (
              <div
                key={partner.name}
                className="inline-flex items-center gap-2 px-3.5 py-2 rounded-xl bg-slate-50 border border-slate-200/80 font-heading font-extrabold text-xs sm:text-sm text-slate-800 tracking-wide hover:bg-white hover:border-brand-300 hover:text-brand-600 transition-all cursor-default shadow-2xs select-none"
              >
                <CompanyIcon name={partner.name} className="w-4 h-4 shrink-0" />
                <span>{partner.name}</span>
              </div>
            ))}
            <div className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-blue-50 border border-blue-200 font-heading font-bold text-xs sm:text-sm text-brand-700 shadow-2xs">
              <Building2 className="w-4 h-4 text-brand-600" />
              <span>+500 Tech Partners</span>
            </div>
          </div>
        </div>

        {/* 4. Featured Alumni Spotlight Card */}
        {featuredSpotlight && (
          <div className="relative overflow-hidden rounded-3xl bg-gradient-to-r from-blue-50 via-indigo-50/50 to-white border border-blue-200/80 p-6 sm:p-10 shadow-sm">
            <div className="flex flex-col lg:flex-row items-center gap-8 justify-between">
              <div className="space-y-4 max-w-2xl">
                <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-brand-600 text-white text-xs font-bold uppercase tracking-wider shadow-sm">
                  <Award className="w-3.5 h-3.5" />
                  <span>Alumni Spotlight of the Month</span>
                </div>

                <div className="flex items-center gap-4">
                  <StudentAvatar
                    src={featuredSpotlight.avatar}
                    alt={featuredSpotlight.name}
                    size="xl"
                  />
                  <div>
                    <h2 className="font-heading font-black text-xl sm:text-2xl text-slate-900">
                      {featuredSpotlight.name}
                    </h2>
                    <p className="text-xs sm:text-sm font-semibold text-slate-600 flex items-center gap-1.5 mt-0.5">
                      <span>Now a {featuredSpotlight.role} at</span>
                      <span className="text-brand-600 font-bold inline-flex items-center gap-1">
                        <CompanyIcon name={featuredSpotlight.company} className="w-3.5 h-3.5 shrink-0" />
                        <span>{featuredSpotlight.company}</span>
                      </span>
                    </p>
                    <div className="inline-block mt-1 px-2.5 py-0.5 rounded-full bg-emerald-100 text-emerald-800 font-extrabold text-xs">
                      {featuredSpotlight.salaryHike}
                    </div>
                  </div>
                </div>

                <div className="p-4 rounded-2xl bg-white border border-blue-100 shadow-xs text-xs sm:text-sm text-slate-700 italic leading-relaxed">
                  "{featuredSpotlight.quote}"
                </div>

                <div className="flex flex-wrap items-center gap-4 text-xs text-slate-600 pt-1">
                  <span className="font-semibold">
                    Career Transition:{' '}
                    <strong className="text-slate-900">{featuredSpotlight.previousRole}</strong> →{' '}
                    <strong className="text-brand-600">{featuredSpotlight.role}</strong>
                  </span>
                  <span className="text-slate-300">•</span>
                  <span>{featuredSpotlight.cohortYear}</span>
                </div>
              </div>

              <div className="w-full lg:w-auto shrink-0 flex flex-col gap-3">
                <Button
                  variant="primary"
                  size="lg"
                  onClick={() => setActiveModalStory(featuredSpotlight)}
                  iconRight={<ArrowRight className="w-4 h-4" />}
                  className="bg-brand-600 hover:bg-brand-700 text-white font-bold rounded-xl shadow-button-glow cursor-pointer"
                >
                  Read Full Case Study
                </Button>
                <button
                  type="button"
                  onClick={() => openEnrollModal()}
                  className="px-6 py-3 rounded-xl border border-slate-300 bg-white hover:bg-slate-50 font-bold text-xs sm:text-sm text-slate-800 transition-all cursor-pointer text-center"
                >
                  Apply For Next Cohort
                </button>
              </div>
            </div>
          </div>
        )}

        {/* 5. Filters & Search Section */}
        <div className="space-y-4">
          <div className="flex flex-col md:flex-row items-center justify-between gap-4 p-4 rounded-2xl bg-white border border-slate-200/90 shadow-sm">
            {/* Domain Tabs */}
            <div className="flex items-center gap-1.5 overflow-x-auto no-scrollbar w-full md:w-auto pb-1 md:pb-0">
              <Filter className="w-4 h-4 text-slate-400 mr-1 shrink-0 hidden sm:inline" />
              {domainTabs.map((d) => (
                <button
                  key={d.id}
                  type="button"
                  onClick={() => setSelectedDomain(d.id)}
                  className={`px-3.5 py-2 rounded-xl text-xs font-bold transition-all shrink-0 cursor-pointer ${
                    selectedDomain === d.id
                      ? 'bg-brand-600 text-white shadow-sm'
                      : 'bg-slate-50 text-slate-600 hover:bg-slate-100 hover:text-slate-900'
                  }`}
                >
                  {d.label}
                </button>
              ))}
            </div>

            {/* Search Input */}
            <div className="relative w-full md:w-72">
              <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
              <input
                type="text"
                placeholder="Search alumni, companies, roles..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-9 pr-3.5 py-2 text-xs bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-brand-500"
              />
            </div>
          </div>

          {/* Sub-Filter: Background */}
          <div className="flex items-center gap-2 overflow-x-auto no-scrollbar px-1">
            <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider shrink-0 mr-1">
              Background:
            </span>
            {backgroundFilters.map((b) => (
              <button
                key={b.id}
                type="button"
                onClick={() => setSelectedBackground(b.id)}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all shrink-0 cursor-pointer ${
                  selectedBackground === b.id
                    ? 'bg-slate-800 text-white'
                    : 'bg-white border border-slate-200/80 text-slate-600 hover:bg-slate-50'
                }`}
              >
                {b.label}
              </button>
            ))}
          </div>
        </div>

        {/* 6. Transformation Cards Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredStories.map((story) => (
            <motion.div
              key={story.id}
              initial={{ opacity: 0, y: 15 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.3 }}
              className="group bg-white rounded-2xl border border-slate-200/90 shadow-card hover:shadow-card-hover hover:border-brand-300 transition-all p-6 flex flex-col justify-between"
            >
              <div>
                {/* Header: Fixed StudentAvatar + Name + Hike Badge */}
                <div className="flex items-start justify-between gap-3 mb-4">
                  <div className="flex items-center gap-3 min-w-0">
                    <StudentAvatar
                      src={story.avatar}
                      alt={story.name}
                      size="lg"
                    />
                    <div className="min-w-0">
                      <h3 className="font-heading font-bold text-base text-slate-900 group-hover:text-brand-600 transition-colors truncate">
                        {story.name}
                      </h3>
                      <p className="text-xs text-slate-500 font-medium truncate">
                        {story.programCompleted}
                      </p>
                      <span className="text-[10px] text-slate-400 font-medium">
                        {story.cohortYear}
                      </span>
                    </div>
                  </div>

                  <span className="px-2.5 py-1 rounded-full bg-emerald-50 text-emerald-700 font-extrabold text-xs shrink-0 border border-emerald-200/80">
                    {story.salaryHike}
                  </span>
                </div>

                {/* Transition Flow: Before -> After */}
                <div className="p-3 rounded-xl bg-slate-50 border border-slate-100 mb-4 flex items-center justify-between text-xs">
                  <div className="min-w-0 flex-1">
                    <span className="text-[10px] text-slate-400 uppercase font-bold block">
                      Previous Role
                    </span>
                    <span className="font-medium text-slate-600 truncate block">
                      {story.previousRole}
                    </span>
                  </div>

                  <div className="mx-2 p-1 rounded-full bg-white border border-slate-200 text-brand-600 shrink-0">
                    <ArrowRight className="w-3.5 h-3.5" />
                  </div>

                  <div className="min-w-0 flex-1 text-right">
                    <span className="text-[10px] text-brand-600 uppercase font-bold flex items-center justify-end gap-1">
                      <CompanyIcon name={story.company} className="w-3 h-3 shrink-0" />
                      <span>Hired @ {story.company}</span>
                    </span>
                    <span className="font-bold text-slate-900 truncate block">
                      {story.role}
                    </span>
                  </div>
                </div>

                {/* Capstone Project Pill */}
                <div className="flex items-start gap-2 p-2.5 rounded-xl bg-blue-50/50 border border-blue-100/80 mb-4 text-xs text-slate-700">
                  <Code2 className="w-3.5 h-3.5 text-brand-600 shrink-0 mt-0.5" />
                  <div className="min-w-0">
                    <span className="font-bold text-slate-900 block text-[11px]">
                      Capstone Defense:
                    </span>
                    <span className="text-slate-600 line-clamp-1 text-[11px]">
                      {story.capstoneProject}
                    </span>
                  </div>
                </div>

                {/* Quote Text */}
                <div className="relative pl-5 mb-4">
                  <Quote className="w-3.5 h-3.5 text-brand-300 absolute left-0 top-0.5" />
                  <p className="text-xs sm:text-sm text-slate-600 leading-relaxed italic line-clamp-3">
                    "{story.quote}"
                  </p>
                </div>
              </div>

              {/* Card Footer: Credential ID + Stars + View Details */}
              <div className="pt-3.5 border-t border-slate-100 flex items-center justify-between text-xs text-slate-500">
                <div className="flex items-center gap-1 text-amber-500 font-bold">
                  {[...Array(story.rating || 5)].map((_, i) => (
                    <Star key={i} className="w-3.5 h-3.5 fill-amber-400 text-amber-400" />
                  ))}
                  <span className="text-[11px] text-slate-400 font-medium ml-1">
                    Verified
                  </span>
                </div>

                <button
                  type="button"
                  onClick={() => setActiveModalStory(story)}
                  className="font-bold text-brand-600 hover:text-brand-700 text-xs flex items-center gap-1 cursor-pointer hover:underline"
                >
                  <span>Case Study</span>
                  <ExternalLink className="w-3 h-3" />
                </button>
              </div>
            </motion.div>
          ))}
        </div>

        {/* Empty Search State */}
        {filteredStories.length === 0 && (
          <div className="p-12 text-center bg-white rounded-3xl border border-slate-200/90 space-y-3">
            <Search className="w-8 h-8 text-slate-400 mx-auto" />
            <h3 className="font-heading font-bold text-lg text-slate-900">
              No transformation stories found
            </h3>
            <p className="text-xs text-slate-500 max-w-sm mx-auto">
              Try adjusting your domain filters or search query to see other alumni profiles.
            </p>
            <button
              type="button"
              onClick={() => {
                setSelectedDomain('all')
                setSelectedBackground('all')
                setSearchQuery('')
              }}
              className="px-4 py-2 rounded-xl bg-brand-50 text-brand-600 font-bold text-xs hover:bg-brand-100"
            >
              Reset Filters
            </button>
          </div>
        )}

        {/* 7. Placement & Hiring FAQs */}
        <div className="space-y-6 pt-6">
          <div className="flex items-center gap-2">
            <HelpCircle className="w-5 h-5 text-brand-600" />
            <h2 className="text-2xl sm:text-3xl font-heading font-extrabold text-slate-900">
              Frequently Asked Placement Questions
            </h2>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            {PLACEMENT_FAQS.map((faq, i) => (
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

        {/* 8. Bottom CTA Banner */}
        <div className="p-8 sm:p-12 rounded-3xl bg-blue-50/80 border border-blue-200/80 text-slate-900 flex flex-col sm:flex-row items-center justify-between gap-6 shadow-sm text-center sm:text-left">
          <div className="space-y-2 max-w-xl">
            <h2 className="text-2xl sm:text-3xl font-heading font-extrabold text-slate-900">
              Ready to write your own success story?
            </h2>
            <p className="text-xs sm:text-sm text-slate-600 leading-relaxed">
              Book a 1-on-1 career consultation call with an industry advisor and find the right cohort for your goals.
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-3 shrink-0">
            <button
              type="button"
              onClick={() => openEnrollModal()}
              className="inline-flex items-center gap-2 px-6 py-3 rounded-full bg-brand-600 hover:bg-brand-700 text-white font-bold text-sm shadow-button-glow hover:shadow-lg transition-all cursor-pointer active:scale-95"
            >
              <span>Enroll Now</span>
              <ArrowRight className="w-4 h-4 text-white" />
            </button>
            <button
              type="button"
              onClick={() => openDemoModal()}
              className="px-6 py-3 rounded-full border border-slate-300 hover:border-brand-600 bg-white hover:bg-slate-50 text-slate-800 font-bold text-sm shadow-xs transition-all cursor-pointer active:scale-95"
            >
              Book a Free Demo
            </button>
          </div>
        </div>
      </div>

      {/* 9. Comprehensive Full Case Study Modal */}
      <AnimatePresence>
        {activeModalStory && activeModalStory.caseStudy && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-slate-900/60 backdrop-blur-sm">
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              className="bg-white rounded-3xl max-w-3xl w-full p-6 sm:p-8 shadow-2xl border border-slate-200 relative overflow-hidden max-h-[90vh] overflow-y-auto"
            >
              <button
                type="button"
                onClick={() => setActiveModalStory(null)}
                className="absolute top-5 right-5 p-2 rounded-full hover:bg-slate-100 text-slate-400 hover:text-slate-700 cursor-pointer z-10"
              >
                <X className="w-5 h-5" />
              </button>

              <div className="space-y-6">
                {/* Header Profile */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-5 border-b border-slate-100 pr-8">
                  <div className="flex items-center gap-4">
                    <StudentAvatar
                      src={activeModalStory.avatar}
                      alt={activeModalStory.name}
                      size="xl"
                    />
                    <div>
                      <div className="flex items-center gap-2">
                        <h3 className="font-heading font-black text-xl sm:text-2xl text-slate-900">
                          {activeModalStory.name}
                        </h3>
                        <span className="px-2 py-0.5 rounded-full bg-blue-50 text-brand-700 font-bold text-[10px]">
                          {activeModalStory.cohortYear}
                        </span>
                      </div>
                      <p className="text-xs sm:text-sm text-slate-600 mt-0.5 flex items-center gap-1.5 flex-wrap">
                        <span>{activeModalStory.programCompleted} • Placed as <strong className="text-slate-900">{activeModalStory.role}</strong> at</span>
                        <span className="inline-flex items-center gap-1 text-brand-600 font-bold">
                          <CompanyIcon name={activeModalStory.company} className="w-3.5 h-3.5 shrink-0" />
                          <span>{activeModalStory.company}</span>
                        </span>
                      </p>
                      <div className="flex items-center gap-2 mt-1 text-[11px] text-slate-400">
                        <span>Credential ID: <code className="font-mono text-slate-600">{activeModalStory.credentialId}</code></span>
                        <span>•</span>
                        <span className="text-emerald-700 font-bold">Audited Offer Letter Verified</span>
                      </div>
                    </div>
                  </div>

                  <div className="text-right sm:self-center shrink-0">
                    <span className="inline-block px-3 py-1.5 rounded-xl bg-emerald-50 text-emerald-700 font-black text-sm border border-emerald-200/80 shadow-xs">
                      {activeModalStory.salaryHike}
                    </span>
                    <span className="block text-[11px] text-slate-400 mt-0.5 font-medium">
                      {activeModalStory.caseStudy.salaryMultiplier}
                    </span>
                  </div>
                </div>

                {/* Compensation & Role Transition Bar */}
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 p-4 rounded-2xl bg-slate-50 border border-slate-100 text-center sm:text-left">
                  <div>
                    <span className="text-[10px] text-slate-400 uppercase font-bold block">Previous Role</span>
                    <span className="font-bold text-xs sm:text-sm text-slate-700 truncate block">{activeModalStory.previousRole}</span>
                    <span className="text-[11px] text-slate-400">{activeModalStory.caseStudy.startingPackage}</span>
                  </div>
                  <div>
                    <span className="text-[10px] text-brand-600 uppercase font-bold block">New Role</span>
                    <span className="font-bold text-xs sm:text-sm text-slate-900 truncate block">{activeModalStory.role}</span>
                    <span className="text-[11px] text-emerald-700 font-bold">{activeModalStory.caseStudy.placedPackage}</span>
                  </div>
                  <div>
                    <span className="text-[10px] text-slate-400 uppercase font-bold block">Domain Track</span>
                    <span className="font-bold text-xs sm:text-sm text-slate-700 block">{activeModalStory.category}</span>
                    <span className="text-[11px] text-slate-400">6 Months Cohort</span>
                  </div>
                  <div>
                    <span className="text-[10px] text-slate-400 uppercase font-bold block">Time to Offer</span>
                    <span className="font-bold text-xs sm:text-sm text-emerald-700 block">4 Months</span>
                    <span className="text-[11px] text-slate-400">Direct Mentor Referral</span>
                  </div>
                </div>

                {/* Section 1: Background Story */}
                <div className="space-y-2">
                  <h4 className="font-heading font-bold text-sm text-slate-900 uppercase tracking-wider text-brand-600">
                    Background & Career Challenge
                  </h4>
                  <p className="text-xs sm:text-sm text-slate-700 leading-relaxed bg-white p-4 rounded-2xl border border-slate-100">
                    {activeModalStory.caseStudy.backgroundStory}
                  </p>
                </div>

                {/* Section 2: 4-Phase Learning Timeline */}
                <div className="space-y-3">
                  <h4 className="font-heading font-bold text-sm text-slate-900 uppercase tracking-wider text-brand-600">
                    Learning Journey & Milestones
                  </h4>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                    {activeModalStory.caseStudy.learningTimeline.map((step) => (
                      <div key={step.phase} className="p-3.5 rounded-xl bg-slate-50 border border-slate-100 space-y-1">
                        <div className="flex items-center justify-between text-xs">
                          <span className="font-black text-brand-600 uppercase tracking-wider text-[11px]">{step.phase}</span>
                          <span className="text-slate-400 text-[10px] font-medium">{step.duration}</span>
                        </div>
                        <h5 className="font-bold text-xs text-slate-900">{step.title}</h5>
                        <p className="text-[11px] text-slate-600 leading-relaxed">{step.details}</p>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Section 3: Capstone Engineering Breakdown */}
                <div className="space-y-3 p-5 rounded-2xl bg-blue-50/40 border border-blue-100/80">
                  <div className="flex items-center gap-2">
                    <Code2 className="w-4 h-4 text-brand-600" />
                    <h4 className="font-heading font-bold text-sm text-slate-900">
                      Capstone Project: {activeModalStory.caseStudy.capstoneDetails.title}
                    </h4>
                  </div>
                  <p className="text-xs text-slate-700 leading-relaxed">
                    {activeModalStory.caseStudy.capstoneDetails.overview}
                  </p>

                  <div className="space-y-1.5 pt-1">
                    <span className="text-[10px] font-bold uppercase tracking-wider text-slate-500 block">Production Tech Stack</span>
                    <div className="flex flex-wrap gap-1.5">
                      {activeModalStory.caseStudy.capstoneDetails.techStack.map((tech) => (
                        <span key={tech} className="px-2.5 py-0.5 rounded-md bg-white border border-blue-200 text-brand-700 font-bold text-xs">
                          {tech}
                        </span>
                      ))}
                    </div>
                  </div>

                  <div className="space-y-1.5 pt-2 border-t border-blue-100">
                    <span className="text-[10px] font-bold uppercase tracking-wider text-slate-500 block">Technical Challenges Overcome</span>
                    <ul className="space-y-1 text-xs text-slate-700">
                      {activeModalStory.caseStudy.capstoneDetails.challengesSolved.map((chal, i) => (
                        <li key={i} className="flex items-start gap-2">
                          <span className="text-brand-600 font-bold">•</span>
                          <span>{chal}</span>
                        </li>
                      ))}
                    </ul>
                  </div>
                </div>

                {/* Section 4: Real Interview Rounds & Questions */}
                <div className="space-y-3">
                  <h4 className="font-heading font-bold text-sm text-slate-900 uppercase tracking-wider text-brand-600">
                    Actual Interview Rounds at {activeModalStory.company}
                  </h4>
                  <div className="space-y-2.5">
                    {activeModalStory.caseStudy.interviewRounds.map((rnd) => (
                      <div key={rnd.round} className="p-3.5 rounded-xl bg-slate-50 border border-slate-100 text-xs space-y-1">
                        <div className="flex items-center justify-between">
                          <span className="font-bold text-slate-900">{rnd.round}: {rnd.title}</span>
                          <span className="text-[10px] font-semibold text-brand-600 bg-brand-50 px-2 py-0.5 rounded-md">{rnd.focus}</span>
                        </div>
                        <p className="text-slate-600 text-[11px] leading-relaxed">
                          <strong className="text-slate-800">Key Question Asked:</strong> "{rnd.keyQuestion}"
                        </p>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Section 5: Mentor Impact & Student Advice */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-2">
                  <div className="p-4 rounded-xl bg-slate-50 border border-slate-100 text-xs space-y-1">
                    <span className="text-[10px] font-bold uppercase tracking-wider text-brand-600 block">Mentor Coaching Impact</span>
                    <p className="text-slate-700 italic leading-relaxed text-[11px]">
                      "{activeModalStory.caseStudy.mentorGuidanceImpact}"
                    </p>
                  </div>
                  <div className="p-4 rounded-xl bg-emerald-50/60 border border-emerald-100 text-xs space-y-1">
                    <span className="text-[10px] font-bold uppercase tracking-wider text-emerald-800 block">Alumni Advice to Future Learners</span>
                    <p className="text-slate-700 leading-relaxed text-[11px]">
                      "{activeModalStory.caseStudy.studentAdvice}"
                    </p>
                  </div>
                </div>

                {/* Action Buttons */}
                <div className="pt-4 border-t border-slate-100 flex flex-col sm:flex-row items-center gap-3">
                  <Button
                    variant="primary"
                    size="md"
                    onClick={() => {
                      setActiveModalStory(null)
                      openEnrollModal()
                    }}
                    className="w-full sm:flex-1 justify-center bg-brand-600 hover:bg-brand-700 font-bold rounded-xl py-3 shadow-button-glow cursor-pointer"
                  >
                    Enroll in {activeModalStory.programCompleted} Cohort
                  </Button>
                  <button
                    type="button"
                    onClick={() => setActiveModalStory(null)}
                    className="w-full sm:w-auto px-6 py-3 rounded-xl border border-slate-300 hover:bg-slate-50 text-slate-700 font-bold text-xs cursor-pointer"
                  >
                    Close Case Study
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
