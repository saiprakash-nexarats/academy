import React from 'react'
import { motion } from 'framer-motion'
import { WHY_CHOOSE_LEFT, WHY_CHOOSE_RIGHT } from '@/data'
import { cn } from '@/lib/utils'

interface WhyChooseSectionProps {
  className?: string
}

export const WhyChooseSection: React.FC<WhyChooseSectionProps> = ({ className }) => {
  return (
    <section className={cn('py-1 sm:py-2', className)}>
      <motion.div
        initial={{ opacity: 0, y: 15 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true }}
        transition={{ duration: 0.5 }}
        className="bg-white rounded-3xl border border-slate-200/90 shadow-card p-6 sm:p-8"
      >
        {/* Section Header */}
        <div className="max-w-2xl mb-6 space-y-2.5">
          <div className="inline-flex items-center px-3 py-1 rounded-full bg-brand-50 text-brand-600 border border-brand-200/90 text-xs font-bold uppercase tracking-wider shadow-sm">
            Why Nexarats
          </div>

          <h2 className="font-heading font-extrabold text-2xl sm:text-3xl lg:text-4xl text-slate-900 tracking-tight leading-tight">
            Why Choose Nexarats?
          </h2>

          <p className="text-sm sm:text-base text-slate-600 leading-relaxed">
            More than just courses — we provide 100% job assurance, 1:1 mentorship from top tech leaders, and direct hiring referrals to 500+ partner companies.
          </p>
        </div>

        {/* Feature Grid with Interactive Dual Columns */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 lg:gap-6 relative">
          {/* Subtle Decorative Connector Line (desktop) */}
          <div className="hidden md:block absolute left-1/2 top-4 bottom-4 w-px border-r border-dashed border-slate-200 -translate-x-1/2 pointer-events-none" />

          {/* Left Column Features */}
          <div className="space-y-3.5">
            {WHY_CHOOSE_LEFT.map((feature, index) => {
              const Icon = feature.icon
              return (
                <motion.div
                  key={feature.id}
                  initial={{ opacity: 0, x: -10 }}
                  whileInView={{ opacity: 1, x: 0 }}
                  viewport={{ once: true }}
                  transition={{ duration: 0.3, delay: index * 0.08 }}
                  whileHover={{ x: 4, transition: { duration: 0.15 } }}
                  className="bg-slate-50/70 hover:bg-white hover:shadow-card hover:border-brand-300 border border-slate-200/80 rounded-2xl p-4 flex items-center gap-3.5 transition-all cursor-pointer group"
                >
                  <div className="w-10 h-10 rounded-xl bg-white group-hover:bg-brand-600 group-hover:text-white text-brand-600 border border-slate-200 group-hover:border-brand-600 flex items-center justify-center shrink-0 shadow-sm transition-colors duration-200">
                    <Icon className="w-5 h-5" />
                  </div>
                  <div className="min-w-0">
                    <h3 className="font-heading font-bold text-sm text-slate-900 group-hover:text-brand-600 transition-colors">
                      {feature.title}
                    </h3>
                    <p className="text-xs text-slate-500 mt-0.5">
                      {feature.description}
                    </p>
                  </div>
                </motion.div>
              )
            })}
          </div>

          {/* Right Column Features */}
          <div className="space-y-3.5">
            {rightFeaturesMapped(WHY_CHOOSE_RIGHT)}
          </div>
        </div>
      </motion.div>
    </section>
  )
}

function rightFeaturesMapped(features: typeof WHY_CHOOSE_RIGHT) {
  return features.map((feature, index) => {
    const Icon = feature.icon
    return (
      <motion.div
        key={feature.id}
        initial={{ opacity: 0, x: 10 }}
        whileInView={{ opacity: 1, x: 0 }}
        viewport={{ once: true }}
        transition={{ duration: 0.3, delay: index * 0.08 }}
        whileHover={{ x: 4, transition: { duration: 0.15 } }}
        className="bg-slate-50/70 hover:bg-white hover:shadow-card hover:border-brand-300 border border-slate-200/80 rounded-2xl p-4 flex items-center gap-3.5 transition-all cursor-pointer group"
      >
        <div className="w-10 h-10 rounded-xl bg-white group-hover:bg-brand-600 group-hover:text-white text-brand-600 border border-slate-200 group-hover:border-brand-600 flex items-center justify-center shrink-0 shadow-sm transition-colors duration-200">
          <Icon className="w-5 h-5" />
        </div>
        <div className="min-w-0">
          <h3 className="font-heading font-bold text-sm text-slate-900 group-hover:text-brand-600 transition-colors">
            {feature.title}
          </h3>
          <p className="text-xs text-slate-500 mt-0.5">
            {feature.description}
          </p>
        </div>
      </motion.div>
    )
  })
}
