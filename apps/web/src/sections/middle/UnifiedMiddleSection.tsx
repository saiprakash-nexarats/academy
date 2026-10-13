import React from 'react'
import { useNavigate } from 'react-router-dom'
import { ArrowRight } from 'lucide-react'
import { motion } from 'framer-motion'
import { ProgramCard } from '@/sections/programs/ProgramCard'
import { PlatformCard } from '@/sections/experience/PlatformCard'
import { POPULAR_PROGRAMS, WHY_CHOOSE_LEFT, WHY_CHOOSE_RIGHT } from '@/data'
import { Button } from '@/components/ui/Button'
import { useModals } from '@/context'
import { cn } from '@/lib/utils'

interface UnifiedMiddleSectionProps {
  onOpenEnroll?: () => void
  onExplorePrograms?: () => void
  className?: string
}

export const UnifiedMiddleSection: React.FC<UnifiedMiddleSectionProps> = ({
  onOpenEnroll,
  onExplorePrograms,
  className,
}) => {
  const navigate = useNavigate()
  const { openDemoModal } = useModals()

  const handleExplorePrograms = onExplorePrograms || (() => navigate('/programs'))
  const handleOpenPlatform = onOpenEnroll || (() => openDemoModal())
  return (
    <div className={cn('space-y-10 sm:space-y-12', className)}>
      {/* 2-Column Grid on XL Screens Matching Reference Mockup */}
      <div className="grid grid-cols-1 xl:grid-cols-12 gap-6 lg:gap-8 items-stretch">
        {/* Left Master Column: Popular Programs (Top) + Platform Experience (Bottom) */}
        <div className="xl:col-span-8 2xl:col-span-9 space-y-8 sm:space-y-10 flex flex-col justify-between">
          
          {/* TOP TIER: Popular Programs */}
          <div id="programs" className="bg-white rounded-3xl border border-slate-200/90 shadow-card p-5 sm:p-6 lg:p-7 scroll-mt-24">
            <div className="flex flex-col lg:flex-row gap-6 lg:gap-7 items-start">
              {/* Left Title Block */}
              <div className="w-full lg:w-48 xl:w-52 2xl:w-56 shrink-0 space-y-3.5">
                <div className="inline-flex items-center px-3 py-1 rounded-full bg-brand-50 text-brand-600 border border-brand-200/90 text-xs font-bold uppercase tracking-wider shadow-sm">
                  Our Programs
                </div>

                <h2 className="font-heading font-extrabold text-2xl sm:text-3xl text-slate-900 tracking-tight leading-tight">
                  Popular Programs
                </h2>

                <p className="text-xs sm:text-sm text-slate-600 leading-relaxed">
                  Learn in-demand skills with industry-aligned programs and real-world projects.
                </p>

                <div className="pt-1">
                  <button
                    type="button"
                    onClick={handleExplorePrograms}
                    className="group inline-flex items-center gap-2 px-4 py-2 rounded-xl border border-slate-300 hover:border-brand-400 text-slate-700 hover:text-brand-600 font-bold text-xs bg-white hover:bg-brand-50/50 shadow-sm transition-all cursor-pointer active:scale-95"
                  >
                    <span>View All Programs</span>
                    <ArrowRight className="w-3.5 h-3.5 text-brand-600 transition-transform group-hover:translate-x-1" />
                  </button>
                </div>
              </div>

              {/* 4 Program Cards in Grid */}
              <div className="flex-1 w-full grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-3.5">
                {POPULAR_PROGRAMS.map((program, index) => (
                  <motion.div
                    key={program.id}
                    initial={{ opacity: 0, y: 15 }}
                    whileInView={{ opacity: 1, y: 0 }}
                    viewport={{ once: true }}
                    transition={{ duration: 0.35, delay: index * 0.06 }}
                    className="h-full"
                  >
                    <ProgramCard {...program} />
                  </motion.div>
                ))}
              </div>
            </div>
          </div>

          {/* BOTTOM TIER: The Nexarats Experience */}
          <div id="experience" className="bg-white rounded-3xl border border-slate-200/90 shadow-card p-5 sm:p-6 lg:p-7 scroll-mt-24">
            <div className="flex flex-col lg:flex-row gap-6 lg:gap-7 items-center">
              {/* Left Title Block */}
              <div className="w-full lg:w-48 xl:w-52 2xl:w-56 shrink-0 space-y-3.5">
                <div className="inline-flex items-center px-3 py-1 rounded-full bg-brand-50 text-brand-600 border border-brand-200/90 text-xs font-bold uppercase tracking-wider shadow-sm">
                  The Nexarats Experience
                </div>

                <h2 className="font-heading font-extrabold text-2xl sm:text-3xl text-slate-900 tracking-tight leading-tight">
                  Your Complete Learning Journey in One Platform
                </h2>

                <p className="text-xs sm:text-sm text-slate-600 leading-relaxed">
                  Track progress, join live classes, complete projects, and connect with mentors — all in one place.
                </p>

                <div className="pt-1">
                  <Button
                    variant="primary"
                    size="sm"
                    onClick={handleOpenPlatform}
                    iconRight={<ArrowRight className="w-3.5 h-3.5" />}
                    className="bg-brand-600 hover:bg-brand-700 text-white font-bold px-5 py-2.5 rounded-xl shadow-button-glow hover:shadow-lg active:scale-95 transition-all text-xs"
                  >
                    Explore the Platform
                  </Button>
                </div>
              </div>

              {/* Center Dashboard Mockup Card */}
              <div className="flex-1 w-full">
                <PlatformCard />
              </div>
            </div>
          </div>

        </div>

        {/* Right Master Column: Unified "Why Choose Nexarats?" Section */}
        <div className="xl:col-span-4 2xl:col-span-3">
          <div id="why-choose" className="bg-white rounded-3xl border border-slate-200/90 shadow-card p-6 sm:p-7 h-full flex flex-col justify-between scroll-mt-24">
            {/* Header */}
            <div className="space-y-3 mb-6">
              <div className="inline-flex items-center px-3 py-1 rounded-full bg-brand-50 text-brand-600 border border-brand-200/90 text-xs font-bold uppercase tracking-wider shadow-sm">
                Why Nexarats
              </div>

              <h2 className="font-heading font-extrabold text-2xl sm:text-3xl text-slate-900 tracking-tight leading-tight">
                Why Choose Nexarats?
              </h2>

              <p className="text-xs sm:text-sm text-slate-600 leading-relaxed">
                More than just courses — we give you the right skills, guidance, and support to build a successful career.
              </p>
            </div>

            {/* Feature Group 1: Core Pillars */}
            <div className="space-y-3">
              {WHY_CHOOSE_LEFT.map((feature, index) => {
                const Icon = feature.icon
                return (
                  <motion.div
                    key={feature.id}
                    initial={{ opacity: 0, x: 10 }}
                    whileInView={{ opacity: 1, x: 0 }}
                    viewport={{ once: true }}
                    transition={{ duration: 0.3, delay: index * 0.05 }}
                    whileHover={{ x: 4, transition: { duration: 0.15 } }}
                    className="bg-slate-50/70 hover:bg-white hover:shadow-card hover:border-brand-300 border border-slate-200/80 rounded-2xl p-3 sm:p-3.5 flex items-center gap-3 transition-all cursor-pointer group"
                  >
                    <div className="w-9 h-9 rounded-xl bg-white group-hover:bg-brand-600 group-hover:text-white text-brand-600 border border-slate-200 group-hover:border-brand-600 flex items-center justify-center shrink-0 shadow-sm transition-colors duration-200">
                      <Icon className="w-4 h-4" />
                    </div>
                    <div className="min-w-0">
                      <h4 className="font-heading font-bold text-xs sm:text-sm text-slate-900 group-hover:text-brand-600 transition-colors truncate">
                        {feature.title}
                      </h4>
                      <p className="text-[11px] text-slate-500 truncate">
                        {feature.description}
                      </p>
                    </div>
                  </motion.div>
                )
              })}
            </div>

            {/* Decorative Divider Connector between Top and Bottom Pillars */}
            <div className="relative my-4 flex items-center justify-center">
              <div className="w-full border-t border-dashed border-slate-200" />
              <span className="absolute bg-white px-2 text-[10px] font-bold text-slate-400 uppercase tracking-widest">
                Connected Experience
              </span>
            </div>

            {/* Feature Group 2: Interactive Learning Flow (Connected to Platform) */}
            <div className="space-y-3">
              {WHY_CHOOSE_RIGHT.map((feature, index) => {
                const Icon = feature.icon
                return (
                  <motion.div
                    key={feature.id}
                    initial={{ opacity: 0, x: 10 }}
                    whileInView={{ opacity: 1, x: 0 }}
                    viewport={{ once: true }}
                    transition={{ duration: 0.3, delay: (index + 4) * 0.05 }}
                    whileHover={{ x: 4, transition: { duration: 0.15 } }}
                    className="bg-slate-50/70 hover:bg-white hover:shadow-card hover:border-brand-300 border border-slate-200/80 rounded-2xl p-3 sm:p-3.5 flex items-center gap-3 transition-all cursor-pointer group"
                  >
                    <div className="w-9 h-9 rounded-xl bg-blue-50 group-hover:bg-brand-600 group-hover:text-white text-brand-600 border border-blue-100 group-hover:border-brand-600 flex items-center justify-center shrink-0 shadow-sm transition-colors duration-200">
                      <Icon className="w-4 h-4" />
                    </div>
                    <div className="min-w-0">
                      <h4 className="font-heading font-bold text-xs sm:text-sm text-slate-900 group-hover:text-brand-600 transition-colors truncate">
                        {feature.title}
                      </h4>
                      <p className="text-[11px] text-slate-500 truncate">
                        {feature.description}
                      </p>
                    </div>
                  </motion.div>
                )
              })}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
