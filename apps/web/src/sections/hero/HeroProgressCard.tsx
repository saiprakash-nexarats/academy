import React from 'react'
import { motion } from 'framer-motion'
import { cn } from '@/lib/utils'

interface HeroProgressCardProps {
  className?: string
}

export const HeroProgressCard: React.FC<HeroProgressCardProps> = ({ className }) => {
  return (
    <motion.div
      initial={{ opacity: 0, y: -15, scale: 0.95 }}
      animate={{ opacity: 1, y: 0, scale: 1 }}
      transition={{ duration: 0.6, delay: 0.2 }}
      whileHover={{ y: -3, transition: { duration: 0.2 } }}
      className={cn(
        'bg-white/95 backdrop-blur-md rounded-2xl p-3.5 shadow-card border border-slate-100/90 z-20',
        className
      )}
    >
      <div className="text-[11px] font-bold text-slate-800 tracking-tight mb-2">
        My Learning Progress
      </div>
      <div className="flex items-center gap-3">
        {/* Circular Progress Gauge */}
        <div className="relative w-12 h-12 flex items-center justify-center shrink-0">
          <svg className="w-12 h-12 -rotate-90" viewBox="0 0 36 36">
            {/* Background circle */}
            <path
              className="text-blue-100"
              strokeWidth="3.5"
              stroke="currentColor"
              fill="none"
              d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
            />
            {/* 75% stroke circle */}
            <path
              className="text-brand-600"
              strokeDasharray="75, 100"
              strokeLinecap="round"
              strokeWidth="3.5"
              stroke="currentColor"
              fill="none"
              d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
            />
          </svg>
          <span className="absolute text-[11px] font-extrabold text-brand-700">75%</span>
        </div>

        {/* Text Details */}
        <div className="min-w-0">
          <div className="text-xs font-bold text-slate-900 truncate">
            Full Stack Development
          </div>
          <div className="text-[11px] text-slate-500 mt-0.5">
            12 of 16 lessons completed
          </div>
        </div>
      </div>
    </motion.div>
  )
}
