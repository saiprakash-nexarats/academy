import React from 'react'
import { motion } from 'framer-motion'
import { PlatformSidebar } from './PlatformSidebar'
import { PlatformMainContent } from './PlatformMainContent'
import { cn } from '@/lib/utils'

interface PlatformCardProps {
  className?: string
}

export const PlatformCard: React.FC<PlatformCardProps> = ({ className }) => {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true }}
      transition={{ duration: 0.6 }}
      className={cn(
        'rounded-3xl border border-slate-200/90 shadow-elevated bg-white overflow-hidden flex flex-col md:flex-row relative z-10',
        className
      )}
    >
      {/* Dark Sidebar */}
      <PlatformSidebar />

      {/* Main White Content Area */}
      <PlatformMainContent />
    </motion.div>
  )
}
