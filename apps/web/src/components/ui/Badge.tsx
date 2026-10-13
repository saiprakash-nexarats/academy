import React from 'react'
import { cn } from '@/lib/utils'

export interface BadgeProps extends React.HTMLAttributes<HTMLSpanElement> {
  variant?: 'primary' | 'bestseller' | 'neutral' | 'success' | 'outline'
  size?: 'sm' | 'md'
  icon?: React.ReactNode
}

export const Badge: React.FC<BadgeProps> = ({
  children,
  className,
  variant = 'primary',
  size = 'md',
  icon,
  ...props
}) => {
  const sizeClasses = {
    sm: 'text-[11px] px-2 py-0.5',
    md: 'text-xs px-2.5 py-1',
  }

  const variantClasses = {
    primary: 'bg-brand-50 text-brand-700 border border-brand-200/80 font-semibold',
    bestseller: 'bg-brand-600 text-white font-semibold shadow-sm',
    neutral: 'bg-slate-100 text-slate-700 border border-slate-200/80 font-medium',
    success: 'bg-emerald-50 text-emerald-700 border border-emerald-200 font-semibold',
    outline: 'bg-transparent text-slate-600 border border-slate-200 font-medium',
  }

  return (
    <span
      className={cn(
        'inline-flex items-center gap-1.5 rounded-full select-none transition-colors',
        sizeClasses[size],
        variantClasses[variant],
        className
      )}
      {...props}
    >
      {icon && <span className="inline-flex shrink-0">{icon}</span>}
      <span>{children}</span>
    </span>
  )
}
