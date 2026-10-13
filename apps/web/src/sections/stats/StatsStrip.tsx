import React from 'react'
import { motion } from 'framer-motion'
import { STATS_DATA } from '@/data'
import { cn } from '@/lib/utils'

interface StatsStripProps {
  className?: string
}

export const StatsStrip: React.FC<StatsStripProps> = ({ className }) => {
  return (
    <section className={cn('relative z-20 -mt-5 sm:-mt-7 mb-4 sm:mb-6', className)}>
      <div className="academy-container">
        <motion.div
          initial={{ opacity: 0, y: 15 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.5 }}
          className="bg-white rounded-2xl md:rounded-3xl border border-slate-200/90 shadow-card py-5 sm:py-6 px-4 sm:px-8 lg:px-10"
        >
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6 lg:gap-0 divide-y sm:divide-y-0 lg:divide-y-0 divide-slate-100">
            {STATS_DATA.map((stat, index) => {
              const Icon = stat.icon
              const isLast = index === STATS_DATA.length - 1

              return (
                <div
                  key={stat.id}
                  className={cn(
                    'flex items-center gap-3 sm:gap-4 py-2 sm:py-0 lg:px-6 xl:px-8 transition-transform duration-200 hover:-translate-y-0.5',
                    !isLast && 'lg:border-r lg:border-slate-200/80',
                    index === 1 && 'sm:border-r sm:border-slate-200/80 lg:border-r'
                  )}
                >
                  {/* Blue Icon Square */}
                  <div className="w-11 h-11 sm:w-12 sm:h-12 rounded-xl bg-blue-50 text-brand-600 flex items-center justify-center shrink-0 border border-blue-100/90 shadow-sm">
                    <Icon className="w-5 h-5 sm:w-6 sm:h-6 text-brand-600" />
                  </div>

                  {/* Value and Label */}
                  <div className="min-w-0">
                    <div className="text-xl sm:text-2xl lg:text-3xl font-extrabold font-heading text-slate-900 tracking-tight leading-none">
                      {stat.value}
                    </div>
                    <div className="text-xs sm:text-sm text-slate-500 font-medium mt-1 truncate">
                      {stat.label}
                    </div>
                  </div>
                </div>
              )
            })}
          </div>
        </motion.div>
      </div>
    </section>
  )
}
