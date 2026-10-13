import React from 'react'
import { ArrowRight, Play } from 'lucide-react'
import { motion } from 'framer-motion'
import { Button } from '@/components/ui/Button'
import { HeroFeatureRow } from './HeroFeatureRow'
import { cn } from '@/lib/utils'

interface HeroTextProps {
  onExplorePrograms?: () => void
  onBookDemo?: () => void
  className?: string
}

export const HeroText: React.FC<HeroTextProps> = ({
  onExplorePrograms,
  onBookDemo,
  className,
}) => {
  return (
    <div className={cn('flex flex-col space-y-6 lg:space-y-7', className)}>
      {/* Badge: 100% Job Assurance Guarantee */}
      <motion.div
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
      >
        <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-blue-50 text-brand-700 border border-blue-200/90 text-xs font-bold tracking-wide select-none shadow-xs">
          <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
          <span>100% Job Assurance Guarantee</span>
        </div>
      </motion.div>

      {/* Main Headline */}
      <motion.h1
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6, delay: 0.1 }}
        className="font-heading font-extrabold text-3xl sm:text-4xl md:text-5xl lg:text-[50px] xl:text-[56px] text-slate-900 tracking-tight leading-[1.12]"
      >
        Build Your Career <br className="hidden sm:inline" />
        With <span className="text-brand-600">100% Job Assurance</span>
      </motion.h1>

      {/* Supporting Description */}
      <motion.p
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6, delay: 0.2 }}
        className="text-sm sm:text-base lg:text-lg text-slate-600 leading-relaxed max-w-xl"
      >
        Industry-accredited tech training, expert 1:1 mentorship from top tech leaders, and guaranteed interview calls to help you gain in-demand skills and land your dream job.
      </motion.p>

      {/* CTA Buttons Row */}
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6, delay: 0.3 }}
        className="flex flex-wrap items-center gap-3.5 sm:gap-4 pt-1"
      >
        {/* Explore Programs CTA */}
        <Button
          variant="primary"
          size="lg"
          onClick={onExplorePrograms}
          iconRight={<ArrowRight className="w-4 h-4 transition-transform group-hover:translate-x-1" />}
          className="bg-brand-600 hover:bg-brand-700 text-white font-bold px-7 py-3.5 rounded-xl shadow-button-glow hover:shadow-lg active:scale-95 transition-all text-sm sm:text-base group cursor-pointer"
        >
          Explore Programs
        </Button>

        {/* Book a Demo CTA */}
        <button
          type="button"
          onClick={onBookDemo}
          className="group inline-flex items-center gap-2.5 px-6 py-3.5 rounded-xl bg-white hover:bg-slate-50 text-slate-800 hover:text-slate-900 font-bold border border-slate-300 hover:border-slate-400 shadow-sm active:scale-95 transition-all text-sm sm:text-base cursor-pointer"
        >
          <div className="w-6 h-6 rounded-full border-2 border-brand-600 flex items-center justify-center text-brand-600 group-hover:scale-105 transition-transform">
            <Play className="w-3 h-3 fill-brand-600 ml-0.5" />
          </div>
          <span>Book a Demo</span>
        </button>
      </motion.div>

      {/* Quick Highlights: Live Classes, Hands-on Projects, Career Support */}
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6, delay: 0.4 }}
      >
        <HeroFeatureRow />
      </motion.div>
    </div>
  )
}
