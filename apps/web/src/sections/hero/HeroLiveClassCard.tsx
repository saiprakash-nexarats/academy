import React from 'react'
import { Video } from 'lucide-react'
import { motion } from 'framer-motion'
import { cn } from '@/lib/utils'

interface HeroLiveClassCardProps {
  onJoinClass?: () => void
  className?: string
}

export const HeroLiveClassCard: React.FC<HeroLiveClassCardProps> = ({ onJoinClass, className }) => {
  return (
    <motion.div
      initial={{ opacity: 0, x: -20 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ duration: 0.6, delay: 0.3 }}
      whileHover={{ y: -3, transition: { duration: 0.2 } }}
      className={cn(
        'bg-white/95 backdrop-blur-md rounded-2xl p-3 shadow-card border border-slate-100/90 z-20 flex items-center justify-between gap-3 min-w-[270px]',
        className
      )}
    >
      <div className="flex items-center gap-3">
        {/* Blue video icon */}
        <div className="w-10 h-10 rounded-xl bg-brand-600 text-white flex items-center justify-center shrink-0 shadow-sm">
          <Video className="w-5 h-5" />
        </div>

        {/* Reminder text */}
        <div className="min-w-0">
          <div className="text-[10px] font-bold text-slate-500 uppercase tracking-wider">
            Live Class Reminder
          </div>
          <div className="text-[11px] font-medium text-slate-600">
            Today at 10:00 AM
          </div>
          <div className="text-xs font-bold text-slate-900 truncate">
            Advanced React Concepts
          </div>
        </div>
      </div>

      {/* Join Class CTA */}
      <button
        type="button"
        onClick={onJoinClass}
        className="shrink-0 px-3 py-1.5 bg-brand-600 hover:bg-brand-700 text-white text-xs font-semibold rounded-lg shadow-sm transition-all active:scale-95 cursor-pointer"
      >
        Join Class
      </button>
    </motion.div>
  )
}
