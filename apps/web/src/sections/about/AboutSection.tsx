import React from 'react'
import { Link } from 'react-router-dom'
import { Sparkles, ArrowRight } from 'lucide-react'
import { motion } from 'framer-motion'
import { COMPANY_ABOUT_DATA } from '@/data'
import { cn } from '@/lib/utils'

interface AboutSectionProps {
  className?: string
}

export const AboutSection: React.FC<AboutSectionProps> = ({ className }) => {
  return (
    <section className={cn('py-8 sm:py-12', className)}>
      <div className="rounded-3xl bg-blue-50/80 text-slate-800 p-6 sm:p-10 lg:p-12 border border-blue-200/80 shadow-sm relative overflow-hidden">
        <div className="relative z-10 grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-12 items-center">
          {/* Left Column: Story & Mission */}
          <div className="lg:col-span-7 space-y-6">
            <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-white text-brand-600 border border-blue-200/80 text-xs font-bold uppercase tracking-wider shadow-xs">
              <Sparkles className="w-3.5 h-3.5" />
              <span>About Nexarats Academy</span>
            </div>

            <h2 className="font-heading font-extrabold text-2xl sm:text-3xl lg:text-4xl text-slate-900 tracking-tight leading-tight">
              Shaping the Next Generation of <span className="text-brand-600">Global Tech Leaders</span>
            </h2>

            <p className="text-sm sm:text-base text-slate-600 leading-relaxed max-w-xl">
              {COMPANY_ABOUT_DATA.mission}
            </p>

            {/* Core Pillars List */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5 pt-2">
              {COMPANY_ABOUT_DATA.pillars.map((pillar) => {
                const IconComponent = pillar.icon
                return (
                  <div
                    key={pillar.title}
                    className="flex items-start gap-3 p-3 rounded-xl bg-white border border-blue-200/70 shadow-xs"
                  >
                    <div className="p-2 rounded-lg bg-blue-50 text-brand-600 shrink-0">
                      <IconComponent className="w-4 h-4" />
                    </div>
                    <div>
                      <h4 className="font-bold text-xs sm:text-sm text-slate-900">{pillar.title}</h4>
                      <p className="text-[11px] text-slate-600 leading-snug mt-0.5">{pillar.description}</p>
                    </div>
                  </div>
                )
              })}
            </div>

            <div className="pt-2">
              <Link
                to="/about"
                className="group inline-flex items-center gap-2 px-5 py-2.5 rounded-full bg-brand-600 hover:bg-brand-700 text-white font-bold text-xs sm:text-sm transition-all shadow-button-glow hover:shadow-lg cursor-pointer active:scale-95"
              >
                <span>Discover Our Complete Story</span>
                <ArrowRight className="w-3.5 h-3.5 transition-transform group-hover:translate-x-1" />
              </Link>
            </div>
          </div>

          {/* Right Column: Key Stats Card */}
          <div className="lg:col-span-5 grid grid-cols-2 gap-4">
            {COMPANY_ABOUT_DATA.stats.map((s, idx) => (
              <motion.div
                key={s.label}
                initial={{ opacity: 0, scale: 0.95 }}
                whileInView={{ opacity: 1, scale: 1 }}
                viewport={{ once: true }}
                transition={{ duration: 0.3, delay: idx * 0.05 }}
                className="p-5 rounded-2xl bg-white border border-blue-200/70 shadow-xs text-center flex flex-col justify-center"
              >
                <div className="font-heading font-black text-2xl sm:text-3xl text-brand-600">
                  {s.value}
                </div>
                <div className="font-bold text-xs sm:text-sm text-slate-900 mt-1">
                  {s.label}
                </div>
                <div className="text-[11px] text-slate-500 mt-0.5">
                  {s.subtext}
                </div>
              </motion.div>
            ))}
          </div>
        </div>
      </div>
    </section>
  )
}
