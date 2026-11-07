import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { StepAvatarUpload } from '../StepAvatarUpload'
import * as onboardingApi from '@/lib/onboardingApi'

describe('StepAvatarUpload Direct S3 Integration Tests', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('rejects client-side upload of unsupported file types without calling API', async () => {
    const uploadSpy = vi.spyOn(onboardingApi, 'uploadAvatarDirectToS3')
    render(
      <StepAvatarUpload
        currentAvatarUrl={null}
        currentAssetId={null}
        onAvatarUploaded={vi.fn()}
        onAvatarRemoved={vi.fn()}
      />
    )

    const file = new File(['<svg>malicious script</svg>'], 'avatar.svg', { type: 'image/svg+xml' })
    const input = document.querySelector('input[type="file"]') as HTMLInputElement

    fireEvent.change(input, { target: { files: [file] } })

    await waitFor(() => {
      expect(
        screen.getByText(/Invalid file type. Supported formats are JPEG, PNG, and WebP/i)
      ).toBeDefined()
    })

    expect(uploadSpy).not.toHaveBeenCalled()
  })

  it('rejects oversized files exceeding 5MB without calling API', async () => {
    const uploadSpy = vi.spyOn(onboardingApi, 'uploadAvatarDirectToS3')
    render(
      <StepAvatarUpload
        currentAvatarUrl={null}
        currentAssetId={null}
        onAvatarUploaded={vi.fn()}
        onAvatarRemoved={vi.fn()}
      />
    )

    const oversizedBuffer = new Uint8Array(5 * 1024 * 1024 + 1024)
    const file = new File([oversizedBuffer], 'huge_photo.jpg', { type: 'image/jpeg' })
    const input = document.querySelector('input[type="file"]') as HTMLInputElement

    fireEvent.change(input, { target: { files: [file] } })

    await waitFor(() => {
      expect(screen.getByText(/File size exceeds 5 MB limit/i)).toBeDefined()
    })

    expect(uploadSpy).not.toHaveBeenCalled()
  })

  it('successfully executes direct S3 pre-signed upload lifecycle and updates parent', async () => {
    const mockSuccessResponse = {
      avatarAssetId: 'asset-uuid-1234',
      storageObjectKey: 'avatars/tenant-1/user-1/approved.png',
      contentType: 'image/png',
      fileSizeBytes: 2048,
      scanStatus: 'CLEAN' as const,
      scanDetails: 'ClamAV OK',
      avatarUrl: '/api/v1/onboarding/avatar/asset-uuid-1234',
    }

    const uploadSpy = vi.spyOn(onboardingApi, 'uploadAvatarDirectToS3').mockResolvedValue(mockSuccessResponse)
    const handleUploaded = vi.fn()

    render(
      <StepAvatarUpload
        currentAvatarUrl={null}
        currentAssetId={null}
        onAvatarUploaded={handleUploaded}
        onAvatarRemoved={vi.fn()}
        token="jwt-token-123"
      />
    )

    const file = new File(['valid-png-binary'], 'portrait.png', { type: 'image/png' })
    const input = document.querySelector('input[type="file"]') as HTMLInputElement

    fireEvent.change(input, { target: { files: [file] } })

    await waitFor(() => {
      expect(uploadSpy).toHaveBeenCalledWith(file, 'jwt-token-123', expect.any(Function))
      expect(handleUploaded).toHaveBeenCalledWith(mockSuccessResponse)
    })
  })

  it('truthfully displays quarantined error banner when ClamAV scanner fails closed', async () => {
    const quarantinedResponse = {
      avatarAssetId: 'asset-quarantine-999',
      storageObjectKey: 'quarantine/avatars/tenant-1/user-1/ticket.png',
      contentType: 'image/png',
      fileSizeBytes: 2048,
      scanStatus: 'QUARANTINED' as const,
      scanDetails: 'Scanner offline (connection refused)',
      avatarUrl: '',
    }

    vi.spyOn(onboardingApi, 'uploadAvatarDirectToS3').mockResolvedValue(quarantinedResponse)
    const handleUploaded = vi.fn()

    render(
      <StepAvatarUpload
        currentAvatarUrl={null}
        currentAssetId={null}
        onAvatarUploaded={handleUploaded}
        onAvatarRemoved={vi.fn()}
      />
    )

    const file = new File(['valid-png-binary'], 'portrait.png', { type: 'image/png' })
    const input = document.querySelector('input[type="file"]') as HTMLInputElement

    fireEvent.change(input, { target: { files: [file] } })

    await waitFor(() => {
      expect(screen.getByText(/File quarantined: Scanner offline/i)).toBeDefined()
      expect(handleUploaded).not.toHaveBeenCalled()
    })
  })

  it('truthfully displays malware rejected error banner when infected with EICAR payload', async () => {
    const infectedResponse = {
      avatarAssetId: 'asset-infected-666',
      storageObjectKey: 'quarantine/avatars/tenant-1/user-1/ticket.png',
      contentType: 'image/png',
      fileSizeBytes: 2048,
      scanStatus: 'INFECTED' as const,
      scanDetails: 'Eicar-Signature FOUND',
      avatarUrl: '',
    }

    vi.spyOn(onboardingApi, 'uploadAvatarDirectToS3').mockResolvedValue(infectedResponse)
    const handleUploaded = vi.fn()

    render(
      <StepAvatarUpload
        currentAvatarUrl={null}
        currentAssetId={null}
        onAvatarUploaded={handleUploaded}
        onAvatarRemoved={vi.fn()}
      />
    )

    const file = new File(['eicar-bytes'], 'eicar.png', { type: 'image/png' })
    const input = document.querySelector('input[type="file"]') as HTMLInputElement

    fireEvent.change(input, { target: { files: [file] } })

    await waitFor(() => {
      expect(screen.getByText(/Malware signature detected: Eicar-Signature FOUND/i)).toBeDefined()
      expect(handleUploaded).not.toHaveBeenCalled()
    })
  })
})
