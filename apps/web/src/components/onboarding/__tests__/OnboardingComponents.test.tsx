import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import { StepStudentInfo } from '../StepStudentInfo'
import { StepDeveloperProfile } from '../StepDeveloperProfile'
import { StepAvatarUpload } from '../StepAvatarUpload'
import { StepLegalAgreements } from '../StepLegalAgreements'
import { StepReviewActivate } from '../StepReviewActivate'
import type { LegalAgreementDto } from '@nexarats/api-contracts'

describe('StepStudentInfo Component Tests', () => {
  it('renders student profile input fields and propagates changes', () => {
    const handleChange = vi.fn()
    render(
      <StepStudentInfo
        data={{
          headline: 'Junior Developer',
          bio: 'Building things',
          academicBackground: 'B.S. CS',
          careerGoals: 'Staff Engineer',
          timezone: 'UTC',
        }}
        onChange={handleChange}
        errors={{}}
      />
    )

    expect(screen.getByLabelText(/Professional \/ Student Headline/i)).toBeDefined()
    expect(screen.getByLabelText(/About You \/ Bio/i)).toBeDefined()

    const headlineInput = screen.getByLabelText(/Professional \/ Student Headline/i)
    fireEvent.change(headlineInput, { target: { value: 'Senior Backend Engineer' } })
    expect(handleChange).toHaveBeenCalledWith('headline', 'Senior Backend Engineer')
  })

  it('displays validation error messages when provided', () => {
    render(
      <StepStudentInfo
        data={{
          headline: '',
          bio: '',
          academicBackground: '',
          careerGoals: '',
          timezone: 'UTC',
        }}
        onChange={vi.fn()}
        errors={{
          headline: 'Headline is required',
          bio: 'Bio is required',
        }}
      />
    )

    expect(screen.getByText('Headline is required')).toBeDefined()
    expect(screen.getByText('Bio is required')).toBeDefined()
  })
})

describe('StepDeveloperProfile Component Tests', () => {
  it('validates GitHub and LinkedIn format feedback', () => {
    const handleChange = vi.fn()
    const { rerender } = render(
      <StepDeveloperProfile
        data={{
          githubUrl: '',
          linkedinUrl: '',
        }}
        onChange={handleChange}
      />
    )

    expect(
      screen.getByText(/Please provide at least one developer or professional link/i)
    ).toBeDefined()

    rerender(
      <StepDeveloperProfile
        data={{
          githubUrl: 'https://github.com/teststudent',
          linkedinUrl: '',
        }}
        onChange={handleChange}
      />
    )

    expect(
      screen.getByText(/Developer profile requirement satisfied/i)
    ).toBeDefined()
  })
})

describe('StepAvatarUpload Component Tests', () => {
  it('renders upload dropzone when no avatar is set', () => {
    render(
      <StepAvatarUpload
        currentAvatarUrl={null}
        currentAssetId={null}
        onAvatarUploaded={vi.fn()}
        onAvatarRemoved={vi.fn()}
      />
    )

    expect(screen.getByText(/Drag and drop your photo here/i)).toBeDefined()
    expect(screen.getByText(/Supports JPEG, PNG, or WebP up to 5 MB/i)).toBeDefined()
  })

  it('renders avatar preview and verified badge when avatar is uploaded', () => {
    render(
      <StepAvatarUpload
        currentAvatarUrl="/api/v1/onboarding/avatar/12345"
        currentAssetId="12345678-0000-0000-0000-000000000000"
        onAvatarUploaded={vi.fn()}
        onAvatarRemoved={vi.fn()}
      />
    )

    expect(screen.getByText(/Verified & Clean/i)).toBeDefined()
    expect(screen.getByText(/Replace Photo/i)).toBeDefined()
    expect(screen.getByText(/Remove/i)).toBeDefined()
  })
})

