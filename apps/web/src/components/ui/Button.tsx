import React from 'react'
import { cn } from '@/lib/utils'

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'outline' | 'ghost' | 'soft'
  size?: 'sm' | 'md' | 'lg'
  iconLeft?: React.ReactNode
  iconRight?: React.ReactNode
  isLoading?: boolean
}

export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  (
    {
      children,
      className,
      variant = 'primary',
      size = 'md',
      iconLeft,
      iconRight,
      isLoading = false,
      disabled,
      ...props
    },
    ref
  ) => {
    const sizeClasses = {
      sm: 'text-xs px-3.5 py-1.5 rounded-lg gap-1.5',
      md: 'text-sm px-5 py-2.5 rounded-xl gap-2 font-semibold',
      lg: 'text-base px-6 py-3.5 rounded-xl gap-2.5 font-semibold',
    }

    const variantClasses = {
      primary:
        'bg-brand-600 hover:bg-brand-700 text-white shadow-button-glow hover:shadow-lg active:scale-[0.98] transition-all duration-200 border border-brand-500/30',
      secondary:
        'bg-white text-slate-800 hover:bg-slate-50 border border-slate-200 shadow-sm hover:border-slate-300 active:scale-[0.98] transition-all duration-200',
      outline:
        'bg-transparent text-slate-700 border border-slate-300 hover:bg-slate-50 hover:text-slate-900 active:scale-[0.98] transition-all duration-200',
      ghost:
        'bg-transparent text-slate-600 hover:text-brand-600 hover:bg-brand-50/60 active:scale-[0.98] transition-all duration-200',
      soft:
        'bg-brand-50 text-brand-700 hover:bg-brand-100 border border-brand-200/60 active:scale-[0.98] transition-all duration-200',
    }

    return (
      <button
        ref={ref}
        disabled={disabled || isLoading}
        className={cn(
          'inline-flex items-center justify-center transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500/50 disabled:opacity-50 disabled:pointer-events-none select-none cursor-pointer',
          sizeClasses[size],
          variantClasses[variant],
          className
        )}
        {...props}
      >
        {isLoading ? (
          <span className="w-4 h-4 border-2 border-current border-t-transparent rounded-full animate-spin mr-2" />
        ) : (
          iconLeft && <span className="inline-flex shrink-0">{iconLeft}</span>
        )}
        <span>{children}</span>
        {!isLoading && iconRight && <span className="inline-flex shrink-0">{iconRight}</span>}
      </button>
    )
  }
)

Button.displayName = 'Button'
