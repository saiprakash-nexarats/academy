import React from 'react'
import { X, Award, CheckCircle, Download, Share2 } from 'lucide-react'
import { motion, AnimatePresence } from 'framer-motion'
import { Button } from '@/components/ui/Button'
import { Logo } from '@/components/common/Logo'

interface CertificateModalProps {
  isOpen: boolean
  onClose: () => void
}

export const CertificateModal: React.FC<CertificateModalProps> = ({ isOpen, onClose }) => {
  if (!isOpen) return null

  return (
    <AnimatePresence>
      <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm">
        <motion.div
          initial={{ opacity: 0, scale: 0.95, y: 15 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          exit={{ opacity: 0, scale: 0.95, y: 15 }}
          className="relative w-full max-w-xl bg-white rounded-3xl shadow-2xl border border-slate-100 overflow-hidden"
        >
          {/* Close button */}
          <button
            type="button"
            onClick={onClose}
            className="absolute top-5 right-5 p-2 rounded-xl text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition-colors z-20 cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>

          <div className="p-6 sm:p-8 space-y-6">
            {/* Header badge */}
            <div className="flex items-center gap-2">
              <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200 text-xs font-bold">
                <CheckCircle className="w-3.5 h-3.5" />
                Verified & Accredited Credential
              </span>
            </div>

            {/* Certificate Graphic Card */}
            <div className="relative rounded-2xl border-4 border-double border-brand-200 bg-gradient-to-br from-blue-50/60 via-white to-sky-50/40 p-6 sm:p-8 text-center space-y-4 shadow-card">
              <div className="flex justify-center">
                <Logo />
              </div>

              <div>
                <span className="text-[10px] uppercase tracking-widest text-slate-400 font-bold">
                  Certificate of Achievement
                </span>
                <p className="text-xs text-slate-500 mt-1">
                  This certifies that
                </p>
                <h3 className="font-heading font-extrabold text-2xl text-slate-900 mt-1 text-brand-700">
                  Ananya Sharma
                </h3>
                <p className="text-xs text-slate-600 mt-1">
                  has successfully completed the foundational coursework in
                </p>
                <div className="text-base font-bold text-slate-900 mt-1">
                  Web Development Basics & Modern React
                </div>
              </div>

              <div className="pt-4 border-t border-slate-200/60 flex items-center justify-between text-[11px] text-slate-500">
                <div className="text-left">
                  <div className="font-bold text-slate-800">Arjun Mehta</div>
                  <div className="text-[10px]">Lead Mentor, Nexarats</div>
                </div>

                <div className="w-12 h-12 rounded-xl bg-amber-50 border border-amber-200 text-amber-500 flex items-center justify-center">
                  <Award className="w-7 h-7" />
                </div>

                <div className="text-right">
                  <div className="font-mono text-[10px] text-slate-400">ID: NX-2026-9481</div>
                  <div className="text-[10px] text-emerald-600 font-bold">Grade: 98% (Pass)</div>
                </div>
              </div>
            </div>

            {/* Actions */}
            <div className="flex items-center gap-3">
              <Button
                variant="primary"
                onClick={onClose}
                iconLeft={<Download className="w-4 h-4" />}
                className="flex-1"
              >
                Download PDF
              </Button>
              <Button
                variant="outline"
                iconLeft={<Share2 className="w-4 h-4" />}
                onClick={() => alert('Certificate link copied to clipboard!')}
              >
                Share Credential
              </Button>
            </div>
          </div>
        </motion.div>
      </div>
    </AnimatePresence>
  )
}
