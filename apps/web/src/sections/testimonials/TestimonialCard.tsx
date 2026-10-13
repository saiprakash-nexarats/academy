import React from 'react'
import { Star } from 'lucide-react'
import { motion } from 'framer-motion'
import { StudentAvatar, CompanyIcon } from '@/components/common'
import type { CompanyBrand } from '@/types'
import { cn } from '@/lib/utils'

export interface TestimonialCardProps {
  id: string
  name: string
  role: string
  company: string
  companyBrand: CompanyBrand
  quote: string
  avatar: string
  rating?: number
  className?: string
}

export const TestimonialCard: React.FC<TestimonialCardProps> = ({
  name,
  role,
  company,
  companyBrand,
  quote,
  avatar,
  rating = 5,
  className,
}) => {
  return (
    <motion.div
      whileHover={{ y: -4, transition: { duration: 0.2 } }}
      className={cn(
        'group bg-white rounded-2xl border border-slate-200/90 shadow-card hover:shadow-card-hover hover:border-brand-300 p-5 sm:p-6 flex flex-col justify-between transition-all duration-300',
        className
      )}
    >
      <div>
        {/* Quote */}
        <p className="text-xs sm:text-sm text-slate-600 group-hover:text-slate-700 leading-relaxed italic mb-5 transition-colors">
          "{quote}"
        </p>
      </div>

      {/* Author and Placement Meta */}
      <div className="pt-4 border-t border-slate-100 flex items-center justify-between gap-3">
        <div className="flex items-center gap-3 min-w-0">
          <StudentAvatar
            src={avatar}
            alt={name}
            size="sm"
          />

          <div className="min-w-0">
            <div className="font-heading font-bold text-xs sm:text-sm text-slate-900 group-hover:text-brand-600 transition-colors truncate">
              {name}
            </div>
            <div className="flex items-center gap-1.5 text-[11px] text-slate-500 mt-0.5 truncate">
              <CompanyIcon name={companyBrand || company} className="w-3.5 h-3.5 shrink-0" />
              <span className="truncate">Now a {role} at <strong className="text-slate-800 font-semibold">{company}</strong></span>
            </div>
          </div>
        </div>

        {/* 5 Gold Stars */}
        <div className="flex items-center gap-0.5 shrink-0">
          {Array.from({ length: rating }).map((_, i) => (
            <Star
              key={i}
              className="w-3.5 h-3.5 fill-amber-400 text-amber-400"
            />
          ))}
        </div>
      </div>
    </motion.div>
  )
}
