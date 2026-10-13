import React from 'react'
import { Clock, BarChart, ArrowRight } from 'lucide-react'
import { motion } from 'framer-motion'
import { useModals } from '@/context'
import { cn } from '@/lib/utils'

export interface ProgramCardProps {
  id: string
  title: string
  description: string
  image: string
  duration: string
  level: string
  badge?: string
  onEnroll?: (id: string) => void
  className?: string
}

export const ProgramCard: React.FC<ProgramCardProps> = ({
  id,
  title,
  description,
  image,
  duration,
  level,
  badge,
  onEnroll,
  className,
}) => {
  const { openEnrollModal } = useModals()

  const handleCardClick = () => {
    if (onEnroll) {
      onEnroll(id)
    } else {
      openEnrollModal(id)
    }
  }

  return (
    <motion.div
      whileHover={{ y: -4, transition: { duration: 0.2 } }}
      onClick={handleCardClick}
      className={cn(
        'group bg-white rounded-2xl border border-slate-200/90 shadow-card hover:shadow-card-hover hover:border-brand-300 transition-all flex flex-col overflow-hidden cursor-pointer select-none h-full',
        className
      )}
    >
      {/* Top Image Container */}
      <div className="relative aspect-[16/10] overflow-hidden bg-slate-100 shrink-0">
        <img
          src={image}
          alt={title}
          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
        />

        {/* Optional Badge (e.g. Bestseller) */}
        {badge && (
          <div className="absolute top-2.5 left-2.5">
            <span className="px-2.5 py-0.5 rounded-md text-[10px] font-bold tracking-wider uppercase bg-brand-600 text-white shadow-sm">
              {badge}
            </span>
          </div>
        )}

        {/* 100% Job Assurance Trust Pill */}
        <div className="absolute top-2.5 right-2.5">
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-[10px] font-bold tracking-tight bg-white/95 text-emerald-700 border border-emerald-200/80 shadow-xs backdrop-blur-xs">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" />
            <span>Job Assured</span>
          </span>
        </div>
      </div>

      {/* Card Body */}
      <div className="p-3.5 sm:p-4 flex flex-col flex-1 justify-between gap-3">
        <div>
          <h3 className="font-heading font-bold text-sm sm:text-base text-slate-900 group-hover:text-brand-600 transition-colors leading-snug line-clamp-2 min-h-[2.5rem]">
            {title}
          </h3>
          <p className="text-xs text-slate-500 leading-relaxed mt-1 line-clamp-2 min-h-[2.25rem]">
            {description}
          </p>
        </div>

        {/* Card Footer Meta */}
        <div className="pt-2.5 border-t border-slate-100 flex items-center justify-between text-[10px] sm:text-[11px] text-slate-500 gap-1.5">
          <div className="flex items-center gap-1 shrink-0 whitespace-nowrap">
            <Clock className="w-3.5 h-3.5 text-slate-400 shrink-0" />
            <span className="whitespace-nowrap font-medium">{duration}</span>
          </div>

          <div className="flex items-center gap-1 shrink-0 whitespace-nowrap">
            <BarChart className="w-3.5 h-3.5 text-slate-400 shrink-0" />
            <span className="whitespace-nowrap font-medium">{level}</span>
          </div>

          {/* Action icon button */}
          <div className="w-5 h-5 sm:w-6 sm:h-6 rounded-full border border-brand-200 text-brand-600 group-hover:bg-brand-600 group-hover:text-white flex items-center justify-center transition-colors shrink-0 ml-auto">
            <ArrowRight className="w-3 h-3" />
          </div>
        </div>
      </div>
    </motion.div>
  )
}
