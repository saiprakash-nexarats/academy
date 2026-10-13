import React, { useState } from 'react'
import { X, Play, CheckCircle2, Video, Calendar, Clock } from 'lucide-react'
import { motion, AnimatePresence } from 'framer-motion'
import { Button } from '@/components/ui/Button'
import heroStudentImg from '@/assets/hero-student.jpg'

interface DemoModalProps {
  isOpen: boolean
  onClose: () => void
}

export const DemoModal: React.FC<DemoModalProps> = ({ isOpen, onClose }) => {
  const [isPlaying, setIsPlaying] = useState(false)
  const [bookingConfirmed, setBookingConfirmed] = useState(false)

  if (!isOpen) return null

  return (
    <AnimatePresence>
      <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm">
        <motion.div
          initial={{ opacity: 0, scale: 0.95, y: 15 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          exit={{ opacity: 0, scale: 0.95, y: 15 }}
          className="relative w-full max-w-2xl bg-white rounded-3xl shadow-2xl border border-slate-100 overflow-hidden"
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
            {/* Header */}
            <div>
              <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-50 text-brand-600 border border-blue-100 text-xs font-bold uppercase tracking-wider mb-2">
                <Video className="w-3.5 h-3.5" />
                <span>Interactive Platform Demo</span>
              </div>
              <h3 className="font-heading font-extrabold text-2xl text-slate-900">
                Experience Nexarats Academy Live
              </h3>
              <p className="text-xs sm:text-sm text-slate-600 mt-1">
                See how our interactive live classrooms, project code sandboxes, and mentor guidance work.
              </p>
            </div>

            {/* Video Player Mockup */}
            <div className="relative rounded-2xl overflow-hidden aspect-video bg-slate-900 shadow-elevated border border-slate-200">
              <img
                src={heroStudentImg}
                alt="Demo preview"
                className="w-full h-full object-cover opacity-60"
              />

              {!isPlaying ? (
                <div className="absolute inset-0 flex flex-col items-center justify-center bg-slate-900/40 gap-3">
                  <button
                    type="button"
                    onClick={() => setIsPlaying(true)}
                    className="w-16 h-16 rounded-full bg-brand-600 hover:bg-brand-700 text-white flex items-center justify-center shadow-button-glow hover:scale-110 active:scale-95 transition-all cursor-pointer"
                  >
                    <Play className="w-7 h-7 fill-white ml-1" />
                  </button>
                  <span className="text-white text-xs font-bold tracking-wide">
                    Watch 2-Minute Platform Walkthrough
                  </span>
                </div>
              ) : (
                <div className="absolute inset-0 flex flex-col items-center justify-center bg-slate-900 p-6 text-center text-white space-y-3">
                  <Video className="w-10 h-10 text-brand-400 animate-pulse" />
                  <h4 className="font-bold text-base">Streaming Demo Session</h4>
                  <p className="text-xs text-slate-300 max-w-sm">
                    In a production environment, this streams the live interactive classroom simulator with terminal sandbox and code pairing.
                  </p>
                  <Button
                    variant="soft"
                    size="sm"
                    onClick={() => setIsPlaying(false)}
                    className="mt-2 text-xs"
                  >
                    Pause Video
                  </Button>
                </div>
              )}
            </div>

            {/* Next Steps / Schedule 1-on-1 */}
            <div className="p-4 rounded-2xl bg-slate-50 border border-slate-200/80 flex flex-col sm:flex-row items-center justify-between gap-4">
              <div className="space-y-1 text-center sm:text-left">
                <div className="text-xs font-bold text-slate-900 flex items-center justify-center sm:justify-start gap-1.5">
                  <Calendar className="w-4 h-4 text-brand-600" />
                  <span>Prefer a 1-on-1 Personalized Demo?</span>
                </div>
                <div className="text-[11px] text-slate-500 flex items-center justify-center sm:justify-start gap-1">
                  <Clock className="w-3.5 h-3.5" />
                  <span>Next available slot: Today at 4:30 PM (15 mins)</span>
                </div>
              </div>

              <Button
                variant="primary"
                size="sm"
                onClick={() => setBookingConfirmed(true)}
                disabled={bookingConfirmed}
                className="shrink-0 w-full sm:w-auto"
              >
                {bookingConfirmed ? (
                  <span className="flex items-center gap-1.5 text-white">
                    <CheckCircle2 className="w-4 h-4" /> Booked!
                  </span>
                ) : (
                  'Schedule 1-on-1'
                )}
              </Button>
            </div>
          </div>
        </motion.div>
      </div>
    </AnimatePresence>
  )
}
