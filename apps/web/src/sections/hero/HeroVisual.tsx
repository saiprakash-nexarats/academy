import React from 'react'
import heroStudentImg from '@/assets/hero-student-clean.png'
import { HeroProgressCard } from './HeroProgressCard'
import { HeroLiveClassCard } from './HeroLiveClassCard'
import { HeroMentorCard } from './HeroMentorCard'
import { HeroCertificateCard } from './HeroCertificateCard'
import { cn } from '@/lib/utils'

interface HeroVisualProps {
  onViewCertificate?: () => void
  onJoinLiveClass?: () => void
  className?: string
}

export const HeroVisual: React.FC<HeroVisualProps> = ({
  onViewCertificate,
  onJoinLiveClass,
  className,
}) => {
  return (
    <div className={cn('relative w-full max-w-xl mx-auto lg:max-w-none py-2', className)}>
      {/* Soft natural ambient glow */}
      <div className="absolute inset-0 bg-gradient-to-tr from-blue-100/40 via-sky-50/30 to-transparent rounded-full -z-10 blur-3xl transform scale-110 pointer-events-none" />

      {/* Decorative Sparkle 1 (Top Left doodle rays) */}
      <div className="absolute -top-4 left-6 sm:left-10 text-brand-500 opacity-80 pointer-events-none z-10 hidden sm:block">
        <svg width="42" height="42" viewBox="0 0 42 42" fill="none">
          <path d="M10 24L3 22M20 10L18 3M14 14L8 8" stroke="#2563EB" strokeWidth="2.5" strokeLinecap="round" />
        </svg>
      </div>

      {/* Decorative Sparkle 2 (4-point star near center-left) */}
      <div className="absolute top-1/2 -left-2 text-brand-400 pointer-events-none z-10 hidden lg:block">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 0L14.5 9.5L24 12L14.5 14.5L12 24L9.5 14.5L0 12L9.5 9.5L12 0Z" />
        </svg>
      </div>

      {/* Decorative Sparkle 3 (small 4-point star near bottom right) */}
      <div className="absolute bottom-12 right-2 text-brand-500 pointer-events-none z-10 hidden lg:block">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 0L14.5 9.5L24 12L14.5 14.5L12 24L9.5 14.5L0 12L9.5 9.5L12 0Z" />
        </svg>
      </div>

      {/* Main Student Visual - 100% Stick onto Background with ZERO Lines */}
      <div className="relative select-none">
        <img
          src={heroStudentImg}
          alt="Nexarats Academy student learning with headphones and laptop"
          className="w-full h-auto object-cover object-top select-none pointer-events-none"
        />
      </div>

      {/* CARD 1: Learning Progress (Top Left) - Fixed & Stuck On (No Floating) */}
      <div className="hidden sm:block absolute -top-3 sm:-top-4 left-1 sm:left-4 lg:-left-2 xl:-left-6 z-20">
        <HeroProgressCard />
      </div>

      {/* CARD 2: Live Class Reminder (Middle / Lower Left) - Fixed & Stuck On (No Floating) */}
      <div className="hidden sm:block absolute top-[48%] sm:top-[50%] -left-1 sm:-left-3 lg:-left-6 xl:-left-10 z-20">
        <HeroLiveClassCard onJoinClass={onJoinLiveClass} />
      </div>

      {/* CARD 3: Mentor Message (Top Right) - Fixed & Stuck On (No Floating) */}
      <div className="hidden sm:block absolute -top-3 sm:-top-4 right-1 sm:right-4 lg:-right-2 xl:-right-6 z-20">
        <HeroMentorCard />
      </div>

      {/* CARD 4: Placement Verified! (Middle Right) - Fixed & Stuck On (No Floating) */}
      <div className="hidden sm:block absolute top-[44%] sm:top-[46%] right-1 sm:-right-1 lg:-right-4 xl:-right-8 z-20">
        <HeroCertificateCard onClick={onViewCertificate} />
      </div>

      {/* Mobile Stacked Floating Cards (< 640px) */}
      <div className="sm:hidden mt-5 space-y-3">
        <HeroProgressCard />
        <HeroLiveClassCard onJoinClass={onJoinLiveClass} />
        <div className="grid grid-cols-1 gap-3">
          <HeroMentorCard />
          <HeroCertificateCard onClick={onViewCertificate} />
        </div>
      </div>
    </div>
  )
}
