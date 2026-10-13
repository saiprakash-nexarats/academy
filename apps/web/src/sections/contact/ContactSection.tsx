import React, { useState } from 'react'
import { Mail, Phone, MapPin, Clock, Send, CheckCircle2, MessageSquare } from 'lucide-react'
import { motion } from 'framer-motion'
import { CONTACT_INFO_DATA } from '@/data'
import { Button } from '@/components/ui/Button'
import { useModals } from '@/context'
import { cn } from '@/lib/utils'

interface ContactSectionProps {
  className?: string
}

export const ContactSection: React.FC<ContactSectionProps> = ({ className }) => {
  const { openDemoModal } = useModals()
  const [submitted, setSubmitted] = useState(false)
  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    phone: '',
    programOfInterest: 'Full Stack Development (6 Months)',
    message: '',
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    setSubmitted(true)
  }

  return (
    <section className={cn('py-8 sm:py-12', className)}>
      <div className="bg-white rounded-3xl border border-slate-200/90 shadow-elevated overflow-hidden">
        <div className="grid grid-cols-1 lg:grid-cols-12">
          {/* Left Column: Admissions Info */}
          <div className="lg:col-span-5 bg-blue-50/80 text-slate-800 p-8 sm:p-10 flex flex-col justify-between border-b lg:border-b-0 lg:border-r border-blue-200/80">
            <div className="space-y-6">
              <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-white text-brand-600 border border-blue-200/80 text-xs font-bold uppercase tracking-wider shadow-xs">
                <MessageSquare className="w-3.5 h-3.5" />
                <span>Admissions Office</span>
              </div>

              <div>
                <h3 className="font-heading font-extrabold text-2xl sm:text-3xl text-slate-900 tracking-tight">
                  Speak With an <br />
                  <span className="text-brand-600">Academic Counselor</span>
                </h3>
                <p className="text-xs sm:text-sm text-slate-600 mt-2 leading-relaxed">
                  Have questions regarding cohort schedules, tuition financing, scholarships, or syllabus deep dives? Our counselors are here to help.
                </p>
              </div>

              <div className="space-y-4 pt-2">
                <div className="flex items-start gap-3 text-xs sm:text-sm">
                  <div className="p-2 rounded-xl bg-white text-brand-600 border border-blue-200/70 shadow-xs shrink-0 mt-0.5">
                    <Phone className="w-4 h-4" />
                  </div>
                  <div>
                    <span className="text-slate-500 block text-[11px]">Direct Helpline</span>
                    <span className="font-bold text-slate-900">{CONTACT_INFO_DATA.office.phone}</span>
                  </div>
                </div>

                <div className="flex items-start gap-3 text-xs sm:text-sm">
                  <div className="p-2 rounded-xl bg-white text-brand-600 border border-blue-200/70 shadow-xs shrink-0 mt-0.5">
                    <Mail className="w-4 h-4" />
                  </div>
                  <div>
                    <span className="text-slate-500 block text-[11px]">Admissions Email</span>
                    <span className="font-bold text-slate-900">{CONTACT_INFO_DATA.office.email}</span>
                  </div>
                </div>

                <div className="flex items-start gap-3 text-xs sm:text-sm">
                  <div className="p-2 rounded-xl bg-white text-brand-600 border border-blue-200/70 shadow-xs shrink-0 mt-0.5">
                    <MapPin className="w-4 h-4" />
                  </div>
                  <div>
                    <span className="text-slate-500 block text-[11px]">Campus Location</span>
                    <span className="font-bold text-slate-900">{CONTACT_INFO_DATA.office.address}</span>
                  </div>
                </div>

                <div className="flex items-start gap-3 text-xs sm:text-sm">
                  <div className="p-2 rounded-xl bg-white text-brand-600 border border-blue-200/70 shadow-xs shrink-0 mt-0.5">
                    <Clock className="w-4 h-4" />
                  </div>
                  <div>
                    <span className="text-slate-500 block text-[11px]">Office Hours</span>
                    <span className="font-bold text-slate-900">{CONTACT_INFO_DATA.office.hours}</span>
                  </div>
                </div>
              </div>
            </div>

            <div className="pt-6 mt-6 border-t border-blue-200/60">
              <button
                type="button"
                onClick={openDemoModal}
                className="w-full py-2.5 px-4 rounded-xl bg-white hover:bg-blue-100 text-brand-700 border border-blue-200/80 font-bold text-xs transition-colors cursor-pointer text-center shadow-xs"
              >
                Schedule an Instant 1-on-1 Virtual Consultation
              </button>
            </div>
          </div>

          {/* Right Column: Inquiry Form */}
          <div className="lg:col-span-7 p-8 sm:p-10">
            {submitted ? (
              <motion.div
                initial={{ opacity: 0, scale: 0.95 }}
                animate={{ opacity: 1, scale: 1 }}
                className="py-12 text-center space-y-4"
              >
                <div className="w-16 h-16 rounded-full bg-emerald-100 text-emerald-600 flex items-center justify-center mx-auto">
                  <CheckCircle2 className="w-8 h-8" />
                </div>
                <h4 className="font-heading font-extrabold text-2xl text-slate-900">
                  Inquiry Received!
                </h4>
                <p className="text-sm text-slate-600 max-w-md mx-auto">
                  Thank you, <span className="font-bold text-slate-900">{formData.fullName}</span>. An academic advisor will reach out to you within 2 hours at <span className="font-bold text-slate-900">{formData.email}</span>.
                </p>
                <Button
                  variant="outline"
                  onClick={() => setSubmitted(false)}
                  className="mt-4"
                >
                  Send Another Inquiry
                </Button>
              </motion.div>
            ) : (
              <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                  <h3 className="font-heading font-bold text-xl sm:text-2xl text-slate-900">
                    Get in Touch
                  </h3>
                  <p className="text-xs sm:text-sm text-slate-500 mt-1">
                    Fill in your details below and an academic advisor will get back to you immediately.
                  </p>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2">
                  <div>
                    <label className="block text-xs font-bold text-slate-700 mb-1">
                      Full Name *
                    </label>
                    <input
                      type="text"
                      required
                      placeholder="e.g. Rahul Sharma"
                      value={formData.fullName}
                      onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                      className="w-full px-3.5 py-2.5 text-xs sm:text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-brand-500 focus:bg-white transition-all"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-bold text-slate-700 mb-1">
                      Email Address *
                    </label>
                    <input
                      type="email"
                      required
                      placeholder="e.g. rahul@example.com"
                      value={formData.email}
                      onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                      className="w-full px-3.5 py-2.5 text-xs sm:text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-brand-500 focus:bg-white transition-all"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-bold text-slate-700 mb-1">
                      Phone Number *
                    </label>
                    <input
                      type="tel"
                      required
                      placeholder="e.g. +91 98765 43210"
                      value={formData.phone}
                      onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                      className="w-full px-3.5 py-2.5 text-xs sm:text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-brand-500 focus:bg-white transition-all"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-bold text-slate-700 mb-1">
                      Program of Interest
                    </label>
                    <select
                      value={formData.programOfInterest}
                      onChange={(e) => setFormData({ ...formData, programOfInterest: e.target.value })}
                      className="w-full px-3.5 py-2.5 text-xs sm:text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-brand-500 focus:bg-white transition-all"
                    >
                      {CONTACT_INFO_DATA.inquiryPrograms.map((prog) => (
                        <option key={prog} value={prog}>
                          {prog}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Your Questions or Goals
                  </label>
                  <textarea
                    rows={3}
                    placeholder="Tell us about your learning objectives or career transition goals..."
                    value={formData.message}
                    onChange={(e) => setFormData({ ...formData, message: e.target.value })}
                    className="w-full px-3.5 py-2.5 text-xs sm:text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-brand-500 focus:bg-white transition-all resize-none"
                  />
                </div>

                <Button
                  type="submit"
                  variant="primary"
                  size="md"
                  iconRight={<Send className="w-4 h-4" />}
                  className="w-full sm:w-auto bg-brand-600 hover:bg-brand-700 font-bold px-8 py-3 rounded-xl shadow-button-glow"
                >
                  Submit Inquiry
                </Button>
              </form>
            )}
          </div>
        </div>
      </div>
    </section>
  )
}
