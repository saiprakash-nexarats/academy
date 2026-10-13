import React from 'react'
import { ArrowRight } from 'lucide-react'
import { motion } from 'framer-motion'
import { PlatformCard } from './PlatformCard'
import { ConnectedFeatureNodes } from './ConnectedFeatureNodes'
import { Button } from '@/components/ui/Button'
import { useModals } from '@/context'
import { cn } from '@/lib/utils'

interface ExperienceSectionProps {
  onExplore?: () => void
  className?: string
}

export const ExperienceSection: React.FC<ExperienceSectionProps> = ({ onExplore, className }) => {
  const { openDemoModal } = useModals()
  const handleExplore = onExplore || (() => openDemoModal())

  return (
    <section className={cn('py-1 sm:py-2 relative overflow-hidden', className)}>
      <div className="grid grid-cols-1 xl:grid-cols-12 gap-8 lg:gap-10 items-center">
        {/* Left Column: Heading, Description, CTA */}
        <motion.div
          initial={{ opacity: 0, x: -20 }}
          whileInView={{ opacity: 1, x: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6 }}
          className="xl:col-span-3 space-y-4"
        >
          <div className="inline-flex items-center px-3 py-1 rounded-full bg-brand-50 text-brand-600 border border-brand-200/80 text-xs font-bold uppercase tracking-wider">
            The Nexarats Experience
          </div>

          <h2 className="font-heading font-extrabold text-2xl sm:text-3xl lg:text-4xl text-slate-900 tracking-tight leading-tight">
            Your Complete Learning Journey in One Platform
          </h2>

          <p className="text-sm sm:text-base text-slate-600 leading-relaxed">
            Track progress, join live classes, complete projects, and connect with mentors — all in one place.
          </p>

          <div className="pt-2">
            <Button
              variant="primary"
              size="md"
              onClick={handleExplore}
              iconRight={<ArrowRight className="w-4 h-4" />}
              className="bg-brand-600 hover:bg-brand-700 text-white font-bold px-6 py-3 rounded-xl shadow-button-glow hover:shadow-lg active:scale-95 transition-all text-xs sm:text-sm"
            >
              Explore the Platform
            </Button>
          </div>
        </motion.div>

        {/* Center Column: Dashboard Card Preview */}
        <div className="xl:col-span-6">
          <PlatformCard />
        </div>

        {/* Right Column: Connected Feature Nodes */}
        <div className="xl:col-span-3">
          <ConnectedFeatureNodes />
        </div>
      </div>
    </section>
  )
}
