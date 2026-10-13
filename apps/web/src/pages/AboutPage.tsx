import React from 'react'
import { Globe } from 'lucide-react'
import { COMPANY_ABOUT_DATA, FAQ_DATA } from '@/data'
import { Badge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { useModals } from '@/context'

export const AboutPage: React.FC = () => {
  const { openEnrollModal, openDemoModal } = useModals()

  const methodologySteps = [
    {
      step: '01',
      title: 'Cohort-Based Interactive Lectures',
      desc: 'Attend live online weekend and evening sessions led by senior engineers from Big Tech and tier-1 hypergrowth startups.',
    },
    {
      step: '02',
      title: 'Production-Grade Capstone Projects',
      desc: 'Build scalable architectures from day one using modern tech stacks, automated CI/CD pipelines, and best-practice patterns.',
    },
    {
      step: '03',
      title: '1:1 Weekly Code & Design Reviews',
      desc: 'Receive direct pull-request line comments and weekly video teardowns from dedicated personal mentors.',
    },
    {
      step: '04',
      title: 'Hiring Pipeline & Mock Interviews',
      desc: 'Get fast-tracked into technical recruitment pipelines across our network of 500+ global hiring partner enterprises.',
    },
  ]

  return (
    <div className="py-12 sm:py-16">
      <div className="academy-container space-y-16">
        {/* Header Hero */}
        <div className="max-w-3xl space-y-4">
          <Badge variant="primary" icon={<Globe className="w-3.5 h-3.5 text-brand-600" />}>
            Empowering Modern Builders
          </Badge>
          <h1 className="font-heading font-extrabold text-3xl sm:text-4xl lg:text-5xl text-slate-900 tracking-tight">
            We Are Building the <span className="text-brand-600">Ivy League of Tech Skills</span>
          </h1>
          <p className="text-sm sm:text-base text-slate-600 leading-relaxed">
            {COMPANY_ABOUT_DATA.mission}
          </p>
        </div>

        {/* Global Impact Numbers */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-5">
          {COMPANY_ABOUT_DATA.stats.map((stat) => (
            <div
              key={stat.label}
              className="p-6 rounded-2xl bg-white border border-slate-200/90 shadow-card text-center"
            >
              <div className="font-heading font-black text-3xl sm:text-4xl text-brand-600">
                {stat.value}
              </div>
              <div className="font-bold text-sm text-slate-800 mt-1">{stat.label}</div>
              <div className="text-xs text-slate-500 mt-0.5">{stat.subtext}</div>
            </div>
          ))}
        </div>

        {/* The 4 Pillars */}
        <div className="space-y-6">
          <div>
            <h2 className="text-2xl sm:text-3xl font-heading font-extrabold text-slate-900">
              Our Core Educational Pillars
            </h2>
            <p className="text-sm text-slate-600 mt-1">
              Every curriculum is crafted with uncompromising rigor and industry precision.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {COMPANY_ABOUT_DATA.pillars.map((pillar) => {
              const IconComp = pillar.icon
              return (
                <div
                  key={pillar.title}
                  className="p-6 rounded-2xl bg-white border border-slate-200/90 shadow-card flex items-start gap-4"
                >
                  <div className="p-3 rounded-xl bg-brand-50 text-brand-600 shrink-0">
                    <IconComp className="w-6 h-6" />
                  </div>
                  <div>
                    <h3 className="font-heading font-bold text-lg text-slate-900 mb-1">
                      {pillar.title}
                    </h3>
                    <p className="text-xs sm:text-sm text-slate-600 leading-relaxed">
                      {pillar.description}
                    </p>
                  </div>
                </div>
              )
            })}
          </div>
        </div>

        {/* Our Methodology */}
        <div className="p-8 sm:p-12 rounded-3xl bg-blue-50/70 border border-blue-200/80 shadow-sm text-slate-800 space-y-8">
          <div>
            <div className="inline-block px-3 py-1 rounded-full bg-white text-brand-600 border border-blue-200/80 text-xs font-bold uppercase tracking-wider mb-2 shadow-xs">
              The Nexarats Pathway
            </div>
            <h2 className="text-2xl sm:text-3xl font-heading font-extrabold text-slate-900">
              How You Master Skills in 6 Months
            </h2>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {methodologySteps.map((step) => (
              <div key={step.step} className="p-5 rounded-2xl bg-white border border-blue-200/70 shadow-sm space-y-2 hover:border-brand-400 transition-colors">
                <span className="font-heading font-black text-2xl text-brand-600 block">{step.step}</span>
                <h4 className="font-bold text-sm text-slate-900">{step.title}</h4>
                <p className="text-xs text-slate-600 leading-relaxed">{step.desc}</p>
              </div>
            ))}
          </div>
        </div>

        {/* FAQs */}
        <div className="space-y-6">
          <h2 className="text-2xl sm:text-3xl font-heading font-extrabold text-slate-900">
            Frequently Asked Questions
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            {FAQ_DATA.map((faq, i) => (
              <div key={i} className="p-5 rounded-2xl bg-white border border-slate-200/90 shadow-sm space-y-2">
                <h4 className="font-bold text-sm text-slate-900">{faq.q}</h4>
                <p className="text-xs sm:text-sm text-slate-600 leading-relaxed">{faq.a}</p>
              </div>
            ))}
          </div>
        </div>

        {/* CTA */}
        <div className="p-8 rounded-3xl bg-brand-50 border border-brand-200/80 flex flex-col sm:flex-row items-center justify-between gap-6 text-center sm:text-left">
          <div className="space-y-1">
            <h2 className="text-xl sm:text-2xl font-heading font-extrabold text-slate-900">
              Transform your career trajectory today
            </h2>
            <p className="text-xs sm:text-sm text-slate-600">
              Join the next upcoming cohort or attend a live admissions workshop.
            </p>
          </div>
          <div className="flex gap-3">
            <Button
              variant="primary"
              size="lg"
              onClick={() => openEnrollModal()}
              className="bg-brand-600 hover:bg-brand-700 font-bold rounded-xl shadow-button-glow"
            >
              Enroll in Cohort
            </Button>
            <button
              type="button"
              onClick={() => openDemoModal()}
              className="px-5 py-3 rounded-xl border border-slate-300 font-bold text-sm text-slate-700 hover:bg-white"
            >
              Book a Demo
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
