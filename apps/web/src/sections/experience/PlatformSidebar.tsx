import React from 'react'
import { GraduationCap } from 'lucide-react'
import { PLATFORM_MENU_ITEMS } from '@/data'
import { cn } from '@/lib/utils'

interface PlatformSidebarProps {
  className?: string
}

export const PlatformSidebar: React.FC<PlatformSidebarProps> = ({ className }) => {
  return (
    <div
      className={cn(
        'w-full md:w-48 lg:w-52 bg-[#131F37] text-slate-300 p-3 sm:p-4 flex flex-col justify-between shrink-0 select-none border-b md:border-b-0 md:border-r border-slate-700/60',
        className
      )}
    >
      <div>
        {/* Brand header */}
        <div className="flex items-center gap-2 mb-3 md:mb-6 px-1">
          <div className="w-6 h-6 sm:w-7 sm:h-7 rounded-lg bg-brand-600 flex items-center justify-center text-white shrink-0">
            <GraduationCap className="w-3.5 h-3.5 sm:w-4 sm:h-4" />
          </div>
          <span className="font-heading font-bold text-xs tracking-tight text-white truncate">
            Nexarats Academy
          </span>
        </div>

        {/* Navigation list: horizontal scroll on mobile, vertical stack on tablet/desktop */}
        <nav className="flex md:flex-col overflow-x-auto no-scrollbar gap-1 pb-1 md:pb-0">
          {PLATFORM_MENU_ITEMS.map((item) => {
            const Icon = item.icon
            return (
              <div
                key={item.label}
                className={cn(
                  'flex items-center gap-2 sm:gap-2.5 px-2.5 sm:px-3 py-1.5 sm:py-2 rounded-xl text-xs font-medium transition-colors cursor-pointer shrink-0 md:shrink',
                  item.active
                    ? 'bg-brand-600 text-white font-semibold shadow-sm'
                    : 'text-slate-400 hover:text-white hover:bg-white/5'
                )}
              >
                <Icon className={cn('w-3.5 h-3.5 shrink-0', item.active ? 'text-white' : 'text-slate-400')} />
                <span className="truncate">{item.label}</span>
              </div>
            )
          })}
        </nav>
      </div>
    </div>
  )
}
