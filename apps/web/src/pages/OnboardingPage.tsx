import React from 'react'
import { Lock } from 'lucide-react'
import { useAuth, useModals } from '@/context'
import { OnboardingWizard } from '@/components/onboarding'

export const OnboardingPage: React.FC = () => {
  const { isAuthenticated } = useAuth()
  const { openLoginModal } = useModals()

  if (!isAuthenticated) {
    return (
      <div className="min-h-[70vh] flex items-center justify-center academy-container py-16">
        <div className="max-w-md w-full bg-white rounded-3xl p-8 border border-slate-200/90 shadow-elevated text-center">
          <div className="w-16 h-16 rounded-2xl bg-brand-50 border border-brand-100 flex items-center justify-center mx-auto mb-4 text-brand-600">
            <Lock className="w-8 h-8" />
          </div>
          <h2 className="text-xl font-bold text-slate-900 mb-2">Student Onboarding Access</h2>
          <p className="text-xs text-slate-500 mb-6">
            Please log in with your Nexarats Academy student credentials to complete your academic profile, verify your identity, and access your enrolled curriculum.
          </p>
          <button
            onClick={openLoginModal}
            className="w-full py-3 bg-brand-600 hover:bg-brand-700 text-white font-bold text-sm rounded-xl transition-all shadow-button-glow cursor-pointer"
          >
            Sign In to Continue
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-[85vh] bg-[#f8fafc] py-8">
      <OnboardingWizard />
    </div>
  )
}

export default OnboardingPage
