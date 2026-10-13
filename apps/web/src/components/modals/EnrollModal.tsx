import React, { useState, useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { X, CheckCircle2, GraduationCap, ArrowRight, AlertCircle, ShieldCheck } from 'lucide-react'
import { motion, AnimatePresence } from 'framer-motion'
import { Button } from '@/components/ui/Button'
import { POPULAR_PROGRAMS } from '@/data'
import { usePrograms, submitApplication, type ApplicationSummaryDto } from '@/lib/catalogApi'

const enrollSchema = z.object({
  fullName: z.string().min(2, 'Name must be at least 2 characters'),
  email: z.string().email('Please enter a valid email address'),
  phone: z.string().min(8, 'Please enter a valid phone number'),
  program: z.string().min(1, 'Please select a program'),
  experience: z.string().min(1, 'Please select your background'),
  statementOfPurpose: z.string().optional(),
})

type EnrollFormData = z.infer<typeof enrollSchema>

interface EnrollModalProps {
  isOpen: boolean
  onClose: () => void
  defaultProgram?: string
}

export const EnrollModal: React.FC<EnrollModalProps> = ({
  isOpen,
  onClose,
  defaultProgram,
}) => {
  const [submittedApplication, setSubmittedApplication] = useState<ApplicationSummaryDto | null>(null)
  const [apiError, setApiError] = useState<string | null>(null)
  const { data: catalogData } = usePrograms()

  const availablePrograms =
    catalogData?.items && catalogData.items.length > 0
      ? catalogData.items.map((p) => ({ id: p.slug, title: p.title, duration: p.durationLabel }))
      : POPULAR_PROGRAMS

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<EnrollFormData>({
    resolver: zodResolver(enrollSchema),
    defaultValues: {
      program: defaultProgram || 'full-stack',
      experience: 'beginner',
      statementOfPurpose: '',
    },
  })

  useEffect(() => {
    if (defaultProgram) {
      reset({
        fullName: '',
        email: '',
        phone: '',
        program: defaultProgram,
        experience: 'beginner',
        statementOfPurpose: '',
      })
    }
  }, [defaultProgram, reset])

  const onSubmit = async (data: EnrollFormData) => {
    setApiError(null)
    try {
      const response = await submitApplication({
        fullName: data.fullName,
        email: data.email,
        phone: data.phone,
        targetProgramOrCourse: data.program,
        experienceLevel: data.experience,
        statementOfPurpose: data.statementOfPurpose || undefined,
      })
      setSubmittedApplication(response)
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to submit application. Please check your details and try again.'
      setApiError(msg)
    }
  }

  const handleClose = () => {
    setSubmittedApplication(null)
    setApiError(null)
    reset()
    onClose()
  }

  if (!isOpen) return null

  return (
    <AnimatePresence>
      <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm">
        <motion.div
          initial={{ opacity: 0, scale: 0.95, y: 15 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          exit={{ opacity: 0, scale: 0.95, y: 15 }}
          className="relative w-full max-w-lg bg-white rounded-3xl shadow-2xl border border-slate-100 overflow-hidden"
        >
          {/* Close button */}
          <button
            type="button"
            onClick={handleClose}
            aria-label="Close modal"
            className="absolute top-5 right-5 p-2 rounded-xl text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition-colors z-10 cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>

          {submittedApplication ? (
            <div className="p-8 text-center space-y-4">
              <div className="w-16 h-16 rounded-2xl bg-emerald-50 text-emerald-600 border border-emerald-200 flex items-center justify-center mx-auto shadow-sm">
                <CheckCircle2 className="w-8 h-8" />
              </div>
              <h3 className="font-heading font-extrabold text-2xl text-slate-900">
                Application Received!
              </h3>

              <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-blue-50 border border-blue-200 text-brand-700 text-xs font-semibold">
                <ShieldCheck className="w-4 h-4 text-brand-600" />
                Ref: {submittedApplication.applicationNumber}
              </div>

              <div className="bg-slate-50 border border-slate-100 rounded-2xl p-4 text-left text-xs space-y-2 max-w-sm mx-auto">
                <div className="flex justify-between text-slate-500">
                  <span>Applicant:</span>
                  <span className="font-semibold text-slate-900">{submittedApplication.fullName}</span>
                </div>
                <div className="flex justify-between text-slate-500">
                  <span>Contact:</span>
                  <span className="font-semibold text-slate-900">{submittedApplication.email}</span>
                </div>
                <div className="flex justify-between text-slate-500">
                  <span>Status:</span>
                  <span className="font-bold text-brand-600 uppercase tracking-wider">{submittedApplication.status}</span>
                </div>
              </div>

              <p className="text-sm text-slate-600 max-w-sm mx-auto leading-relaxed">
                Thank you for applying to Nexarats Academy. Our admissions committee is reviewing your application. You will receive an offer notification and seat reservation details shortly.
              </p>

              <div className="pt-2">
                <Button variant="primary" onClick={handleClose} className="w-full py-3">
                  Return to Academy
                </Button>
              </div>
            </div>
          ) : (
            <div className="p-6 sm:p-8 space-y-6">
              {/* Header */}
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-brand-600 text-white flex items-center justify-center shadow-sm">
                  <GraduationCap className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="font-heading font-extrabold text-xl text-slate-900">
                    Apply to Nexarats Academy
                  </h3>
                  <p className="text-xs text-slate-500">
                    Submit your application for verified cohort admission and seat reservation.
                  </p>
                </div>
              </div>

              {/* API Error Notification */}
              {apiError && (
                <div className="p-3.5 rounded-xl bg-rose-50 border border-rose-200 flex items-start gap-3 text-rose-700 text-xs">
                  <AlertCircle className="w-4 h-4 shrink-0 mt-0.5 text-rose-500" />
                  <p className="leading-relaxed">{apiError}</p>
                </div>
              )}

              {/* Form */}
              <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Full Name *
                  </label>
                  <input
                    type="text"
                    placeholder="e.g. Ananya Sharma"
                    {...register('fullName')}
                    className="w-full px-3.5 py-2.5 text-xs sm:text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-all"
                  />
                  {errors.fullName && (
                    <p className="text-rose-500 text-[11px] mt-1">{errors.fullName.message}</p>
                  )}
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
                  <div>
                    <label className="block text-xs font-bold text-slate-700 mb-1">
                      Email Address *
                    </label>
                    <input
                      type="email"
                      placeholder="you@domain.com"
                      {...register('email')}
                      className="w-full px-3.5 py-2.5 text-xs sm:text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-all"
                    />
                    {errors.email && (
                      <p className="text-rose-500 text-[11px] mt-1">{errors.email.message}</p>
                    )}
                  </div>

                  <div>
                    <label className="block text-xs font-bold text-slate-700 mb-1">
                      Phone Number *
                    </label>
                    <input
                      type="tel"
                      placeholder="+1 (555) 000-0000"
                      {...register('phone')}
                      className="w-full px-3.5 py-2.5 text-xs sm:text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-all"
                    />
                    {errors.phone && (
                      <p className="text-rose-500 text-[11px] mt-1">{errors.phone.message}</p>
                    )}
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Target Program or Course *
                  </label>
                  <select
                    {...register('program')}
                    className="w-full px-3.5 py-2.5 text-xs sm:text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-all"
                  >
                    {availablePrograms.map((p) => (
                      <option key={p.id} value={p.id}>
                        {p.title} ({p.duration})
                      </option>
                    ))}
                  </select>
                  {errors.program && (
                    <p className="text-rose-500 text-[11px] mt-1">{errors.program.message}</p>
                  )}
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Current Experience Level *
                  </label>
                  <select
                    {...register('experience')}
                    className="w-full px-3.5 py-2.5 text-xs sm:text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-all"
                  >
                    <option value="beginner">Complete Beginner (No coding experience)</option>
                    <option value="intermediate">Self-taught / University student</option>
                    <option value="advanced">Working professional transitioning roles</option>
                  </select>
                </div>

                <div className="pt-2">
                  <Button
                    type="submit"
                    variant="primary"
                    isLoading={isSubmitting}
                    iconRight={<ArrowRight className="w-4 h-4" />}
                    className="w-full py-3.5 font-bold shadow-button-glow"
                  >
                    Submit Application
                  </Button>
                </div>

                <p className="text-center text-[11px] text-slate-400">
                  By submitting, you agree to Nexarats Academy admissions policies. No payment is required until formal admission offer issuance.
                </p>
              </form>
            </div>
          )}
        </motion.div>
      </div>
    </AnimatePresence>
  )
}
