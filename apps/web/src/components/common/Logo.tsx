import React from 'react'
import { GraduationCap } from 'lucide-react'
import { Link } from 'react-router-dom'
import { cn } from '@/lib/utils'

interface LogoProps {
  className?: string
  iconClassName?: string
  showText?: boolean
  inverted?: boolean
}

export const Logo: React.FC<LogoProps> = ({
  className,
  iconClassName,
  showText = true,
  inverted = false,
}) => {
  return (
    <Link
      to="/"
      className={cn(
        'inline-flex items-center gap-2.5 group transition-opacity hover:opacity-95 select-none shrink-0',
        className
      )}
    >
      <div
        className={cn(
          'w-10 h-10 rounded-xl flex items-center justify-center transition-all duration-200 group-hover:scale-105',
          inverted
            ? 'bg-white/10 text-white'
            : 'bg-brand-900 text-white shadow-sm'
        )}
      >
        <GraduationCap className={cn('w-6 h-6', iconClassName)} />
      </div>
      {showText && (
        <span
          className={cn(
            'font-heading font-bold text-xl md:text-2xl tracking-tight leading-none',
            inverted ? 'text-white' : 'text-[#0B1A30]'
          )}
        >
          Nexarats <span className={inverted ? 'text-blue-200' : 'text-brand-600'}>Academy</span>
        </span>
      )}
    </Link>
  )
}
