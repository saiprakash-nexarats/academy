import React from 'react'
import { User, Sparkles, BookOpen, Target, Globe } from 'lucide-react'

export interface StudentInfoData {
  headline: string
  bio: string
  academicBackground: string
  careerGoals: string
  timezone: string
}

interface StepStudentInfoProps {
  data: StudentInfoData
  onChange: (field: keyof StudentInfoData, value: string) => void
  errors?: Partial<Record<keyof StudentInfoData, string>>
}

const COMMON_TIMEZONES = [
  'UTC',
  'America/New_York',
  'America/Chicago',
  'America/Denver',
  'America/Los_Angeles',
  'Europe/London',
  'Europe/Berlin',
  'Europe/Paris',
  'Asia/Dubai',
  'Asia/Kolkata',
  'Asia/Singapore',
  'Asia/Tokyo',
  'Australia/Sydney',
]

export const StepStudentInfo: React.FC<StepStudentInfoProps> = ({
  data,
  onChange,
  errors = {},
}) => {
  return (
    <div className="space-y-6">
      <div>
        <div className="flex items-center gap-2 mb-1">
          <div className="w-8 h-8 rounded-lg bg-brand-50 border border-brand-100 flex items-center justify-center text-brand-600">
            <User className="w-4 h-4" />
          </div>
          <h3 className="text-lg font-bold text-slate-900">Personal & Academic Profile</h3>
        </div>
        <p className="text-xs text-slate-500">
          Tell us about yourself and your educational background so mentors and cohort instructors can personalize your learning path.
        </p>
      </div>

      <div className="space-y-4">
        {/* Professional Headline */}
        <div>
          <label htmlFor="headline" className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
            Professional / Student Headline <span className="text-rose-500">*</span>
          </label>
          <div className="relative">
            <input
              id="headline"
              type="text"
              value={data.headline}
              onChange={(e) => onChange('headline', e.target.value)}
              placeholder="e.g. Aspiring Full-Stack Software Engineer | CS Undergraduate"
              maxLength={255}
              className={`w-full px-3.5 py-2.5 text-sm bg-white border ${
                errors.headline ? 'border-rose-400 focus:ring-rose-200' : 'border-slate-300 focus:border-brand-500 focus:ring-brand-200'
              } rounded-xl shadow-xs focus:ring-2 focus:outline-none transition-all`}
            />
            <div className="absolute right-3 top-2.5 text-slate-400">
              <Sparkles className="w-4 h-4" />
            </div>
          </div>
          {errors.headline && (
            <p className="mt-1 text-xs text-rose-500 font-medium">{errors.headline}</p>
          )}
          <p className="mt-1 text-[11px] text-slate-400">
            Brief one-line summary displayed on your academy cohort roster ({data.headline.length}/255).
          </p>
        </div>

        {/* Bio */}
        <div>
          <label htmlFor="bio" className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
            About You / Bio <span className="text-rose-500">*</span>
          </label>
          <textarea
            id="bio"
            rows={3}
            value={data.bio}
            onChange={(e) => onChange('bio', e.target.value)}
            placeholder="Share what drives you, what technologies you enjoy working with, and what you aim to achieve in this academy program."
            maxLength={2000}
            className={`w-full px-3.5 py-2.5 text-sm bg-white border ${
              errors.bio ? 'border-rose-400 focus:ring-rose-200' : 'border-slate-300 focus:border-brand-500 focus:ring-brand-200'
            } rounded-xl shadow-xs focus:ring-2 focus:outline-none transition-all resize-y`}
          />
          {errors.bio && (
            <p className="mt-1 text-xs text-rose-500 font-medium">{errors.bio}</p>
          )}
          <p className="mt-1 text-[11px] text-slate-400">
            A short introduction for your peers and mentor ({data.bio.length}/2000).
          </p>
        </div>

        {/* Academic Background */}
        <div>
          <label htmlFor="academicBackground" className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
            Academic Background
          </label>
          <div className="relative">
            <textarea
              id="academicBackground"
              rows={2}
              value={data.academicBackground}
              onChange={(e) => onChange('academicBackground', e.target.value)}
              placeholder="e.g. B.S. in Computer Science / High School Diploma / Self-taught Bootcamp Graduate"
              maxLength={2000}
              className="w-full px-3.5 py-2.5 text-sm bg-white border border-slate-300 focus:border-brand-500 focus:ring-2 focus:ring-brand-200 rounded-xl shadow-xs focus:outline-none transition-all resize-y"
            />
            <div className="absolute right-3 top-2.5 text-slate-400">
              <BookOpen className="w-4 h-4" />
            </div>
          </div>
          <p className="mt-1 text-[11px] text-slate-400">
            Degrees, certifications, or self-study experience.
          </p>
        </div>

        {/* Career Goals */}
        <div>
          <label htmlFor="careerGoals" className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
            Career Goals & Aspirations
          </label>
          <div className="relative">
            <textarea
              id="careerGoals"
              rows={2}
              value={data.careerGoals}
              onChange={(e) => onChange('careerGoals', e.target.value)}
              placeholder="e.g. Transition into Senior Backend Developer role, launch a SaaS startup, master Distributed Systems"
              maxLength={2000}
              className="w-full px-3.5 py-2.5 text-sm bg-white border border-slate-300 focus:border-brand-500 focus:ring-2 focus:ring-brand-200 rounded-xl shadow-xs focus:outline-none transition-all resize-y"
            />
            <div className="absolute right-3 top-2.5 text-slate-400">
              <Target className="w-4 h-4" />
            </div>
          </div>
        </div>

        {/* Timezone */}
        <div>
          <label htmlFor="timezone" className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
            Primary Learning Timezone <span className="text-rose-500">*</span>
          </label>
          <div className="relative">
            <select
              id="timezone"
              value={data.timezone || 'UTC'}
              onChange={(e) => onChange('timezone', e.target.value)}
              className="w-full px-3.5 py-2.5 text-sm bg-white border border-slate-300 focus:border-brand-500 focus:ring-2 focus:ring-brand-200 rounded-xl shadow-xs focus:outline-none transition-all appearance-none cursor-pointer"
            >
              {COMMON_TIMEZONES.map((tz) => (
                <option key={tz} value={tz}>
                  {tz}
                </option>
              ))}
            </select>
            <div className="absolute right-3.5 top-3 pointer-events-none text-slate-400">
              <Globe className="w-4 h-4" />
            </div>
          </div>
          <p className="mt-1 text-[11px] text-slate-400">
            Used to schedule mentor office hours, live lectures, and project deadlines.
          </p>
        </div>
      </div>
    </div>
  )
}
