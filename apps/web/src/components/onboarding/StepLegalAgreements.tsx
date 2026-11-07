import React, { useState } from 'react'
import {
  FileText,
  Shield,
  CheckCircle2,
  Clock,
  Hash,
  ChevronDown,
  ChevronUp,
  AlertCircle,
} from 'lucide-react'
import type { LegalAgreementDto } from '@nexarats/api-contracts'
import { acceptLegalAgreement } from '@/lib/onboardingApi'

interface StepLegalAgreementsProps {
  agreements: LegalAgreementDto[]
  onAgreementsUpdated: (updated: LegalAgreementDto[]) => void
  token?: string | null
}

export const StepLegalAgreements: React.FC<StepLegalAgreementsProps> = ({
  agreements,
  onAgreementsUpdated,
  token,
}) => {
  const [expandedId, setExpandedId] = useState<string | null>(null)
  const [signingId, setSigningId] = useState<string | null>(null)
  const [errorMap, setErrorMap] = useState<Record<string, string>>({})

  const toggleExpand = (id: string) => {
    setExpandedId((prev) => (prev === id ? null : id))
  }

  const handleToggleAccept = async (ag: LegalAgreementDto) => {
    if (ag.isAccepted) {
      // Once accepted, legal e-signatures are non-repudiable audit records in enterprise systems
      return
    }

    try {
      setSigningId(ag.id)
      setErrorMap((prev) => ({ ...prev, [ag.id]: '' }))

      const updatedAg = await acceptLegalAgreement(ag.id, ag.version, token)

      const nextList = agreements.map((item) =>
        item.id === ag.id ? { ...item, isAccepted: true, acceptedAt: updatedAg.acceptedAt } : item
      )
      onAgreementsUpdated(nextList)
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Acceptance failed'
      setErrorMap((prev) => ({ ...prev, [ag.id]: message }))
    } finally {
      setSigningId(null)
    }
  }

  const allAccepted = agreements.length > 0 && agreements.every((a) => a.isAccepted)
  const acceptedCount = agreements.filter((a) => a.isAccepted).length

  return (
    <div className="space-y-6">
      <div>
        <div className="flex items-center gap-2 mb-1">
          <div className="w-8 h-8 rounded-lg bg-brand-50 border border-brand-100 flex items-center justify-center text-brand-600">
            <Shield className="w-4 h-4" />
          </div>
          <h3 className="text-lg font-bold text-slate-900">Legal Agreements & E-Signatures</h3>
        </div>
        <p className="text-xs text-slate-500">
          Review and digitally sign the authoritative academy agreements. Your clickwrap acceptance is securely audited with timestamp, client IP, and cryptographic content hash.
        </p>
      </div>

      {/* Progress status pill */}
      <div className="flex items-center justify-between p-3 bg-slate-50 border border-slate-200 rounded-xl text-xs">
        <span className="font-semibold text-slate-700">Agreement Acceptance Status:</span>
        <span
          className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold ${
            allAccepted
              ? 'bg-emerald-100 text-emerald-800'
              : 'bg-amber-100 text-amber-800'
          }`}
        >
          {allAccepted ? (
            <>
              <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
              All 3 Mandatory Agreements Signed
            </>
          ) : (
            <>
              <Clock className="w-3.5 h-3.5 text-amber-600" />
              {acceptedCount} of {agreements.length} Signed
            </>
          )}
        </span>
      </div>

      {/* Agreement Cards */}
      <div className="space-y-4">
        {agreements.map((ag) => {
          const isExpanded = expandedId === ag.id
          const isSigning = signingId === ag.id
          const errorMsg = errorMap[ag.id]

          return (
            <div
              key={ag.id}
              className={`border rounded-2xl transition-all duration-200 ${
                ag.isAccepted
                  ? 'border-emerald-200 bg-white shadow-xs'
                  : 'border-slate-200 bg-white shadow-card'
              }`}
            >
              <div className="p-4 sm:p-5">
                <div className="flex items-start justify-between gap-3">
                  <div className="flex items-start gap-3">
                    <div
                      className={`w-9 h-9 rounded-xl flex items-center justify-center shrink-0 mt-0.5 ${
                        ag.isAccepted
                          ? 'bg-emerald-50 text-emerald-600 border border-emerald-200'
                          : 'bg-slate-100 text-slate-600 border border-slate-200'
                      }`}
                    >
                      <FileText className="w-4 h-4" />
                    </div>
                    <div>
                      <div className="flex flex-wrap items-center gap-2 mb-1">
                        <h4 className="text-sm font-bold text-slate-900">{ag.title}</h4>
                        <span className="px-2 py-0.5 text-[10px] font-bold bg-slate-100 text-slate-600 rounded-md">
                          v{ag.version}
                        </span>
                        {ag.isAccepted && (
                          <span className="inline-flex items-center gap-1 text-[11px] font-semibold text-emerald-600">
                            <CheckCircle2 className="w-3.5 h-3.5" />
                            Signed
                          </span>
                        )}
                      </div>
                      <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-[11px] text-slate-400 font-mono">
                        <span className="flex items-center gap-1">
                          <Hash className="w-3 h-3 text-slate-400" />
                          SHA256: {ag.contentHash.slice(0, 12)}...
                        </span>
                        {ag.acceptedAt && (
                          <span className="text-slate-500">
                            Accepted: {new Date(ag.acceptedAt).toLocaleString()}
                          </span>
                        )}
                      </div>
                    </div>
                  </div>

                  <button
                    type="button"
                    onClick={() => toggleExpand(ag.id)}
                    className="p-1 text-slate-400 hover:text-slate-600 transition-colors cursor-pointer"
                    aria-label="Toggle full agreement text"
                  >
                    {isExpanded ? <ChevronUp className="w-5 h-5" /> : <ChevronDown className="w-5 h-5" />}
                  </button>
                </div>

                {/* Expandable full text preview */}
                {isExpanded && (
                  <div className="mt-4 pt-4 border-t border-slate-100">
                    <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200 text-xs text-slate-700 leading-relaxed font-sans max-h-48 overflow-y-auto">
                      {ag.content}
                    </div>
                    <div className="mt-2 text-[10px] text-slate-400 font-mono break-all">
                      Full Content Hash (SHA-256): {ag.contentHash}
                    </div>
                  </div>
                )}

                {/* E-signature checkbox */}
                <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between gap-3">
                  <label className="flex items-start gap-2.5 cursor-pointer select-none">
                    <input
                      type="checkbox"
                      checked={ag.isAccepted}
                      disabled={ag.isAccepted || isSigning}
                      onChange={() => handleToggleAccept(ag)}
                      className="mt-0.5 w-4 h-4 rounded text-brand-600 border-slate-300 focus:ring-brand-500 cursor-pointer disabled:opacity-70"
                    />
                    <span className="text-xs text-slate-700">
                      {ag.isAccepted ? (
                        <span className="font-semibold text-emerald-700">
                          Digitally signed and accepted under Clickwrap Agreement Terms.
                        </span>
                      ) : (
                        <span>
                          I have read, understood, and agree to adhere to the{' '}
                          <span className="font-semibold text-slate-900">{ag.title}</span>.
                        </span>
                      )}
                    </span>
                  </label>

                  {!ag.isAccepted && (
                    <button
                      type="button"
                      onClick={() => handleToggleAccept(ag)}
                      disabled={isSigning}
                      className="px-3 py-1 bg-brand-600 hover:bg-brand-700 disabled:opacity-50 text-white text-xs font-semibold rounded-lg shadow-xs transition-all cursor-pointer shrink-0"
                    >
                      {isSigning ? 'Signing...' : 'Sign Agreement'}
                    </button>
                  )}
                </div>

                {errorMsg && (
                  <div className="mt-2 text-xs text-rose-600 flex items-center gap-1.5">
                    <AlertCircle className="w-3.5 h-3.5 shrink-0" />
                    <span>{errorMsg}</span>
                  </div>
                )}
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}
