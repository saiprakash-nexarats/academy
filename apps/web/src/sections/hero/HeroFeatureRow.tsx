import React from 'react'
import { Video, Laptop, ShieldCheck } from 'lucide-react'
import { cn } from '@/lib/utils'

interface HeroFeatureRowProps {
  className?: string
}

export const HeroFeatureRow: React.FC<HeroFeatureRowProps> = ({ className }) => {
  const features = [
    {
      id: 'live-classes',
      label: 'Live Classes',
      icon: Video,
      highlight: false,
    },
    {
      id: 'projects',
      label: 'Hands-on Projects',
      icon: Laptop,
      highlight: false,
    },
    {
      id: 'job-assurance',
      label: '100% Job Assurance',
      icon: ShieldCheck,
      highlight: true,
    },
  ]

  return (
    <div className={cn('flex flex-wrap items-center gap-4 sm:gap-6 pt-3', className)}>
      {features.map((feature) => {
        const Icon = feature.icon
        return (
          <div
            key={feature.id}
            className={cn(
              'flex items-center gap-2 text-slate-700 font-medium text-xs sm:text-sm select-none',
              feature.highlight && 'text-brand-700 font-semibold'
            )}
          >
            <div
              className={cn(
                'w-7 h-7 rounded-lg flex items-center justify-center shrink-0 border shadow-2xs',
                feature.highlight
                  ? 'bg-blue-50 border-blue-200 text-brand-600'
                  : 'bg-slate-100 border-slate-200 text-slate-600'
              )}
            >
              <Icon className="w-3.5 h-3.5" />
            </div>
            <span className={cn('font-semibold', feature.highlight ? 'text-brand-700' : 'text-slate-800')}>
              {feature.label}
            </span>
          </div>
        )
      })}
    </div>
  )
}

