import React from 'react'
import { MoreHorizontal } from 'lucide-react'
import { motion } from 'framer-motion'
import mentorArjunImg from '@/assets/mentor-arjun.jpg'
import { CompanyIcon } from '@/components/common'
import { cn } from '@/lib/utils'

interface HeroMentorCardProps {
  className?: string
}

export const HeroMentorCard: React.FC<HeroMentorCardProps> = ({ className }) => {
  return (
    <motion.div
      initial={{ opacity: 0, y: -20, scale: 0.95 }}
      animate={{ opacity: 1, y: 0, scale: 1 }}
      transition={{ duration: 0.6, delay: 0.4 }}
      whileHover={{ y: -3, transition: { duration: 0.2 } }}
      className={cn(
        'bg-white/95 backdrop-blur-md rounded-2xl p-3.5 shadow-card border border-slate-100/90 z-20 max-w-[260px]',
        className
      )}
    >
      <div className="flex items-center justify-between gap-2 mb-2">
        <div className="flex items-center gap-2 min-w-0">
          <img
            src={mentorArjunImg}
            alt="Mentor Arjun Mehta"
            className="w-8 h-8 rounded-full object-cover border border-slate-200 shrink-0"
          />
          <div className="min-w-0">
            <div className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider">
              Mentor Message
            </div>
            <div className="flex items-center gap-1.5">
              <span className="text-xs font-bold text-slate-900 truncate">Arjun Mehta</span>
              <span className="inline-flex items-center gap-0.5 text-[9px] font-bold text-slate-700 bg-slate-100 border border-slate-200/80 px-1.5 py-0.5 rounded shadow-2xs">
                <CompanyIcon name="Google" className="w-2.5 h-2.5 shrink-0" />
                <span>Google</span>
              </span>
            </div>
          </div>
        </div>

        <div className="flex items-center gap-1 text-[10px] text-slate-400 shrink-0">
          <span>2m ago</span>
          <MoreHorizontal className="w-3.5 h-3.5" />
        </div>
      </div>

      <div className="bg-slate-50 rounded-xl p-2 text-[11px] text-slate-600 leading-snug border border-slate-100">
        Great progress! Keep going. Your project looks amazing!
      </div>
    </motion.div>
  )
}
