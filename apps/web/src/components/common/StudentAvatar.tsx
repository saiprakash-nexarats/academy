import React, { useState } from 'react'
import { CheckCircle2 } from 'lucide-react'
import { cn } from '@/lib/utils'

interface StudentAvatarProps {
  src?: string
  alt: string
  size?: 'sm' | 'md' | 'lg' | 'xl'
  className?: string
  showVerified?: boolean
}

export const StudentAvatar: React.FC<StudentAvatarProps> = ({
  src,
  alt,
  size = 'md',
  className,
  showVerified = true,
}) => {
  const [hasError, setHasError] = useState(false)

  // Explicit dimensions to prevent any unconstrained CSS layout breaks
  const sizeClasses = {
    sm: 'w-10 h-10 min-w-[40px] min-h-[40px] max-w-[40px] max-h-[40px] text-xs',
    md: 'w-12 h-12 min-w-[48px] min-h-[48px] max-w-[48px] max-h-[48px] text-sm',
    lg: 'w-14 h-14 min-w-[56px] min-h-[56px] max-w-[56px] max-h-[56px] text-base',
    xl: 'w-16 h-16 min-w-[64px] min-h-[64px] max-w-[64px] max-h-[64px] text-lg',
  }

  // Generate initials from name (e.g., "Priya Sharma" -> "PS")
  const initials = alt
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join('') || 'NA'

  return (
    <div className={cn('relative inline-block shrink-0', className)}>
      <div
        className={cn(
          'rounded-full overflow-hidden border-2 border-white shadow-sm ring-1 ring-slate-200/80 bg-slate-100 flex items-center justify-center select-none',
          sizeClasses[size]
        )}
      >
        {!hasError && src ? (
          <img
            src={src}
            alt={alt}
            onError={() => setHasError(true)}
            className="w-full h-full object-cover object-top block"
            loading="lazy"
          />
        ) : (
          <div className="w-full h-full bg-gradient-to-tr from-brand-600 via-indigo-600 to-sky-400 text-white font-bold flex items-center justify-center">
            {initials}
          </div>
        )}
      </div>

      {showVerified && (
        <span
          title="Verified Nexarats Alumni"
          className="absolute -bottom-0.5 -right-0.5 w-4.5 h-4.5 bg-white rounded-full flex items-center justify-center shadow-xs"
        >
          <CheckCircle2 className="w-3.5 h-3.5 fill-brand-600 text-white" />
        </span>
      )}
    </div>
  )
}
