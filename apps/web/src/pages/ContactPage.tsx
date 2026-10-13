import React from 'react'
import { MessageSquare, HelpCircle } from 'lucide-react'
import { Badge } from '@/components/ui/Badge'
import { ContactSection } from '@/sections/contact/ContactSection'
import { FAQ_DATA } from '@/data'

export const ContactPage: React.FC = () => {
  return (
    <div className="py-12 sm:py-16">
      <div className="academy-container space-y-12">
        {/* Page Header */}
        <div className="max-w-3xl space-y-4">
          <Badge variant="primary" icon={<MessageSquare className="w-3.5 h-3.5 text-brand-600" />}>
            Admissions & Student Advisory
          </Badge>
          <h1 className="font-heading font-extrabold text-3xl sm:text-4xl lg:text-5xl text-slate-900 tracking-tight">
            We Are Here to Guide Your <span className="text-brand-600">Career Transformation</span>
          </h1>
          <p className="text-sm sm:text-base text-slate-600 leading-relaxed">
            Have questions about syllabus, mentor pairings, scholarship evaluations, or upcoming cohort dates? Reach out directly to our admissions team.
          </p>
        </div>

        {/* Contact Form & Office Cards */}
        <ContactSection />

        {/* Admissions FAQs */}
        <div className="space-y-6 pt-6">
          <div className="flex items-center gap-2">
            <HelpCircle className="w-5 h-5 text-brand-600" />
            <h2 className="text-2xl sm:text-3xl font-heading font-extrabold text-slate-900">
              Frequently Asked Admissions Questions
            </h2>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            {FAQ_DATA.map((faq, i) => (
              <div
                key={i}
                className="p-5 rounded-2xl bg-white border border-slate-200/90 shadow-sm space-y-2"
              >
                <h4 className="font-bold text-sm text-slate-900">{faq.q}</h4>
                <p className="text-xs sm:text-sm text-slate-600 leading-relaxed">{faq.a}</p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}
