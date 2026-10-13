import React from 'react'
import { cn } from '@/lib/utils'

interface ProgramCardSkeletonProps {
  className?: string
}

export const ProgramCardSkeleton: React.FC<ProgramCardSkeletonProps> = ({ className }) => {
  return (
    <div
      className={cn(
        'bg-white rounded-2xl border border-slate-200/90 shadow-card flex flex-col overflow-hidden h-full animate-pulse select-none',
        className
      )}
    >
      {/* Shimmer Image Area */}
      <div className="relative aspect-[16/10] bg-slate-200/80 shrink-0">
        <div className="absolute top-2.5 left-2.5 w-16 h-4 bg-slate-300 rounded-md" />
        <div className="absolute top-2.5 right-2.5 w-20 h-4 bg-slate-300 rounded-md" />
      </div>

      {/* Shimmer Body Area */}
      <div className="p-3.5 sm:p-4 flex flex-col flex-1 justify-between gap-3">
        <div className="space-y-2">
          <div className="h-4 bg-slate-200 rounded w-4/5" />
          <div className="h-3 bg-slate-100 rounded w-full" />
          <div className="h-3 bg-slate-100 rounded w-2/3" />
        </div>

        {/* Shimmer Footer Meta */}
        <div className="pt-2.5 border-t border-slate-100 flex items-center justify-between gap-2">
          <div className="h-3 bg-slate-200 rounded w-16" />
          <div className="h-3 bg-slate-200 rounded w-20" />
          <div className="w-5 h-5 rounded-full bg-slate-200 ml-auto shrink-0" />
        </div>
      </div>
    </div>
  )
}