describe('StepLegalAgreements Component Tests', () => {
  const sampleAgreements: LegalAgreementDto[] = [
    {
      id: 'ag-1',
      agreementType: 'TERMS_OF_SERVICE',
      version: '1.0',
      title: 'Nexarats Academy Terms of Service',
      content: 'Sample terms of service content text.',
      contentHash: '6f91e9a28055cab586559828536864da47b022cbe58d2c061f3ed766d57220c4',
      effectiveDate: '2026-01-01T00:00:00Z',
      isAccepted: true,
      acceptedAt: '2026-02-01T12:00:00Z',
    },
    {
      id: 'ag-2',
      agreementType: 'ACADEMIC_INTEGRITY',
      version: '1.0',
      title: 'Nexarats Academy Academic Integrity Agreement',
      content: 'Sample academic integrity content text.',
      contentHash: 'a6b1adb495692064419c3699aec5f4d9e29d6649e5242d3bb66ff32284ea5c7e',
      effectiveDate: '2026-01-01T00:00:00Z',
      isAccepted: false,
      acceptedAt: null,
    },
  ]

  it('displays agreement cards with version and hash', () => {
    render(
      <StepLegalAgreements
        agreements={sampleAgreements}
        onAgreementsUpdated={vi.fn()}
      />
    )

    expect(screen.getByRole('heading', { name: 'Nexarats Academy Terms of Service' })).toBeDefined()
    expect(screen.getByRole('heading', { name: 'Nexarats Academy Academic Integrity Agreement' })).toBeDefined()
    expect(screen.getByText(/1 of 2 Signed/i)).toBeDefined()
  })

  it('can expand full agreement content', () => {
    render(
      <StepLegalAgreements
        agreements={sampleAgreements}
        onAgreementsUpdated={vi.fn()}
      />
    )

    const toggleButtons = screen.getAllByLabelText(/Toggle full agreement text/i)
    fireEvent.click(toggleButtons[0])

    expect(screen.getByText('Sample terms of service content text.')).toBeDefined()
  })
})

describe('StepReviewActivate Component Tests', () => {
  const completeAgreements: LegalAgreementDto[] = [
    {
      id: 'ag-1',
      agreementType: 'TERMS_OF_SERVICE',
      version: '1.0',
      title: 'Terms of Service',
      content: 'Content',
      contentHash: 'hash1',
      effectiveDate: '2026-01-01T00:00:00Z',
      isAccepted: true,
      acceptedAt: '2026-01-02T00:00:00Z',
    },
  ]

  it('disables activate button when requirements are incomplete', () => {
    render(
      <StepReviewActivate
        studentInfo={{
          headline: '',
          bio: '',
          academicBackground: '',
          careerGoals: '',
          timezone: 'UTC',
        }}
        devProfile={{
          githubUrl: '',
          linkedinUrl: '',
        }}
        avatarUrl={null}
        agreements={[]}
        onActivated={vi.fn()}
        onJumpToStep={vi.fn()}
      />
    )

    const activateBtn = screen.getByRole('button', { name: /Activate Student Profile/i }) as HTMLButtonElement
    expect(activateBtn.disabled).toBe(true)
    expect(screen.getByText(/Headline or bio missing/i)).toBeDefined()
  })

  it('enables activate button when all 4 steps are valid', () => {
    render(
      <StepReviewActivate
        studentInfo={{
          headline: 'Full Stack Engineer',
          bio: 'Passionate learner',
          academicBackground: 'B.S. CS',
          careerGoals: 'AI Architect',
          timezone: 'UTC',
        }}
        devProfile={{
          githubUrl: 'https://github.com/student',
          linkedinUrl: '',
        }}
        avatarUrl="/api/v1/onboarding/avatar/test"
        agreements={completeAgreements}
        onActivated={vi.fn()}
        onJumpToStep={vi.fn()}
      />
    )

    const activateBtn = screen.getByRole('button', { name: /Activate Student Profile/i }) as HTMLButtonElement
    expect(activateBtn.disabled).toBe(false)
  })
})
