import React from 'react'
import { Trophy, ArrowRight } from 'lucide-react'
import { motion } from 'framer-motion'
import { CompanyIcon } from '@/components/common'
import { cn } from '@/lib/utils'

interface HeroCertificateCardProps {
  onClick?: () => void
  className?: string
}

export const HeroCertificateCard: React.FC<HeroCertificateCardProps> = ({ onClick, className }) => {
  return (
    <motion.div
      initial={{ opacity: 0, x: 20 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ duration: 0.6, delay: 0.5 }}
      whileHover={{ y: -3, transition: { duration: 0.2 } }}
      onClick={onClick}
      className={cn(
        'bg-white/95 backdrop-blur-md rounded-2xl p-3 shadow-card border border-slate-100/90 z-20 flex items-center gap-3 min-w-[230px] cursor-pointer hover:border-amber-200 transition-all',
        className
      )}
    >
      {/* Trophy Badge in Gold/Amber */}
      <div className="w-10 h-10 rounded-xl bg-amber-50 border border-amber-200/60 text-amber-500 flex items-center justify-center shrink-0 shadow-sm">
        <Trophy className="w-5 h-5 fill-amber-400/30" />
      </div>

      {/* Certificate & Placement Details */}
      <div className="min-w-0">
        <div className="flex items-center gap-1.5">
          <span className="text-xs font-bold text-slate-900 leading-tight">
            Placement Verified!
          </span>
          <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 shrink-0" />
        </div>
        <div className="text-[10px] text-slate-600 mt-0.5 truncate font-medium flex items-center gap-1">
          <span>₹14.5 LPA •</span>
          <CompanyIcon name="Microsoft" className="w-2.5 h-2.5 shrink-0" />
          <span>Placed @ Microsoft</span>
        </div>
        <button
          type="button"
          onClick={onClick}
          className="text-[11px] font-semibold text-brand-600 hover:text-brand-700 inline-flex items-center gap-1 mt-1 cursor-pointer transition-colors"
        >
          <span>View Certificate</span>
          <ArrowRight className="w-3 h-3" />
        </button>
      </div>
    </motion.div>
  )
}

