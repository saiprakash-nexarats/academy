export type OnboardingStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED'

export type LegalAgreementType = 'TERMS_OF_SERVICE' | 'ACADEMIC_INTEGRITY' | 'CODE_OF_CONDUCT'

export type ScanStatus = 'PENDING' | 'CLEAN' | 'QUARANTINED' | 'INFECTED'

export interface StudentProfileDto {
  id: string
  tenantId: string | null
  userId: string
  headline: string | null
  bio: string | null
  academicBackground: string | null
  careerGoals: string | null
  timezone: string
  githubUrl: string | null
  linkedinUrl: string | null
  avatarAssetId: string | null
  avatarUrl: string | null
  onboardingStatus: OnboardingStatus
  onboardingCompletedAt: string | null
  createdAt: string
  updatedAt: string
}

export interface UpdateProfileRequest {
  headline?: string
  bio?: string
  academicBackground?: string
  careerGoals?: string
  timezone?: string
  githubUrl?: string
  linkedinUrl?: string
}

export interface LegalAgreementDto {
  id: string
  agreementType: LegalAgreementType
  version: string
  title: string
  content: string
  contentHash: string
  effectiveDate: string
  isAccepted: boolean
  acceptedAt: string | null
}

export interface AcceptAgreementRequest {
  agreementVersion: string
}

export interface AvatarUploadResponse {
  avatarAssetId: string
  storageObjectKey: string
  contentType: string
  fileSizeBytes: number
  scanStatus: ScanStatus
  scanDetails: string
  avatarUrl: string
}

export interface OnboardingStatusResponse {
  overallStatus: OnboardingStatus
  profileCompleted: boolean
  developerLinksCompleted: boolean
  avatarUploaded: boolean
  legalAgreementsAccepted: boolean
  pendingAgreements: string[]
  canActivate: boolean
  studentProfile: StudentProfileDto | null
  activeEnrollmentId: string | null
  activeEnrollmentStatus: string | null
  blockingReasons?: string[]
}

export interface CompleteOnboardingResponse {
  success: boolean
  message: string
  onboardingStatus: OnboardingStatus
  completedAt: string
  enrollmentStatus: string
}
