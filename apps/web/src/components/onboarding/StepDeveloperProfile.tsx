import React from 'react'
import { Link2, ExternalLink, CheckCircle2, AlertCircle } from 'lucide-react'
import { FaGithub, FaLinkedin } from 'react-icons/fa'

export interface DeveloperProfileData {
  githubUrl: string
  linkedinUrl: string
}

interface StepDeveloperProfileProps {
  data: DeveloperProfileData
  onChange: (field: keyof DeveloperProfileData, value: string) => void
  errors?: Partial<Record<keyof DeveloperProfileData, string>>
}

const GITHUB_REGEX = /^(https?:\/\/)?(www\.)?github\.com\/[A-Za-z0-9_.-]+\/?$/
const LINKEDIN_REGEX = /^(https?:\/\/)?(www\.)?linkedin\.com\/in\/[A-Za-z0-9_.-]+\/?$/

export const StepDeveloperProfile: React.FC<StepDeveloperProfileProps> = ({
  data,
  onChange,
  errors = {},
}) => {
  const isValidGithub = !data.githubUrl || GITHUB_REGEX.test(data.githubUrl.trim())
  const isValidLinkedin = !data.linkedinUrl || LINKEDIN_REGEX.test(data.linkedinUrl.trim())
  const hasAtLeastOne = Boolean(data.githubUrl.trim() || data.linkedinUrl.trim())

  return (
    <div className="space-y-6">
      <div>
        <div className="flex items-center gap-2 mb-1">
          <div className="w-8 h-8 rounded-lg bg-brand-50 border border-brand-100 flex items-center justify-center text-brand-600">
            <Link2 className="w-4 h-4" />
          </div>
          <h3 className="text-lg font-bold text-slate-900">Developer & Professional Links</h3>
        </div>
        <p className="text-xs text-slate-500">
          Connect your GitHub repository account and LinkedIn profile. At least one professional link is required so mentors can review your code repositories and track your career growth.
        </p>
      </div>

      <div className="space-y-5">
        {/* GitHub URL */}
        <div>
          <label htmlFor="githubUrl" className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
            GitHub Profile URL
          </label>
          <div className="relative">
            <div className="absolute left-3.5 top-3 text-slate-700">
              <FaGithub className="w-4 h-4" />
            </div>
            <input
              id="githubUrl"
              type="url"
              value={data.githubUrl}
              onChange={(e) => onChange('githubUrl', e.target.value)}
              placeholder="https://github.com/your-username"
              maxLength={255}
              className={`w-full pl-10 pr-10 py-2.5 text-sm bg-white border ${
                errors.githubUrl || (!isValidGithub && data.githubUrl)
                  ? 'border-rose-400 focus:ring-rose-200'
                  : 'border-slate-300 focus:border-brand-500 focus:ring-brand-200'
              } rounded-xl shadow-xs focus:ring-2 focus:outline-none transition-all`}
            />
            {data.githubUrl && isValidGithub && (
              <div className="absolute right-3.5 top-3 text-emerald-500">
                <CheckCircle2 className="w-4 h-4" />
              </div>
            )}
          </div>
          {(errors.githubUrl || (!isValidGithub && data.githubUrl)) && (
            <p className="mt-1 text-xs text-rose-500 font-medium">
              {errors.githubUrl || 'Please enter a valid GitHub profile URL (e.g. https://github.com/octocat).'}
            </p>
          )}
          {data.githubUrl && isValidGithub && (
            <a
              href={data.githubUrl.startsWith('http') ? data.githubUrl : `https://${data.githubUrl}`}
              target="_blank"
              rel="noopener noreferrer"
              className="mt-1.5 inline-flex items-center gap-1 text-[11px] text-brand-600 hover:underline"
            >
              Verify GitHub link <ExternalLink className="w-3 h-3" />
            </a>
          )}
        </div>

        {/* LinkedIn URL */}
        <div>
          <label htmlFor="linkedinUrl" className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
            LinkedIn Profile URL
          </label>
          <div className="relative">
            <div className="absolute left-3.5 top-3 text-[#0a66c2]">
              <FaLinkedin className="w-4 h-4" />
            </div>
            <input
              id="linkedinUrl"
              type="url"
              value={data.linkedinUrl}
              onChange={(e) => onChange('linkedinUrl', e.target.value)}
              placeholder="https://linkedin.com/in/your-profile"
              maxLength={255}
              className={`w-full pl-10 pr-10 py-2.5 text-sm bg-white border ${
                errors.linkedinUrl || (!isValidLinkedin && data.linkedinUrl)
                  ? 'border-rose-400 focus:ring-rose-200'
                  : 'border-slate-300 focus:border-brand-500 focus:ring-brand-200'
              } rounded-xl shadow-xs focus:ring-2 focus:outline-none transition-all`}
            />
            {data.linkedinUrl && isValidLinkedin && (
              <div className="absolute right-3.5 top-3 text-emerald-500">
                <CheckCircle2 className="w-4 h-4" />
              </div>
            )}
          </div>
          {(errors.linkedinUrl || (!isValidLinkedin && data.linkedinUrl)) && (
            <p className="mt-1 text-xs text-rose-500 font-medium">
              {errors.linkedinUrl || 'Please enter a valid LinkedIn profile URL (e.g. https://linkedin.com/in/username).'}
            </p>
          )}
          {data.linkedinUrl && isValidLinkedin && (
            <a
              href={data.linkedinUrl.startsWith('http') ? data.linkedinUrl : `https://${data.linkedinUrl}`}
              target="_blank"
              rel="noopener noreferrer"
              className="mt-1.5 inline-flex items-center gap-1 text-[11px] text-brand-600 hover:underline"
            >
              Verify LinkedIn link <ExternalLink className="w-3 h-3" />
            </a>
          )}
        </div>

        {/* Status Callout */}
        <div
          className={`p-3.5 rounded-xl border flex items-start gap-3 ${
            hasAtLeastOne
              ? 'bg-emerald-50/50 border-emerald-200 text-emerald-800'
              : 'bg-amber-50/50 border-amber-200 text-amber-800'
          }`}
        >
          {hasAtLeastOne ? (
            <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0 mt-0.5" />
          ) : (
            <AlertCircle className="w-4 h-4 text-amber-600 shrink-0 mt-0.5" />
          )}
          <div className="text-xs">
            {hasAtLeastOne ? (
              <span className="font-semibold">Developer profile requirement satisfied.</span>
            ) : (
              <span>
                Please provide at least one developer or professional link (GitHub or LinkedIn) to proceed.
              </span>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
