import React from 'react'
import { motion } from 'framer-motion'
import { CONNECTED_FEATURE_NODES } from '@/data'
import { cn } from '@/lib/utils'

interface ConnectedFeatureNodesProps {
  className?: string
}

export const ConnectedFeatureNodes: React.FC<ConnectedFeatureNodesProps> = ({ className }) => {
  return (
    <div className={cn('relative flex items-center w-full', className)}>
      {/* Curved / Dotted Connector SVG Diagram (Desktop XL) */}
      <div className="hidden xl:block absolute -left-12 top-0 bottom-0 w-12 pointer-events-none">
        <svg
          className="w-full h-full text-blue-300"
          viewBox="0 0 48 320"
          fill="none"
          preserveAspectRatio="none"
        >
          {/* Main Trunk Line */}
          <path
            d="M0 160 C20 160, 20 40, 48 40"
            stroke="#93C5FD"
            strokeWidth="1.5"
            strokeDasharray="3 3"
          />
          <path
            d="M0 160 C20 160, 20 120, 48 120"
            stroke="#93C5FD"
            strokeWidth="1.5"
            strokeDasharray="3 3"
          />
          <path
            d="M0 160 C20 160, 20 200, 48 200"
            stroke="#93C5FD"
            strokeWidth="1.5"
            strokeDasharray="3 3"
          />
          <path
            d="M0 160 C20 160, 20 280, 48 280"
            stroke="#93C5FD"
            strokeWidth="1.5"
            strokeDasharray="3 3"
          />
          {/* Connector Node Circles */}
          <circle cx="48" cy="40" r="3" fill="#2563EB" />
          <circle cx="48" cy="120" r="3" fill="#2563EB" />
          <circle cx="48" cy="200" r="3" fill="#2563EB" />
          <circle cx="48" cy="280" r="3" fill="#2563EB" />
          <circle cx="0" cy="160" r="4" fill="#2563EB" />
        </svg>
      </div>

      {/* 4 Feature Mini-Cards: 2-column on tablet, single-column on mobile & desktop */}
      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-1 gap-3 sm:gap-3.5 w-full">
        {CONNECTED_FEATURE_NODES.map((node, index) => {
          const Icon = node.icon
          return (
            <motion.div
              key={node.id}
              initial={{ opacity: 0, x: 15 }}
              whileInView={{ opacity: 1, x: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.4, delay: index * 0.08 }}
              whileHover={{ x: 4, transition: { duration: 0.15 } }}
              className="bg-white/95 hover:bg-white hover:border-brand-300 hover:shadow-card-hover border border-slate-200/90 rounded-2xl p-3.5 flex items-center gap-3.5 shadow-sm transition-all cursor-pointer group"
            >
              <div className="w-10 h-10 rounded-xl bg-blue-50 text-brand-600 group-hover:bg-brand-600 group-hover:text-white border border-blue-100 group-hover:border-brand-600 flex items-center justify-center shrink-0 transition-colors duration-200 shadow-sm">
                <Icon className="w-5 h-5" />
              </div>

              <div className="min-w-0">
                <h4 className="font-heading font-bold text-xs sm:text-sm text-slate-900 group-hover:text-brand-600 transition-colors truncate">
                  {node.title}
                </h4>
                <p className="text-[11px] text-slate-500 mt-0.5 truncate">
                  {node.description}
                </p>
              </div>
            </motion.div>
          )
        })}
      </div>
    </div>
  )
}
