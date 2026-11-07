import React, { useState, useRef } from 'react'
import {
  UploadCloud,
  Image as ImageIcon,
  CheckCircle2,
  AlertTriangle,
  RefreshCw,
  Trash2,
  ShieldCheck,
  ShieldAlert,
  Clock,
} from 'lucide-react'
import type { AvatarUploadResponse } from '@nexarats/api-contracts'
import { uploadAvatarDirectToS3 } from '@/lib/onboardingApi'

interface StepAvatarUploadProps {
  currentAvatarUrl?: string | null
  currentAssetId?: string | null
  currentScanStatus?: string | null
  currentScanDetails?: string | null
  onAvatarUploaded: (response: AvatarUploadResponse) => void
  onAvatarRemoved: () => void
  token?: string | null
}

const MAX_SIZE_BYTES = 5 * 1024 * 1024 // 5 MB
const ALLOWED_MIME_TYPES = ['image/jpeg', 'image/png', 'image/webp']

export const StepAvatarUpload: React.FC<StepAvatarUploadProps> = ({
  currentAvatarUrl,
  currentAssetId,
  currentScanStatus = 'CLEAN',
  currentScanDetails,
  onAvatarUploaded,
  onAvatarRemoved,
  token,
}) => {
  const [isDragging, setIsDragging] = useState(false)
  const [isUploading, setIsUploading] = useState(false)
  const [uploadError, setUploadError] = useState<string | null>(null)
  const [scanStatusMessage, setScanStatusMessage] = useState<string | null>(null)
  const fileInputRef = useRef<HTMLInputElement | null>(null)

  const handleFileSelection = async (file: File) => {
    setUploadError(null)
    setScanStatusMessage(null)

    // Client-side validations
    if (!ALLOWED_MIME_TYPES.includes(file.type)) {
      setUploadError('Invalid file type. Supported formats are JPEG, PNG, and WebP. SVG and executable formats are prohibited.')
      return
    }

    if (file.size > MAX_SIZE_BYTES) {
      setUploadError(`File size exceeds 5 MB limit (${(file.size / (1024 * 1024)).toFixed(2)} MB). Please select a smaller photo.`)
      return
    }

    try {
      setIsUploading(true)
      const resp = await uploadAvatarDirectToS3(file, token, (msg) => setScanStatusMessage(msg))

      if (resp.scanStatus === 'INFECTED') {
        setUploadError(`Malware signature detected: ${resp.scanDetails || 'Threat detected'}. File rejected.`)
      } else if (resp.scanStatus === 'QUARANTINED') {
        setUploadError(`File quarantined: ${resp.scanDetails || 'Scanner offline'}. File cannot be activated.`)
      } else {
        onAvatarUploaded(resp)
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Upload failed'
      setUploadError(msg)
    } finally {
      setIsUploading(false)
      setScanStatusMessage(null)
      if (fileInputRef.current) {
        fileInputRef.current.value = ''
      }
    }
  }

  const handleDrop = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault()
    setIsDragging(false)
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      handleFileSelection(e.dataTransfer.files[0])
    }
  }

  const handleDragOver = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault()
    setIsDragging(true)
  }

  const handleDragLeave = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault()
    setIsDragging(false)
  }

  const renderStatusBadge = () => {
    switch (currentScanStatus) {
      case 'PENDING':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-amber-100 text-amber-800">
            <Clock className="w-3 h-3 text-amber-600 animate-spin" />
            Scan in Progress
          </span>
        )
      case 'QUARANTINED':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-rose-100 text-rose-800">
            <ShieldAlert className="w-3 h-3 text-rose-600" />
            Quarantined (Unverified)
          </span>
        )
      case 'INFECTED':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-red-100 text-red-800">
            <AlertTriangle className="w-3 h-3 text-red-600" />
            Malware Detected
          </span>
        )
      case 'CLEAN':
      default:
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-emerald-100 text-emerald-800">
            <CheckCircle2 className="w-3 h-3 text-emerald-600" />
            Verified & Clean
          </span>
        )
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <div className="flex items-center gap-2 mb-1">
          <div className="w-8 h-8 rounded-lg bg-brand-50 border border-brand-100 flex items-center justify-center text-brand-600">
            <ImageIcon className="w-4 h-4" />
          </div>
          <h3 className="text-lg font-bold text-slate-900">Student Profile Picture</h3>
        </div>
        <p className="text-xs text-slate-500">
          Upload a clear portrait photo. This avatar appears on your academy badge, code reviews, and cohort discussion forums.
        </p>
      </div>

      {/* Hidden file input */}
      <input
        ref={fileInputRef}
        type="file"
        accept="image/jpeg,image/png,image/webp"
        onChange={(e) => {
          if (e.target.files && e.target.files.length > 0) {
            handleFileSelection(e.target.files[0])
          }
        }}
        className="hidden"
      />

      {/* Preview if uploaded */}
      {currentAvatarUrl ? (
        <div className="p-6 bg-slate-50 border border-slate-200/90 rounded-2xl flex flex-col sm:flex-row items-center gap-6">
          <div className="relative">
            <img
              src={currentAvatarUrl}
              alt="Uploaded avatar"
              className="w-28 h-28 rounded-full object-cover border-4 border-white shadow-card"
            />
            <div className={`absolute -bottom-1 -right-1 p-1 rounded-full shadow-xs ${
              currentScanStatus === 'CLEAN' ? 'bg-emerald-500 text-white' : 'bg-amber-500 text-white'
            }`}>
              {currentScanStatus === 'CLEAN' ? <ShieldCheck className="w-4 h-4" /> : <ShieldAlert className="w-4 h-4" />}
            </div>
          </div>

          <div className="flex-1 text-center sm:text-left space-y-2">
            <div className="flex items-center justify-center sm:justify-start gap-2">
              {renderStatusBadge()}
              <span className="text-xs text-slate-500 font-mono">ID: {currentAssetId?.slice(0, 8)}...</span>
            </div>
            <p className="text-xs text-slate-600">
              {currentScanStatus === 'CLEAN'
                ? 'Avatar verified clean by ClamAV threat inspection and binary magic-bytes validation.'
                : currentScanDetails || 'Image pending verified antivirus scanning. Unverified images cannot become active avatars.'}
            </p>
            <div className="flex items-center justify-center sm:justify-start gap-3 pt-2">
              <button
                type="button"
                onClick={() => fileInputRef.current?.click()}
                disabled={isUploading}
                className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold bg-white border border-slate-200 text-slate-700 hover:bg-slate-50 shadow-2xs transition-colors"
              >
                <RefreshCw className="w-3.5 h-3.5 text-slate-500" />
                Replace Photo
              </button>
              <button
                type="button"
                onClick={onAvatarRemoved}
                disabled={isUploading}
                className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold bg-white border border-rose-200 text-rose-600 hover:bg-rose-50 shadow-2xs transition-colors"
              >
                <Trash2 className="w-3.5 h-3.5 text-rose-500" />
                Remove
              </button>
            </div>
          </div>
        </div>
      ) : (
        /* Upload dropzone */
        <div
          onDrop={handleDrop}
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onClick={() => fileInputRef.current?.click()}
          className={`relative border-2 border-dashed rounded-2xl p-8 text-center cursor-pointer transition-all duration-200 ${
            isDragging
              ? 'border-brand-500 bg-brand-50/50 scale-[1.01]'
              : 'border-slate-300 hover:border-brand-400 hover:bg-slate-50/50 bg-white'
          }`}
        >
          <div className="w-14 h-14 mx-auto mb-4 rounded-2xl bg-brand-50 border border-brand-100 flex items-center justify-center text-brand-600 shadow-2xs">
            {isUploading ? (
              <RefreshCw className="w-6 h-6 animate-spin text-brand-600" />
            ) : (
              <UploadCloud className="w-6 h-6" />
            )}
          </div>
          <h4 className="text-sm font-bold text-slate-900 mb-1">
            {isUploading ? 'Inspecting image with ClamAV...' : 'Click to select or drag photo here'}
          </h4>
          <p className="text-xs text-slate-500 max-w-sm mx-auto mb-3">
            Supports portrait images in JPG, PNG, or WebP format up to 5 MB in size.
          </p>

          <div className="flex flex-wrap items-center justify-center gap-2 text-[11px] text-slate-400 font-mono">
            <span className="px-2 py-0.5 bg-slate-100 rounded-md">JPG / JPEG</span>
            <span className="px-2 py-0.5 bg-slate-100 rounded-md">PNG</span>
            <span className="px-2 py-0.5 bg-slate-100 rounded-md">WebP</span>
            <span className="px-2 py-0.5 bg-slate-100 rounded-md">&le; 5 MB</span>
          </div>

          {scanStatusMessage && (
            <p className="mt-3 text-xs text-brand-600 font-medium animate-pulse">
              {scanStatusMessage}
            </p>
          )}
        </div>
      )}

      {/* Upload Error Banner */}
      {uploadError && (
        <div className="p-3 bg-rose-50 border border-rose-200 rounded-xl flex items-start gap-2.5 text-xs text-rose-800">
          <AlertTriangle className="w-4 h-4 text-rose-600 shrink-0 mt-0.5" />
          <div className="flex-1">
            <p className="font-semibold">Upload Rejected</p>
            <p className="mt-0.5 text-rose-700">{uploadError}</p>
          </div>
        </div>
      )}

      {/* Security notice */}
      <div className="p-3.5 bg-slate-50 border border-slate-200/80 rounded-xl flex items-start gap-3">
        <ShieldCheck className="w-4 h-4 text-brand-600 shrink-0 mt-0.5" />
        <div className="text-xs text-slate-600 leading-relaxed">
          <span className="font-semibold text-slate-800">Secure Media Ingestion: </span>
          All avatar uploads undergo server-side MIME magic-byte verification, image dimension validation, and stream scanning via ClamAV. Executable or HTML-embedded files are automatically rejected and quarantined.
        </div>
      </div>
    </div>
  )
}
