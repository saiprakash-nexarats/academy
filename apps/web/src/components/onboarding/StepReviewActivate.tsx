import React, { useState } from 'react'
import {
  CheckCircle2,
  AlertCircle,
  Sparkles,
  Rocket,
  ShieldCheck,
  User,
  Link2,
  FileCheck2,
} from 'lucide-react'
import type { CompleteOnboardingResponse, LegalAgreementDto } from '@nexarats/api-contracts'
import { completeOnboarding } from '@/lib/onboardingApi'
import type { StudentInfoData } from './StepStudentInfo'
import type { DeveloperProfileData } from './StepDeveloperProfile'

interface StepReviewActivateProps {
  studentInfo: StudentInfoData
  devProfile: DeveloperProfileData
  avatarUrl: string | null
  agreements: LegalAgreementDto[]
  token?: string | null
  onActivated: (resp: CompleteOnboardingResponse) => void
  onJumpToStep: (step: number) => void
}

export const StepReviewActivate: React.FC<StepReviewActivateProps> = ({
  studentInfo,
  devProfile,
  avatarUrl,
  agreements,
  token,
  onActivated,
  onJumpToStep,
}) => {
  const [isActivating, setIsActivating] = useState(false)
  const [activationError, setActivationError] = useState<string | null>(null)

  const isProfileValid = Boolean(studentInfo.headline.trim() && studentInfo.bio.trim())
  const isDevLinkValid = Boolean(devProfile.githubUrl.trim() || devProfile.linkedinUrl.trim())
  const isAvatarValid = Boolean(avatarUrl)
  const areAgreementsValid = agreements.length > 0 && agreements.every((a) => a.isAccepted)

  const canActivate = isProfileValid && isDevLinkValid && isAvatarValid && areAgreementsValid

  const handleActivate = async () => {
    if (!canActivate || isActivating) return

    setActivationError(null)
    setIsActivating(true)
    try {
      const resp = await completeOnboarding(token)
      onActivated(resp)
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Activation failed'
      setActivationError(message)
    } finally {
      setIsActivating(false)
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <div className="flex items-center gap-2 mb-1">
          <div className="w-8 h-8 rounded-lg bg-brand-50 border border-brand-100 flex items-center justify-center text-brand-600">
            <Rocket className="w-4 h-4" />
          </div>
          <h3 className="text-lg font-bold text-slate-900">Review & Activate Student Profile</h3>
        </div>
        <p className="text-xs text-slate-500">
          Review your onboarding information below. Once verified, activate your student account to unlock the full course LMS, interactive syllabus, and video lectures.
        </p>
      </div>

      {/* Validation Checklist Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        {/* Step 1 Check */}
        <div
          onClick={() => onJumpToStep(1)}
          className={`p-3.5 rounded-xl border transition-all cursor-pointer flex items-center justify-between ${
            isProfileValid
              ? 'bg-emerald-50/50 border-emerald-200 text-emerald-900 hover:bg-emerald-50'
              : 'bg-rose-50/50 border-rose-200 text-rose-900 hover:bg-rose-50'
          }`}
        >
          <div className="flex items-center gap-2.5">
            <User className="w-4 h-4 shrink-0 text-slate-600" />
            <div>
              <div className="text-xs font-bold">1. Personal Profile</div>
              <div className="text-[11px] text-slate-500">
                {isProfileValid ? 'Headline and bio complete' : 'Headline or bio missing'}
              </div>
            </div>
          </div>
          {isProfileValid ? (
            <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
          ) : (
            <AlertCircle className="w-4 h-4 text-rose-600 shrink-0" />
          )}
        </div>

        {/* Step 2 Check */}
        <div
          onClick={() => onJumpToStep(2)}
          className={`p-3.5 rounded-xl border transition-all cursor-pointer flex items-center justify-between ${
            isDevLinkValid
              ? 'bg-emerald-50/50 border-emerald-200 text-emerald-900 hover:bg-emerald-50'
              : 'bg-rose-50/50 border-rose-200 text-rose-900 hover:bg-rose-50'
          }`}
        >
          <div className="flex items-center gap-2.5">
            <Link2 className="w-4 h-4 shrink-0 text-slate-600" />
            <div>
              <div className="text-xs font-bold">2. Developer Links</div>
              <div className="text-[11px] text-slate-500">
                {isDevLinkValid ? 'GitHub or LinkedIn linked' : 'No developer links provided'}
              </div>
            </div>
          </div>
          {isDevLinkValid ? (
            <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
          ) : (
            <AlertCircle className="w-4 h-4 text-rose-600 shrink-0" />
          )}
        </div>

        {/* Step 3 Check */}
        <div
          onClick={() => onJumpToStep(3)}
          className={`p-3.5 rounded-xl border transition-all cursor-pointer flex items-center justify-between ${
            isAvatarValid
              ? 'bg-emerald-50/50 border-emerald-200 text-emerald-900 hover:bg-emerald-50'
              : 'bg-rose-50/50 border-rose-200 text-rose-900 hover:bg-rose-50'
          }`}
        >
          <div className="flex items-center gap-2.5">
            <ShieldCheck className="w-4 h-4 shrink-0 text-slate-600" />
            <div>
              <div className="text-xs font-bold">3. Profile Avatar</div>
              <div className="text-[11px] text-slate-500">
                {isAvatarValid ? 'Verified avatar uploaded' : 'Photo required'}
              </div>
            </div>
          </div>
          {isAvatarValid ? (
            <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
          ) : (
            <AlertCircle className="w-4 h-4 text-rose-600 shrink-0" />
          )}
        </div>

        {/* Step 4 Check */}
        <div
          onClick={() => onJumpToStep(4)}
          className={`p-3.5 rounded-xl border transition-all cursor-pointer flex items-center justify-between ${
            areAgreementsValid
              ? 'bg-emerald-50/50 border-emerald-200 text-emerald-900 hover:bg-emerald-50'
              : 'bg-rose-50/50 border-rose-200 text-rose-900 hover:bg-rose-50'
          }`}
        >
          <div className="flex items-center gap-2.5">
            <FileCheck2 className="w-4 h-4 shrink-0 text-slate-600" />
            <div>
              <div className="text-xs font-bold">4. Legal Agreements</div>
              <div className="text-[11px] text-slate-500">
                {areAgreementsValid ? 'All 3 agreements signed' : 'Pending signatures'}
              </div>
            </div>
          </div>
          {areAgreementsValid ? (
            <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
          ) : (
            <AlertCircle className="w-4 h-4 text-rose-600 shrink-0" />
          )}
        </div>
      </div>

      {/* Summary Profile Card */}
      <div className="p-5 bg-white border border-slate-200/90 rounded-2xl shadow-card space-y-4">
        <div className="flex items-center gap-4">
          {avatarUrl ? (
            <img
              src={avatarUrl}
              alt="Avatar preview"
              className="w-16 h-16 rounded-full object-cover border-2 border-brand-500 shadow-xs"
            />
          ) : (
            <div className="w-16 h-16 rounded-full bg-slate-100 flex items-center justify-center text-slate-400 border border-slate-200">
              <User className="w-8 h-8" />
            </div>
          )}
          <div>
            <h4 className="text-sm font-bold text-slate-900">
              {studentInfo.headline || 'Headline not provided'}
            </h4>
            <p className="text-xs text-slate-500 mt-0.5">Timezone: {studentInfo.timezone || 'UTC'}</p>
            <div className="flex items-center gap-3 mt-1.5 text-xs text-brand-600">
              {devProfile.githubUrl && <span>GitHub: {devProfile.githubUrl}</span>}
              {devProfile.linkedinUrl && <span>LinkedIn: {devProfile.linkedinUrl}</span>}
            </div>
          </div>
        </div>

        {studentInfo.bio && (
          <div className="p-3 bg-slate-50 rounded-xl border border-slate-100 text-xs text-slate-600 italic">
            "{studentInfo.bio}"
          </div>
        )}
      </div>

      {/* Activation Error Display */}
      {activationError && (
        <div className="p-3.5 bg-rose-50 border border-rose-200 text-rose-800 rounded-xl text-xs flex items-center gap-2">
          <AlertCircle className="w-4 h-4 text-rose-600 shrink-0" />
          <span>{activationError}</span>
        </div>
      )}

      {/* Activation Action */}
      <div className="pt-2">
        <button
          type="button"
          onClick={handleActivate}
          disabled={!canActivate || isActivating}
          className={`w-full py-3.5 px-6 rounded-xl font-bold text-sm shadow-button-glow transition-all flex items-center justify-center gap-2 cursor-pointer ${
            canActivate && !isActivating
              ? 'bg-brand-600 hover:bg-brand-700 text-white'
              : 'bg-slate-200 text-slate-400 cursor-not-allowed'
          }`}
        >
          {isActivating ? (
            <>
              <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              Activating Student Profile & Enrolling...
            </>
          ) : (
            <>
              <Sparkles className="w-4 h-4" />
              Activate Student Profile & Start Learning
            </>
          )}
        </button>
        {!canActivate && (
          <p className="mt-2 text-center text-[11px] text-slate-400">
            Please complete all checklist items above before activating your student profile.
          </p>
        )}
      </div>
    </div>
  )
}
