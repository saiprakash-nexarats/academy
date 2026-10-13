import React from 'react'
import { cn } from '@/lib/utils'

export interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  hoverEffect?: boolean
  elevated?: boolean
}

export const Card: React.FC<CardProps> = ({
  children,
  className,
  hoverEffect = false,
  elevated = false,
  ...props
}) => {
  return (
    <div
      className={cn(
        'bg-white rounded-2xl border border-slate-200/90 transition-all duration-300',
        elevated ? 'shadow-elevated' : 'shadow-card',
        hoverEffect && 'hover:border-brand-300 hover:shadow-card-hover hover:-translate-y-1',
        className
      )}
      {...props}
    >
      {children}
    </div>
  )
}
