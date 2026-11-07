import type {
  OnboardingStatusResponse,
  StudentProfileDto,
  UpdateProfileRequest,
  AvatarUploadResponse,
  LegalAgreementDto,
  CompleteOnboardingResponse,
} from '@nexarats/api-contracts'

function getAuthHeaders(token?: string | null, isJson = true): Record<string, string> {
  const headers: Record<string, string> = {}
  if (isJson) {
    headers['Content-Type'] = 'application/json'
  }
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }
  return headers
}

export async function fetchOnboardingStatus(token?: string | null): Promise<OnboardingStatusResponse> {
  const res = await fetch('/api/v1/onboarding/status', {
    headers: getAuthHeaders(token),
    credentials: 'include',
  })
  if (!res.ok) {
    const errorBody = await res.text().catch(() => '')
    throw new Error(`Failed to fetch onboarding status (HTTP ${res.status}): ${errorBody}`)
  }
  return res.json()
}

export async function fetchStudentProfile(token?: string | null): Promise<StudentProfileDto> {
  const res = await fetch('/api/v1/onboarding/profile', {
    headers: getAuthHeaders(token),
    credentials: 'include',
  })
  if (!res.ok) {
    throw new Error(`Failed to fetch student profile (HTTP ${res.status})`)
  }
  return res.json()
}

export async function updateStudentProfile(
  data: UpdateProfileRequest,
  token?: string | null
): Promise<StudentProfileDto> {
  const res = await fetch('/api/v1/onboarding/profile', {
    method: 'PUT',
    headers: getAuthHeaders(token),
    credentials: 'include',
    body: JSON.stringify(data),
  })
  if (!res.ok) {
    const errorBody = await res.json().catch(() => null)
    const message = errorBody?.message || `HTTP ${res.status}`
    throw new Error(`Failed to update profile: ${message}`)
  }
  return res.json()
}

export async function uploadAvatar(file: File, token?: string | null): Promise<AvatarUploadResponse> {
  const formData = new FormData()
  formData.append('file', file)

  const res = await fetch('/api/v1/onboarding/avatar', {
    method: 'POST',
    headers: getAuthHeaders(token, false),
    credentials: 'include',
    body: formData,
  })
  if (!res.ok) {
    const errorBody = await res.json().catch(() => null)
    const message = errorBody?.message || `HTTP ${res.status}`
    throw new Error(`Avatar upload failed: ${message}`)
  }
  return res.json()
}

export async function fetchLegalAgreements(token?: string | null): Promise<LegalAgreementDto[]> {
  const res = await fetch('/api/v1/onboarding/agreements', {
    headers: getAuthHeaders(token),
    credentials: 'include',
  })
  if (!res.ok) {
    throw new Error(`Failed to fetch legal agreements (HTTP ${res.status})`)
  }
  return res.json()
}

export async function acceptLegalAgreement(
  agreementId: string,
  version: string,
  token?: string | null
): Promise<LegalAgreementDto> {
  const res = await fetch(`/api/v1/onboarding/agreements/${agreementId}/accept`, {
    method: 'POST',
    headers: getAuthHeaders(token),
    credentials: 'include',
    body: JSON.stringify({ agreementVersion: version }),
  })
  if (!res.ok) {
    const errorBody = await res.json().catch(() => null)
    const message = errorBody?.message || `HTTP ${res.status}`
    throw new Error(`Failed to accept agreement: ${message}`)
  }
  return res.json()
}

export async function completeOnboarding(token?: string | null): Promise<CompleteOnboardingResponse> {
  const res = await fetch('/api/v1/onboarding/complete', {
    method: 'POST',
    headers: getAuthHeaders(token),
    credentials: 'include',
  })
  if (!res.ok) {
    const errorBody = await res.json().catch(() => null)
    const message = errorBody?.message || `HTTP ${res.status}`
    throw new Error(`Onboarding completion failed: ${message}`)
  }
  return res.json()
}

export async function deleteAvatar(token?: string | null): Promise<void> {
  const res = await fetch('/api/v1/onboarding/avatar', {
    method: 'DELETE',
    headers: getAuthHeaders(token, false),
    credentials: 'include',
  })
  if (!res.ok) {
    const errorBody = await res.json().catch(() => null)
    const message = errorBody?.message || `HTTP ${res.status}`
    throw new Error(`Failed to remove avatar: ${message}`)
  }
}
