import React from 'react'
import { GraduationCap, ArrowRight } from 'lucide-react'
import { motion } from 'framer-motion'
import { useModals } from '@/context'
import { cn } from '@/lib/utils'

interface CtaBannerSectionProps {
  onGetStarted?: () => void
  className?: string
}

export const CtaBannerSection: React.FC<CtaBannerSectionProps> = ({ onGetStarted, className }) => {
  const { openEnrollModal } = useModals()
  const handleClick = onGetStarted || (() => openEnrollModal())

  return (
    <section className={cn('relative py-1 sm:py-2 mb-2 sm:mb-4 overflow-hidden', className)}>
      <motion.div
        initial={{ opacity: 0, scale: 0.98 }}
        whileInView={{ opacity: 1, scale: 1 }}
        viewport={{ once: true }}
        transition={{ duration: 0.6 }}
        className="relative rounded-3xl bg-gradient-to-r from-brand-700 via-brand-600 to-blue-600 p-6 sm:p-10 lg:p-12 text-white shadow-xl overflow-hidden"
      >
        {/* Ambient background curves & glow */}
        <div className="absolute -right-16 -top-16 w-80 h-80 bg-white/10 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute -left-16 -bottom-16 w-80 h-80 bg-blue-400/20 rounded-full blur-2xl pointer-events-none" />

        <div className="relative z-10 flex flex-col md:flex-row items-center justify-between gap-6 sm:gap-8">
          {/* Left Content */}
          <div className="flex flex-col sm:flex-row items-center sm:items-center text-center sm:text-left gap-4 sm:gap-6 w-full md:w-auto">
            <div className="w-14 h-14 sm:w-16 sm:h-16 rounded-2xl bg-white/15 backdrop-blur-md flex items-center justify-center shrink-0 border border-white/20 shadow-inner">
              <GraduationCap className="w-8 h-8 sm:w-9 sm:h-9 text-white" />
            </div>

            <div className="space-y-2">
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/15 border border-white/25 text-white text-xs font-bold tracking-wide backdrop-blur-sm">
                <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
                <span>100% Job Assurance Guarantee</span>
              </div>
              <h2 className="font-heading font-extrabold text-2xl sm:text-3xl lg:text-4xl text-white tracking-tight leading-tight">
                Launch Your Tech Career With 100% Job Assurance
              </h2>
              <p className="text-blue-100 text-xs sm:text-sm md:text-base leading-relaxed max-w-xl">
                Join 50,000+ ambitious learners. Get trained by Big Tech mentors and placed across 500+ hiring partners with assured interview calls.
              </p>
            </div>
          </div>

          {/* Right CTA Button */}
          <div className="w-full sm:w-auto shrink-0">
            <button
              type="button"
              onClick={handleClick}
              className="group w-full sm:w-auto inline-flex items-center justify-center gap-2 px-8 py-3.5 sm:py-4 bg-white hover:bg-blue-50 text-brand-700 font-bold text-sm sm:text-base rounded-xl shadow-lg hover:shadow-xl active:scale-95 transition-all cursor-pointer"
            >
              <span>Get Started</span>
              <ArrowRight className="w-4 h-4 text-brand-700 transition-transform group-hover:translate-x-1" />
            </button>
          </div>
        </div>
      </motion.div>
    </section>
  )
}
