import React from 'react'
import { Link } from 'react-router-dom'
import {
  ShieldCheck,
  Building2,
  Users,
  Code2,
  TrendingUp,
  ArrowRight,
  CheckCircle2,
  Sparkles,
} from 'lucide-react'
import { motion } from 'framer-motion'
import { useModals } from '@/context'
import { CompanyIcon } from '@/components/common'
import { HIRING_PARTNERS_LIST } from '@/data'
import { cn } from '@/lib/utils'

interface JobAssuranceSectionProps {
  className?: string
}

export const JobAssuranceSection: React.FC<JobAssuranceSectionProps> = ({ className }) => {
  const { openDemoModal } = useModals()

  const placementPillars = [
    {
      id: 'hiring-network',
      icon: Building2,
      title: '500+ Active Hiring Partners',
      description:
        'Direct tie-ups with global tech leaders and high-growth unicorns. Skip the regular applicant queue with exclusive campus drives.',
      tag: 'Direct Referrals',
      stat: '500+ Tech Partners',
    },
    {
      id: 'mock-crucible',
      icon: Users,
      title: '1:1 Big Tech Mock Interviews',
      description:
        'Practice live coding, system design, and behavioral STAR rounds with staff engineers from Google, Microsoft, and Amazon until you are 100% ready.',
      tag: '5+ Mock Sessions',
      stat: 'Big Tech Interviewers',
    },
    {
      id: 'capstone-portfolio',
      icon: Code2,
      title: 'ATS-Optimized Portfolios',
      description:
        'Build production-grade SaaS and AI capstone projects with verified GitHub commits, CI/CD pipelines, and cloud deployments that recruiters love.',
      tag: 'Verified Projects',
      stat: 'Production Codebases',
    },
    {
      id: 'interview-calls',
      icon: TrendingUp,
      title: 'Unlimited Interview Calls',
      description:
        'We arrange continuous interview drives until you accept an offer, accompanied by professional salary negotiation to help you lock in the highest package.',
      tag: 'Assured Support',
      stat: 'Avg ₹14.5 LPA CTC',
    },
  ]

  // Extended list with additional top tech enterprises
  const featuredCompanies = [
    ...HIRING_PARTNERS_LIST,
    { name: 'Meta', color: '#0668E1' },
    { name: 'Apple', color: '#000000' },
    { name: 'Netflix', color: '#E50914' },
  ]

  return (
    <section className={cn('py-1 sm:py-2', className)}>
      <motion.div
        initial={{ opacity: 0, y: 15 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true }}
        transition={{ duration: 0.5 }}
        className="bg-blue-50/70 border border-blue-200/80 rounded-3xl p-6 sm:p-8 lg:p-10 shadow-card"
      >
        {/* Section Header */}
        <div className="flex flex-col lg:flex-row lg:items-end justify-between gap-5 mb-8">
          <div className="max-w-2xl space-y-3">
            <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-white text-brand-700 border border-blue-200 text-xs font-bold tracking-wide shadow-xs select-none">
              <ShieldCheck className="w-4 h-4 text-emerald-600" />
              <span>100% Job Assurance Guarantee</span>
            </div>

            <h2 className="font-heading font-extrabold text-2xl sm:text-3xl lg:text-4xl text-slate-900 tracking-tight leading-tight">
              We Don&apos;t Just Teach —{' '}
              <span className="text-brand-600">We Place You Surely</span>
            </h2>

            <p className="text-sm sm:text-base text-slate-600 leading-relaxed">
              Every enrolled student receives our full-fledged placement ecosystem, guaranteed interview
              opportunities, and dedicated career guidance until you sign your offer letter.
            </p>
          </div>

          {/* Quick CTA */}
          <div className="shrink-0 flex items-center gap-3">
            <button
              type="button"
              onClick={openDemoModal}
              className="inline-flex items-center gap-2 px-5 py-3 rounded-full bg-brand-600 hover:bg-brand-700 text-white font-bold text-xs sm:text-sm shadow-button-glow hover:shadow-lg active:scale-95 transition-all cursor-pointer"
            >
              <span>Check Placement Eligibility</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* 4 Placement Pillars Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-5 mb-8">
          {placementPillars.map((pillar, idx) => {
            const Icon = pillar.icon
            return (
              <motion.div
                key={pillar.id}
                initial={{ opacity: 0, y: 15 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.4, delay: idx * 0.08 }}
                whileHover={{ y: -4, transition: { duration: 0.15 } }}
                className="bg-white rounded-2xl border border-slate-200/90 p-5 shadow-xs hover:shadow-card hover:border-brand-300 transition-all flex flex-col justify-between"
              >
                <div>
                  <div className="flex items-center justify-between mb-3.5">
                    <div className="w-10 h-10 rounded-xl bg-blue-50 border border-blue-100 flex items-center justify-center text-brand-600">
                      <Icon className="w-5 h-5" />
                    </div>
                    <span className="text-[11px] font-bold text-emerald-700 bg-emerald-50 border border-emerald-200/80 px-2 py-0.5 rounded-md">
                      {pillar.tag}
                    </span>
                  </div>

                  <h3 className="font-heading font-bold text-base text-slate-900 mb-2 leading-snug">
                    {pillar.title}
                  </h3>

                  <p className="text-xs text-slate-600 leading-relaxed mb-4">
                    {pillar.description}
                  </p>
                </div>

                <div className="pt-3 border-t border-slate-100 flex items-center gap-1.5 text-slate-700 text-xs font-semibold">
                  <CheckCircle2 className="w-3.5 h-3.5 text-brand-600 shrink-0" />
                  <span>{pillar.stat}</span>
                </div>
              </motion.div>
            )
          })}
        </div>

        {/* Hiring Partners Strip with Real Company Icons */}
        <div className="bg-white rounded-2xl border border-blue-200/60 p-4 sm:p-5 shadow-xs">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-3 mb-3.5">
            <div className="flex items-center gap-2">
              <Sparkles className="w-4 h-4 text-brand-600 shrink-0" />
              <span className="text-xs font-bold text-slate-800 uppercase tracking-wider">
                Top Hiring Partners Recruiting Nexarats Graduates
              </span>
            </div>
            <Link
              to="/success-stories"
              className="text-xs font-bold text-brand-600 hover:text-brand-700 inline-flex items-center gap-1 transition-colors"
            >
              <span>View Placed Alumni Stories</span>
              <ArrowRight className="w-3 h-3" />
            </Link>
          </div>

          <div className="flex flex-wrap items-center gap-2.5 sm:gap-3">
            {featuredCompanies.map((partner) => (
              <div
                key={partner.name}
                className="inline-flex items-center gap-2 px-3.5 py-2 rounded-xl bg-slate-50 hover:bg-white border border-slate-200/80 hover:border-brand-300 text-xs font-bold text-slate-800 shadow-2xs hover:shadow-xs transition-all select-none cursor-default"
              >
                <CompanyIcon name={partner.name} className="w-4 h-4 shrink-0" />
                <span className="font-heading tracking-tight">{partner.name}</span>
              </div>
            ))}
            <div className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-blue-50 border border-blue-200 text-xs font-bold text-brand-700 shadow-2xs">
              <Building2 className="w-3.5 h-3.5 text-brand-600" />
              <span>+500 More Tech Partners</span>
            </div>
          </div>
        </div>
      </motion.div>
    </section>
  )
}
