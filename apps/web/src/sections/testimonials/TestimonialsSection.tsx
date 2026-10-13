import React, { useState, useRef } from 'react'
import { Link } from 'react-router-dom'
import { ArrowRight, Play, Pause, ChevronLeft, ChevronRight } from 'lucide-react'
import { motion } from 'framer-motion'
import { TestimonialCard } from './TestimonialCard'
import { TESTIMONIALS_DATA } from '@/data'
import { cn } from '@/lib/utils'

interface TestimonialsSectionProps {
  className?: string
}

export const TestimonialsSection: React.FC<TestimonialsSectionProps> = ({ className }) => {
  const [isPaused, setIsPaused] = useState(false)
  const scrollContainerRef = useRef<HTMLDivElement>(null)

  const handleManualScroll = (direction: 'left' | 'right') => {
    if (scrollContainerRef.current) {
      const offset = direction === 'left' ? -380 : 380
      scrollContainerRef.current.scrollBy({ left: offset, behavior: 'smooth' })
    }
  }

  // Duplicate the array for a seamless, continuous slow-moving marquee loop
  const marqueeItems = [...TESTIMONIALS_DATA, ...TESTIMONIALS_DATA]

  return (
    <section className={cn('py-1 sm:py-2 overflow-hidden', className)}>
      {/* Header with Title and Moving Controls */}
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-3 mb-5">
        <motion.div
          initial={{ opacity: 0, y: 15 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.5 }}
          className="max-w-2xl space-y-3"
        >
          <div className="inline-flex items-center px-3 py-1 rounded-full bg-brand-50 text-brand-600 border border-brand-200/90 text-xs font-bold uppercase tracking-wider shadow-sm">
            Testimonials
          </div>

          <h2 className="font-heading font-extrabold text-2xl sm:text-3xl lg:text-4xl text-slate-900 tracking-tight leading-tight">
            Student Success Stories
          </h2>

          <p className="text-sm sm:text-base text-slate-600 leading-relaxed">
            Real stories from {TESTIMONIALS_DATA.length}+ ambitious learners who transformed their careers with Nexarats Academy.
          </p>
        </motion.div>

        {/* Right Action Controls: Play/Pause, Manual Arrows & View All Stories */}
        <div className="flex flex-wrap items-center gap-2.5 sm:gap-3 shrink-0 self-start md:self-auto">
          {/* Slow Motion Play/Pause Toggle */}
          <button
            type="button"
            onClick={() => setIsPaused(!isPaused)}
            title={isPaused ? 'Resume slow auto-scroll' : 'Pause slow auto-scroll'}
            className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-full border border-slate-300 hover:border-brand-500 bg-white hover:bg-slate-50 text-slate-700 hover:text-brand-600 text-xs font-bold transition-all shadow-sm hover:shadow cursor-pointer select-none active:scale-95"
          >
            {isPaused ? (
              <>
                <Play className="w-3.5 h-3.5 fill-brand-600 text-brand-600" />
                <span>Resume Motion</span>
              </>
            ) : (
              <>
                <Pause className="w-3.5 h-3.5 text-slate-500" />
                <span>Slow Motion</span>
              </>
            )}
          </button>

          {/* Manual Arrow Buttons */}
          <div className="hidden sm:flex items-center gap-1.5">
            <button
              type="button"
              onClick={() => handleManualScroll('left')}
              aria-label="Scroll left"
              className="p-2 rounded-full border border-slate-300 bg-white hover:bg-slate-50 text-slate-600 hover:text-slate-900 shadow-sm hover:shadow cursor-pointer transition-all active:scale-95"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <button
              type="button"
              onClick={() => handleManualScroll('right')}
              aria-label="Scroll right"
              className="p-2 rounded-full border border-slate-300 bg-white hover:bg-slate-50 text-slate-600 hover:text-slate-900 shadow-sm hover:shadow cursor-pointer transition-all active:scale-95"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>

          {/* View All Stories Button */}
          <Link
            to="/success-stories"
            id="view-all-stories-btn"
            className="group inline-flex items-center gap-2.5 px-5 py-2.5 rounded-full border border-slate-300 hover:border-brand-600 bg-white hover:bg-slate-50 text-slate-800 hover:text-brand-600 font-semibold text-xs sm:text-sm shadow-sm hover:shadow transition-all cursor-pointer active:scale-95"
          >
            <span>View All Stories</span>
            <ArrowRight className="w-3.5 h-3.5 text-brand-600 transition-transform group-hover:translate-x-1" />
          </Link>
        </div>
      </div>

      {/* Slowly Moving Conveyor Strip */}
      <div className="relative overflow-hidden">
        {/* Soft edge blur masks */}
        <div className="pointer-events-none absolute left-0 top-0 bottom-0 w-8 sm:w-16 bg-gradient-to-r from-[#f8fafc] to-transparent z-10" />
        <div className="pointer-events-none absolute right-0 top-0 bottom-0 w-8 sm:w-16 bg-gradient-to-l from-[#f8fafc] to-transparent z-10" />

        {/* Overflow Container with zero visible scrollbar */}
        <div
          ref={scrollContainerRef}
          className="overflow-x-auto no-scrollbar py-2 [scrollbar-width:none] [-ms-overflow-style:none] [&::-webkit-scrollbar]:hidden [&::-webkit-scrollbar]:w-0 [&::-webkit-scrollbar]:h-0"
          style={{ scrollbarWidth: 'none', msOverflowStyle: 'none' }}
        >
          <div
            className={cn(
              'animate-marquee-slow flex items-stretch gap-5 sm:gap-6',
              isPaused && 'marquee-paused'
            )}
          >
            {marqueeItems.map((testimonial, idx) => (
              <div
                key={`${testimonial.id}-${idx}`}
                className="w-[320px] sm:w-[370px] shrink-0 transition-transform hover:-translate-y-1"
              >
                <TestimonialCard {...testimonial} className="h-full" />
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  )
}
