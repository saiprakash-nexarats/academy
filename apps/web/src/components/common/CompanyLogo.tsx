import React from 'react'
import { FcGoogle } from 'react-icons/fc'
import {
  BiLogoAmazon,
  BiLogoAdobe,
} from 'react-icons/bi'
import {
  SiSwiggy,
  SiRazorpay,
  SiAtlassian,
  SiUber,
  SiNetflix,
  SiApple,
  SiMeta,
  SiSpotify,
} from 'react-icons/si'
import { cn } from '@/lib/utils'

export type CompanyName =
  | 'google'
  | 'microsoft'
  | 'amazon'
  | 'adobe'
  | 'meta'
  | 'apple'
  | 'netflix'
  | 'uber'
  | 'razorpay'
  | 'swiggy'
  | 'atlassian'
  | 'spotify'
  | string

interface CompanyIconProps {
  name: CompanyName
  className?: string
}

export const CompanyIcon: React.FC<CompanyIconProps> = ({ name, className = 'w-4 h-4' }) => {
  const normalized = name.toLowerCase().trim()

  switch (normalized) {
    case 'google':
    case 'google partner':
    case 'google partner agency':
      return <FcGoogle className={cn(className, 'shrink-0')} aria-label="Google" />

    case 'microsoft':
      return (
        <svg
          viewBox="0 0 21 21"
          className={cn(className, 'shrink-0')}
          aria-label="Microsoft"
          fill="none"
        >
          <rect x="1" y="1" width="9" height="9" fill="#F25022" />
          <rect x="11" y="1" width="9" height="9" fill="#7FBA00" />
          <rect x="1" y="11" width="9" height="9" fill="#00A4EF" />
          <rect x="11" y="11" width="9" height="9" fill="#FFB900" />
        </svg>
      )

    case 'amazon':
    case 'amazon aws':
    case 'aws':
      return (
        <BiLogoAmazon
          className={cn(className, 'shrink-0 text-[#FF9900]')}
          aria-label="Amazon"
        />
      )

    case 'adobe':
      return (
        <BiLogoAdobe
          className={cn(className, 'shrink-0 text-[#FA0F00]')}
          aria-label="Adobe"
        />
      )

    case 'meta':
      return (
        <SiMeta
          className={cn(className, 'shrink-0 text-[#0668E1]')}
          aria-label="Meta"
        />
      )

    case 'apple':
      return (
        <SiApple
          className={cn(className, 'shrink-0 text-slate-900')}
          aria-label="Apple"
        />
      )

    case 'uber':
      return (
        <SiUber
          className={cn(className, 'shrink-0 text-slate-900')}
          aria-label="Uber"
        />
      )

    case 'razorpay':
      return (
        <SiRazorpay
          className={cn(className, 'shrink-0 text-[#0C2340]')}
          aria-label="Razorpay"
        />
      )

    case 'swiggy':
      return (
        <SiSwiggy
          className={cn(className, 'shrink-0 text-[#FC8019]')}
          aria-label="Swiggy"
        />
      )

    case 'atlassian':
      return (
        <SiAtlassian
          className={cn(className, 'shrink-0 text-[#0052CC]')}
          aria-label="Atlassian"
        />
      )

    case 'netflix':
      return (
        <SiNetflix
          className={cn(className, 'shrink-0 text-[#E50914]')}
          aria-label="Netflix"
        />
      )

    case 'spotify':
      return (
        <SiSpotify
          className={cn(className, 'shrink-0 text-[#1ED760]')}
          aria-label="Spotify"
        />
      )

    default:
      return (
        <span className="w-4 h-4 rounded-full bg-brand-100 text-brand-700 text-[10px] font-extrabold flex items-center justify-center shrink-0">
          {name.charAt(0).toUpperCase()}
        </span>
      )
  }
}

interface CompanyBadgeProps {
  name: string
  className?: string
  showName?: boolean
  size?: 'sm' | 'md' | 'lg'
}

export const CompanyBadge: React.FC<CompanyBadgeProps> = ({
  name,
  className,
  showName = true,
  size = 'md',
}) => {
  const iconSizeClasses = {
    sm: 'w-3.5 h-3.5',
    md: 'w-4 h-4',
    lg: 'w-5 h-5',
  }

  const badgePadding = {
    sm: 'px-2.5 py-1 text-xs gap-1.5',
    md: 'px-3.5 py-1.5 text-xs sm:text-sm gap-2',
    lg: 'px-4 py-2 text-sm gap-2.5',
  }

  return (
    <div
      className={cn(
        'inline-flex items-center rounded-xl bg-white border border-slate-200/90 text-slate-800 font-bold shadow-2xs hover:border-brand-300 hover:shadow-xs transition-all select-none',
        badgePadding[size],
        className
      )}
    >
      <CompanyIcon name={name} className={cn(iconSizeClasses[size], 'shrink-0')} />
      {showName && <span className="font-heading tracking-tight leading-none">{name}</span>}
    </div>
  )
}
