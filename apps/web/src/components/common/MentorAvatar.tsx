import React, { useState } from 'react'
import { ShieldCheck } from 'lucide-react'
import { cn } from '@/lib/utils'

interface MentorAvatarProps {
  src?: string
  alt: string
  company?: string
  size?: 'sm' | 'md' | 'lg' | 'xl'
  className?: string
  showBadge?: boolean
}

export const MentorAvatar: React.FC<MentorAvatarProps> = ({
  src,
  alt,
  company,
  size = 'lg',
  className,
  showBadge = true,
}) => {
  const [hasError, setHasError] = useState(false)

  const sizeClasses = {
    sm: 'w-10 h-10 min-w-[40px] min-h-[40px] max-w-[40px] max-h-[40px] text-xs',
    md: 'w-12 h-12 min-w-[48px] min-h-[48px] max-w-[48px] max-h-[48px] text-sm',
    lg: 'w-16 h-16 min-w-[64px] min-h-[64px] max-w-[64px] max-h-[64px] text-base',
    xl: 'w-20 h-20 min-w-[80px] min-h-[80px] max-w-[80px] max-h-[80px] text-xl',
  }

  // Extract initials (e.g., "Arjun Mehta" -> "AM")
  const initials = alt
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join('') || 'ME'

  // Dynamic distinct gradient background based on mentor name
  const getGradient = (name: string) => {
    const gradients = [
      'from-blue-600 via-indigo-600 to-purple-700',
      'from-emerald-600 via-teal-600 to-cyan-700',
      'from-rose-600 via-pink-600 to-amber-600',
      'from-violet-600 via-purple-700 to-indigo-800',
      'from-amber-600 via-orange-600 to-red-600',
    ]
    const index = Math.abs(name.split('').reduce((acc, c) => acc + c.charCodeAt(0), 0)) % gradients.length
    return gradients[index]
  }

  return (
    <div className={cn('relative inline-block shrink-0', className)}>
      <div
        className={cn(
          'rounded-2xl overflow-hidden border-2 border-white shadow-md ring-1 ring-slate-200/90 bg-slate-100 flex items-center justify-center select-none',
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
          <div
            className={cn(
              'w-full h-full bg-gradient-to-tr text-white font-black tracking-wider flex items-center justify-center shadow-inner',
              getGradient(alt)
            )}
          >
            {initials}
          </div>
        )}
      </div>

      {showBadge && (
        <span
          title={company ? `Verified Mentor at ${company}` : 'Verified Industry Mentor'}
          className="absolute -bottom-1 -right-1 w-5 h-5 bg-white rounded-full flex items-center justify-center shadow-xs border border-slate-200"
        >
          <ShieldCheck className="w-3.5 h-3.5 text-brand-600 fill-brand-50" />
        </span>
      )}
    </div>
  )
}
