import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { OnboardingWizard } from '../OnboardingWizard'
import * as onboardingApi from '@/lib/onboardingApi'

// Mock react-router-dom
const mockNavigate = vi.fn()
vi.mock('react-router-dom', () => ({
  useNavigate: () => mockNavigate,
}))

// Mock useAuth
vi.mock('@/context', () => ({
  useAuth: () => ({
    isAuthenticated: true,
    accessToken: 'test-token-jwt',
  }),
}))

// Mock onboardingApi
vi.mock('@/lib/onboardingApi', () => ({
  fetchOnboardingStatus: vi.fn(),
  fetchStudentProfile: vi.fn(),
  fetchLegalAgreements: vi.fn(),
  updateStudentProfile: vi.fn(),
  deleteAvatar: vi.fn(),
}))

describe('OnboardingWizard Navigation and Persistence Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    window.scrollTo = vi.fn()

    vi.mocked(onboardingApi.fetchOnboardingStatus).mockResolvedValue({
      overallStatus: 'IN_PROGRESS',
      profileCompleted: false,
      developerLinksCompleted: false,
      avatarUploaded: false,
      legalAgreementsAccepted: false,
      pendingAgreements: [],
      canActivate: false,
      studentProfile: null,
      activeEnrollmentId: 'enroll-1',
      activeEnrollmentStatus: 'ENROLLED',
    })

    vi.mocked(onboardingApi.fetchStudentProfile).mockResolvedValue({
      id: 'prof-1',
      tenantId: 'tenant-1',
      userId: 'user-1',
      headline: 'Software Engineer',
      bio: 'Enthusiastic student',
      academicBackground: 'B.S. in Computer Science',
      careerGoals: 'Staff Architect',
      timezone: 'UTC',
      githubUrl: 'https://github.com/test',
      linkedinUrl: 'https://linkedin.com/in/test',
      avatarAssetId: null,
      avatarUrl: null,
      onboardingStatus: 'IN_PROGRESS',
      onboardingCompletedAt: null,
      createdAt: '2026-01-01T00:00:00Z',
      updatedAt: '2026-01-01T00:00:00Z',
    })

    vi.mocked(onboardingApi.fetchLegalAgreements).mockResolvedValue([])
    vi.mocked(onboardingApi.updateStudentProfile).mockResolvedValue({} as any)
    vi.mocked(onboardingApi.deleteAvatar).mockResolvedValue({} as any)
  })

  it('advances to step 2 when step 1 is valid and draft save succeeds', async () => {
    render(<OnboardingWizard />)

    // Wait for initial load
    await waitFor(() => {
      expect(screen.getByText(/Personal Info/i)).toBeDefined()
    })

    // Find and click Next button
    const nextBtn = screen.getByRole('button', { name: /Next Step/i })
    fireEvent.click(nextBtn)

    await waitFor(() => {
      expect(onboardingApi.updateStudentProfile).toHaveBeenCalledTimes(1)
      // Step 2 is Developer Profile
      expect(screen.getByText(/Developer & Professional Links/i)).toBeDefined()
    })
  })

  it('halts advancement and displays error when draft save fails on Step 1', async () => {
    vi.mocked(onboardingApi.updateStudentProfile).mockRejectedValueOnce(new Error('Network error saving profile'))

    render(<OnboardingWizard />)

    await waitFor(() => {
      expect(screen.getByText(/Personal Info/i)).toBeDefined()
    })

    const nextBtn = screen.getByRole('button', { name: /Next Step/i })
    fireEvent.click(nextBtn)

    await waitFor(() => {
      expect(onboardingApi.updateStudentProfile).toHaveBeenCalledTimes(1)
      // Should show feedback error
      expect(screen.getByText(/Error saving draft: Network error saving profile/i)).toBeDefined()
      // Should NOT have advanced to Step 2 (Developer & Social Presence header not visible)
      expect(screen.queryByText(/Developer & Social Presence/i)).toBeNull()
      // Still on Step 1
      expect(screen.getByLabelText(/Professional \/ Student Headline/i)).toBeDefined()
    })
  })

  it('calls deleteAvatar when user removes uploaded avatar', async () => {
    vi.mocked(onboardingApi.fetchStudentProfile).mockResolvedValueOnce({
      id: 'prof-1',
      tenantId: 'tenant-1',
      userId: 'user-1',
      headline: 'Software Engineer',
      bio: 'Enthusiastic student',
      academicBackground: 'B.S.',
      careerGoals: 'Architect',
      timezone: 'UTC',
      githubUrl: 'https://github.com/test',
      linkedinUrl: 'https://linkedin.com/in/test',
      avatarAssetId: 'asset-1234-5678',
      avatarUrl: '/api/v1/onboarding/avatar/test.png',
      onboardingStatus: 'IN_PROGRESS',
      onboardingCompletedAt: null,
      createdAt: '2026-01-01T00:00:00Z',
      updatedAt: '2026-01-01T00:00:00Z',
    })

    render(<OnboardingWizard />)

    // Move to step 3 (Avatar Upload)
    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Next Step/i })).toBeDefined()
    })
    fireEvent.click(screen.getByRole('button', { name: /Next Step/i }))

    await waitFor(() => {
      expect(screen.getByText(/Developer & Professional Links/i)).toBeDefined()
    })
    fireEvent.click(screen.getByRole('button', { name: /Next Step/i }))

    await waitFor(() => {
      expect(screen.getByText(/Student Profile Picture/i)).toBeDefined()
    })

    // Click Remove button
    const removeBtn = screen.getByRole('button', { name: /Remove/i })
    fireEvent.click(removeBtn)

    await waitFor(() => {
      expect(onboardingApi.deleteAvatar).toHaveBeenCalledWith('test-token-jwt')
      expect(screen.getByText(/Avatar photo removed successfully/i)).toBeDefined()
    })
  })
})
