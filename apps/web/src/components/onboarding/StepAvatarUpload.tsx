import React, { useState, useRef } from 'react'
import {
  UploadCloud,
  Image as ImageIcon,
  CheckCircle2,
  AlertTriangle,
  RefreshCw,
  Trash2,
  ShieldCheck,
  FileCheck,
} from 'lucide-react'
import type { AvatarUploadResponse } from '@nexarats/api-contracts'
import { uploadAvatar } from '@/lib/onboardingApi'

interface StepAvatarUploadProps {
  currentAvatarUrl?: string | null
  currentAssetId?: string | null
  onAvatarUploaded: (response: AvatarUploadResponse) => void
  onAvatarRemoved: () => void
  token?: string | null
}

const MAX_SIZE_BYTES = 5 * 1024 * 1024 // 5 MB
const ALLOWED_MIME_TYPES = ['image/jpeg', 'image/png', 'image/webp']

export const StepAvatarUpload: React.FC<StepAvatarUploadProps> = ({
  currentAvatarUrl,
  currentAssetId,
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
      setScanStatusMessage('Uploading and scanning payload with malware detection...')
      const resp = await uploadAvatar(file, token)
      setScanStatusMessage(`Scan completed: ${resp.scanStatus} (${resp.scanDetails})`)
      onAvatarUploaded(resp)
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Upload failed'
      setUploadError(message)
      setScanStatusMessage(null)
    } finally {
      setIsUploading(false)
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
            <div className="absolute -bottom-1 -right-1 bg-emerald-500 text-white p-1 rounded-full shadow-xs">
              <ShieldCheck className="w-4 h-4" />
            </div>
          </div>

          <div className="flex-1 text-center sm:text-left space-y-2">
            <div className="flex items-center justify-center sm:justify-start gap-2">
              <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-emerald-100 text-emerald-800">
                <CheckCircle2 className="w-3 h-3 text-emerald-600" />
                Verified & Clean
              </span>
              <span className="text-xs text-slate-500 font-mono">ID: {currentAssetId?.slice(0, 8)}...</span>
            </div>
            <p className="text-xs text-slate-600">
              Avatar uploaded successfully and passed MIME magic-bytes validation and security inspection.
            </p>

            <div className="flex items-center justify-center sm:justify-start gap-3 pt-1">
              <button
                type="button"
                onClick={() => fileInputRef.current?.click()}
                disabled={isUploading}
                className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-white border border-slate-300 hover:border-brand-500 text-slate-700 text-xs font-semibold rounded-lg shadow-xs transition-all cursor-pointer"
              >
                <RefreshCw className={`w-3.5 h-3.5 ${isUploading ? 'animate-spin' : ''}`} />
                Replace Photo
              </button>
              <button
                type="button"
                onClick={onAvatarRemoved}
                disabled={isUploading}
                className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-white border border-rose-200 hover:bg-rose-50 text-rose-600 text-xs font-semibold rounded-lg shadow-xs transition-all cursor-pointer"
              >
                <Trash2 className="w-3.5 h-3.5" />
                Remove
              </button>
            </div>
          </div>
        </div>
      ) : (
        /* Dropzone */
        <div
          onDrop={handleDrop}
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onClick={() => !isUploading && fileInputRef.current?.click()}
          className={`border-2 border-dashed rounded-2xl p-8 text-center cursor-pointer transition-all ${
            isDragging
              ? 'border-brand-500 bg-brand-50/50'
              : 'border-slate-300 hover:border-brand-400 bg-slate-50/50 hover:bg-brand-50/20'
          }`}
        >
          <div className="w-14 h-14 rounded-2xl bg-brand-50 border border-brand-100 flex items-center justify-center mx-auto mb-3 text-brand-600">
            {isUploading ? (
              <RefreshCw className="w-6 h-6 animate-spin" />
            ) : (
              <UploadCloud className="w-6 h-6" />
            )}
          </div>

          <h4 className="text-sm font-bold text-slate-900 mb-1">
            {isUploading ? 'Uploading & Scanning Avatar...' : 'Drag and drop your photo here, or click to browse'}
          </h4>
          <p className="text-xs text-slate-500 mb-3">
            Supports JPEG, PNG, or WebP up to 5 MB. Strictly verified via magic-byte inspection.
          </p>

          <span className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-white border border-slate-200 rounded-xl text-xs font-semibold text-slate-700 shadow-xs">
            <FileCheck className="w-3.5 h-3.5 text-brand-600" />
            Choose File
          </span>
        </div>
      )}

      {/* Upload & Scan Progress */}
      {scanStatusMessage && (
        <div className="p-3 bg-blue-50 border border-blue-200 text-blue-800 rounded-xl text-xs flex items-center gap-2">
          <ShieldCheck className="w-4 h-4 text-blue-600 shrink-0" />
          <span>{scanStatusMessage}</span>
        </div>
      )}

      {/* Error Callout */}
      {uploadError && (
        <div className="p-3 bg-rose-50 border border-rose-200 text-rose-800 rounded-xl text-xs flex items-center gap-2">
          <AlertTriangle className="w-4 h-4 text-rose-600 shrink-0" />
          <span>{uploadError}</span>
        </div>
      )}

      {/* Security Specifications Note */}
      <div className="bg-slate-100/70 p-3.5 rounded-xl border border-slate-200 text-[11px] text-slate-500 space-y-1">
        <div className="font-semibold text-slate-700">Security & Integrity Enforcement:</div>
        <ul className="list-disc list-inside space-y-0.5 text-slate-600">
          <li>Uploaded images undergo server-side MIME magic-byte verification (rejecting embedded HTML/SVG/executables).</li>
          <li>Files are scanned by ClamAV / heuristic threat detection prior to storage.</li>
          <li>Avatars are stored with randomized UUID keys to prevent directory traversal and overwrite attacks.</li>
        </ul>
      </div>
    </div>
  )
}
