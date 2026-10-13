import React from 'react'
import { HeroText } from './hero/HeroText'
import { HeroVisual } from './hero/HeroVisual'
import { useModals } from '@/context'
import { cn } from '@/lib/utils'

interface HeroSectionProps {
  onExplorePrograms?: () => void
  onBookDemo?: () => void
  onViewCertificate?: () => void
  className?: string
}

export const HeroSection: React.FC<HeroSectionProps> = ({
  onExplorePrograms,
  onBookDemo,
  onViewCertificate,
  className,
}) => {
  const modals = useModals()
  const handleExplore = onExplorePrograms || (() => modals.openEnrollModal())
  const handleDemo = onBookDemo || (() => modals.openDemoModal())
  const handleCertificate = onViewCertificate || (() => modals.openCertificateModal())

  return (
    <section className={cn('relative overflow-hidden bg-white pt-5 pb-10 sm:pt-7 sm:pb-12 lg:pt-9 lg:pb-14', className)}>
      {/* Background ambient lighting and soft blue glows */}
      <div className="absolute top-0 right-0 w-[700px] h-[700px] bg-gradient-to-bl from-blue-100/60 via-sky-50/30 to-transparent rounded-full blur-3xl -z-10 pointer-events-none" />
      <div className="absolute top-0 left-0 w-[500px] h-[500px] bg-gradient-to-br from-blue-50/70 via-sky-50/20 to-transparent rounded-full blur-3xl -z-10 pointer-events-none" />

      <div className="academy-container">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 lg:gap-8 items-center">
          {/* Left Hero Content Column */}
          <div className="lg:col-span-6 xl:col-span-6 z-10">
            <HeroText onExplorePrograms={handleExplore} onBookDemo={handleDemo} />
          </div>

          {/* Right Hero Visual Column */}
          <div className="lg:col-span-6 xl:col-span-6 z-10 lg:pl-4 xl:pl-8">
            <HeroVisual
              onViewCertificate={handleCertificate}
              onJoinLiveClass={handleDemo}
            />
          </div>
        </div>
      </div>
    </section>
  )
}
