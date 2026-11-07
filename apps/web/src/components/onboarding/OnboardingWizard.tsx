import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  ArrowRight,
  ArrowLeft,
  Save,
  GraduationCap,
  Sparkles,
} from 'lucide-react'
import type {
  LegalAgreementDto,
  AvatarUploadResponse,
  CompleteOnboardingResponse,
} from '@nexarats/api-contracts'
import { useAuth } from '@/context'
import {
  fetchOnboardingStatus,
  fetchStudentProfile,
  updateStudentProfile,
  fetchLegalAgreements,
  deleteAvatar,
} from '@/lib/onboardingApi'
import { StepStudentInfo, type StudentInfoData } from './StepStudentInfo'
import { StepDeveloperProfile, type DeveloperProfileData } from './StepDeveloperProfile'
import { StepAvatarUpload } from './StepAvatarUpload'
import { StepLegalAgreements } from './StepLegalAgreements'
import { StepReviewActivate } from './StepReviewActivate'

const TOTAL_STEPS = 5

const STEP_LABELS = [
  'Personal Info',
  'Developer Profile',
  'Avatar Upload',
  'Legal Terms',
  'Review & Activate',
]

export const OnboardingWizard: React.FC = () => {
  const { isAuthenticated, accessToken } = useAuth()
  const navigate = useNavigate()

  const [currentStep, setCurrentStep] = useState<number>(1)
  const [isLoading, setIsLoading] = useState<boolean>(true)
  const [isSavingDraft, setIsSavingDraft] = useState<boolean>(false)
  const [feedbackMessage, setFeedbackMessage] = useState<string | null>(null)
  const [completedSuccess, setCompletedSuccess] = useState<CompleteOnboardingResponse | null>(null)

  // Wizard state
  const [studentInfo, setStudentInfo] = useState<StudentInfoData>({
    headline: '',
    bio: '',
    academicBackground: '',
    careerGoals: '',
    timezone: 'UTC',
  })

  const [devProfile, setDevProfile] = useState<DeveloperProfileData>({
    githubUrl: '',
    linkedinUrl: '',
  })

  const [avatarAssetId, setAvatarAssetId] = useState<string | null>(null)
  const [avatarUrl, setAvatarUrl] = useState<string | null>(null)
  const [avatarScanStatus, setAvatarScanStatus] = useState<string | null>('CLEAN')
  const [avatarScanDetails, setAvatarScanDetails] = useState<string | null>(null)
  const [agreements, setAgreements] = useState<LegalAgreementDto[]>([])
  const [errors, setErrors] = useState<Record<string, string>>({})

  // Load initial data
  useEffect(() => {
    let isMounted = true

    async function loadData() {
      if (!isAuthenticated) {
        setIsLoading(false)
        return
      }

      try {
        setIsLoading(true)
        const [statusResp, profileResp, agreementsResp] = await Promise.all([
          fetchOnboardingStatus(accessToken).catch(() => null),
          fetchStudentProfile(accessToken).catch(() => null),
          fetchLegalAgreements(accessToken).catch(() => []),
        ])

        if (!isMounted) return

        if (profileResp) {
          setStudentInfo({
            headline: profileResp.headline || '',
            bio: profileResp.bio || '',
            academicBackground: profileResp.academicBackground || '',
            careerGoals: profileResp.careerGoals || '',
            timezone: profileResp.timezone || 'UTC',
          })

          setDevProfile({
            githubUrl: profileResp.githubUrl || '',
            linkedinUrl: profileResp.linkedinUrl || '',
          })

          if (profileResp.avatarAssetId) {
            setAvatarAssetId(profileResp.avatarAssetId)
          }
          if (profileResp.avatarUrl) {
            setAvatarUrl(profileResp.avatarUrl)
          }
        }

        if (agreementsResp && agreementsResp.length > 0) {
          setAgreements(agreementsResp)
        }

        // If student is already completed, go to step 5
        if (statusResp && statusResp.overallStatus === 'COMPLETED') {
          setCurrentStep(5)
        }
      } catch (err) {
        console.error('Failed to load onboarding wizard data:', err)
      } finally {
        if (isMounted) {
          setIsLoading(false)
        }
      }
    }

    loadData()

    return () => {
      isMounted = false
    }
  }, [isAuthenticated, accessToken])

  const handleStudentInfoChange = (field: keyof StudentInfoData, value: string) => {
    setStudentInfo((prev) => ({ ...prev, [field]: value }))
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: '' }))
    }
  }

  const handleDevProfileChange = (field: keyof DeveloperProfileData, value: string) => {
    setDevProfile((prev) => ({ ...prev, [field]: value }))
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: '' }))
    }
  }

  const handleAvatarUploaded = (resp: AvatarUploadResponse) => {
    setAvatarAssetId(resp.avatarAssetId)
    setAvatarUrl(resp.avatarUrl)
    setAvatarScanStatus(resp.scanStatus)
    setAvatarScanDetails(resp.scanDetails)
    if (resp.scanStatus === 'CLEAN') {
      setFeedbackMessage('Avatar photo uploaded and verified clean.')
    } else {
      setFeedbackMessage(`Avatar uploaded. Scan status: ${resp.scanStatus}`)
    }
    setTimeout(() => setFeedbackMessage(null), 3000)
  }

  const handleAvatarRemoved = async () => {
    try {
      await deleteAvatar(accessToken)
      setAvatarAssetId(null)
      setAvatarUrl(null)
      setAvatarScanStatus(null)
      setAvatarScanDetails(null)
      setFeedbackMessage('Avatar photo removed successfully.')
      setTimeout(() => setFeedbackMessage(null), 3000)
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to delete avatar'
      setFeedbackMessage(`Error removing avatar: ${msg}`)
    }
  }

  const saveDraft = async (): Promise<boolean> => {
    try {
      setIsSavingDraft(true)
      await updateStudentProfile(
        {
          headline: studentInfo.headline,
          bio: studentInfo.bio,
          academicBackground: studentInfo.academicBackground,
          careerGoals: studentInfo.careerGoals,
          timezone: studentInfo.timezone,
          githubUrl: devProfile.githubUrl,
          linkedinUrl: devProfile.linkedinUrl,
        },
        accessToken
      )
      setFeedbackMessage('Progress saved to draft.')
      setTimeout(() => setFeedbackMessage(null), 3000)
      return true
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to save draft'
      setFeedbackMessage(`Error saving draft: ${msg}`)
      return false
    } finally {
      setIsSavingDraft(false)
    }
  }

  const validateStep = (step: number): boolean => {
    const newErrors: Record<string, string> = {}

    if (step === 1) {
      if (!studentInfo.headline.trim()) {
        newErrors.headline = 'Professional headline is required.'
      }
      if (!studentInfo.bio.trim()) {
        newErrors.bio = 'About you / bio is required.'
      }
    } else if (step === 2) {
      const hasDevLink = Boolean(devProfile.githubUrl.trim() || devProfile.linkedinUrl.trim())
      if (!hasDevLink) {
        newErrors.githubUrl = 'Please provide at least your GitHub or LinkedIn profile URL.'
      }
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleNext = async () => {
    if (!validateStep(currentStep)) {
      return
    }

    // Persist profile updates when navigating between steps 1, 2, or 3
    if (currentStep <= 2) {
      const saved = await saveDraft()
      if (!saved) {
        return // Do not advance if draft persistence fails
      }
    }

    if (currentStep < TOTAL_STEPS) {
      setCurrentStep((prev) => prev + 1)
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }
  }

  const handleBack = () => {
    if (currentStep > 1) {
      setCurrentStep((prev) => prev - 1)
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }
  }

  const handleActivated = (resp: CompleteOnboardingResponse) => {
    setCompletedSuccess(resp)
    setTimeout(() => {
      navigate('/dashboard')
    }, 2500)
  }

  if (isLoading) {
    return (
      <div className="min-h-[50vh] flex items-center justify-center p-8">
        <div className="text-center">
          <div className="w-10 h-10 border-4 border-brand-200 border-t-brand-600 rounded-full animate-spin mx-auto mb-3" />
          <p className="text-xs font-semibold text-slate-500">Loading student onboarding wizard...</p>
        </div>
      </div>
    )
  }

  if (completedSuccess) {
    return (
      <div className="max-w-xl mx-auto py-12 px-4 text-center">
        <div className="bg-white rounded-3xl p-8 border border-emerald-200 shadow-elevated space-y-4">
          <div className="w-16 h-16 rounded-2xl bg-emerald-50 border border-emerald-200 flex items-center justify-center mx-auto text-emerald-600">
            <Sparkles className="w-8 h-8" />
          </div>
          <h2 className="text-2xl font-extrabold text-slate-900">Academic Onboarding Complete!</h2>
          <p className="text-xs text-slate-600">
            {completedSuccess.message}
          </p>
          <div className="p-3 bg-emerald-50 rounded-xl border border-emerald-200 text-xs font-bold text-emerald-800">
            Status: ACTIVE_STUDENT • Enrollment Activated
          </div>
          <p className="text-xs text-slate-400">
            Redirecting to your student learning dashboard...
          </p>
        </div>
      </div>
    )
  }

  const progressPercentage = (currentStep / TOTAL_STEPS) * 100

  return (
    <div className="max-w-3xl mx-auto py-8 px-4 sm:px-6">
      {/* Header Banner */}
      <div className="mb-6">
        <div className="flex items-center gap-2 mb-2">
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-brand-50 text-brand-600 border border-brand-200/90 shadow-sm">
            <GraduationCap className="w-3.5 h-3.5" />
            Phase 6B • Academic Onboarding
          </span>
          <span className="text-xs font-medium text-slate-400">
            Step {currentStep} of {TOTAL_STEPS}
          </span>
        </div>
        <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
          Student Academic Onboarding
        </h1>
        <p className="text-xs sm:text-sm text-slate-500 mt-1">
          Complete your student profile, upload your avatar, and digitally sign academic agreements to activate your learning journey.
        </p>
      </div>

      {/* Progress Bar */}
      <div className="mb-8">
        <div className="w-full bg-slate-200 h-2 rounded-full overflow-hidden">
          <div
            className="bg-brand-600 h-full transition-all duration-300 rounded-full"
            style={{ width: `${progressPercentage}%` }}
          />
        </div>

        {/* Step Indicator Pills */}
        <div className="grid grid-cols-5 gap-1 sm:gap-2 mt-3">
          {STEP_LABELS.map((label, idx) => {
            const stepNum = idx + 1
            const isCompleted = stepNum < currentStep
            const isCurrent = stepNum === currentStep

            return (
              <button
                key={label}
                type="button"
                onClick={() => {
                  if (stepNum < currentStep) {
                    setCurrentStep(stepNum)
                  }
                }}
                disabled={stepNum > currentStep}
                className={`text-center py-1.5 px-1 rounded-lg transition-all text-[11px] font-semibold truncate cursor-pointer ${
                  isCurrent
                    ? 'bg-brand-50 text-brand-700 border border-brand-300'
                    : isCompleted
                    ? 'text-slate-600 hover:bg-slate-100'
                    : 'text-slate-300 cursor-not-allowed'
                }`}
              >
                <span className="hidden sm:inline">{stepNum}. </span>
                {label}
              </button>
            )
          })}
        </div>
      </div>

      {/* Main Content Card */}
      <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200/90 shadow-card">
        {currentStep === 1 && (
          <StepStudentInfo
            data={studentInfo}
            onChange={handleStudentInfoChange}
            errors={errors}
          />
        )}

        {currentStep === 2 && (
          <StepDeveloperProfile
            data={devProfile}
            onChange={handleDevProfileChange}
            errors={errors}
          />
        )}

        {currentStep === 3 && (
          <StepAvatarUpload
            currentAvatarUrl={avatarUrl}
            currentAssetId={avatarAssetId}
            currentScanStatus={avatarScanStatus}
            currentScanDetails={avatarScanDetails}
            onAvatarUploaded={handleAvatarUploaded}
            onAvatarRemoved={handleAvatarRemoved}
            token={accessToken}
          />
        )}

        {currentStep === 4 && (
          <StepLegalAgreements
            agreements={agreements}
            onAgreementsUpdated={setAgreements}
            token={accessToken}
          />
        )}

        {currentStep === 5 && (
          <StepReviewActivate
            studentInfo={studentInfo}
            devProfile={devProfile}
            avatarUrl={avatarUrl}
            agreements={agreements}
            token={accessToken}
            onActivated={handleActivated}
            onJumpToStep={setCurrentStep}
          />
        )}

        {/* Feedback Alert */}
        {feedbackMessage && (
          <div className="mt-4 p-3 bg-slate-100 border border-slate-200 text-slate-700 rounded-xl text-xs flex items-center justify-between">
            <span>{feedbackMessage}</span>
          </div>
        )}

        {/* Navigation Actions */}
        <div className="mt-8 pt-6 border-t border-slate-100 flex flex-col-reverse sm:flex-row items-center justify-between gap-3">
          <div className="flex items-center gap-2 w-full sm:w-auto">
            {currentStep > 1 && (
              <button
                type="button"
                onClick={handleBack}
                className="w-full sm:w-auto px-4 py-2.5 bg-white border border-slate-300 hover:bg-slate-50 text-slate-700 text-xs font-semibold rounded-xl shadow-xs transition-all flex items-center justify-center gap-1.5 cursor-pointer"
              >
                <ArrowLeft className="w-3.5 h-3.5" />
                Previous Step
              </button>
            )}

            {currentStep < 5 && (
              <button
                type="button"
                onClick={saveDraft}
                disabled={isSavingDraft}
                className="w-full sm:w-auto px-4 py-2.5 bg-white border border-slate-300 hover:bg-slate-50 text-slate-700 text-xs font-semibold rounded-xl shadow-xs transition-all flex items-center justify-center gap-1.5 cursor-pointer"
              >
                <Save className="w-3.5 h-3.5" />
                {isSavingDraft ? 'Saving...' : 'Save Draft'}
              </button>
            )}
          </div>

          {currentStep < 5 && (
            <button
              type="button"
              onClick={handleNext}
              className="w-full sm:w-auto px-6 py-2.5 bg-brand-600 hover:bg-brand-700 text-white text-xs font-bold rounded-xl shadow-button-glow transition-all flex items-center justify-center gap-1.5 cursor-pointer"
            >
              Next Step
              <ArrowRight className="w-3.5 h-3.5" />
            </button>
          )}
        </div>
      </div>
    </div>
  )
}
