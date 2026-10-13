import React from 'react'
import { Link } from 'react-router-dom'
import { ArrowRight, GraduationCap, Mail, Phone, MapPin, Heart } from 'lucide-react'
import { Logo } from '@/components/common/Logo'
import { Button } from '@/components/ui/Button'
import { FOOTER_QUICK_LINKS } from '@/data/navigation'
import { useModals } from '@/context'

const CURRENT_YEAR = new Date().getFullYear()

interface FooterProps {
  showBanner?: boolean
}

export const Footer: React.FC<FooterProps> = ({ showBanner = false }) => {
  const { openEnrollModal } = useModals()
  return (
    <footer className="bg-[#111C33] text-slate-300 border-t border-slate-800/80">
      {/* Optional Pre-Footer CTA Banner if enabled */}
      {showBanner && (
        <div className="relative -top-12 academy-container">
          <div className="rounded-3xl bg-gradient-to-r from-brand-700 via-brand-600 to-blue-600 p-8 md:p-12 text-white shadow-xl flex flex-col md:flex-row items-center justify-between gap-6 overflow-hidden relative">
            <div className="absolute -right-10 -bottom-10 w-64 h-64 bg-white/10 rounded-full blur-2xl pointer-events-none" />

            <div className="flex items-center gap-5 z-10">
              <div className="w-16 h-16 rounded-2xl bg-white/15 backdrop-blur-md flex items-center justify-center shrink-0 border border-white/20">
                <GraduationCap className="w-9 h-9 text-white" />
              </div>
              <div>
                <h3 className="text-2xl md:text-3xl font-extrabold font-heading tracking-tight">
                  Start Learning With Confidence
                </h3>
                <p className="text-blue-100 text-sm md:text-base mt-1 max-w-xl">
                  Join thousands of learners and take the next step towards a brighter future.
                </p>
              </div>
            </div>

            <div className="z-10 shrink-0 w-full md:w-auto">
              <Button
                variant="secondary"
                size="lg"
                onClick={() => openEnrollModal()}
                iconRight={<ArrowRight className="w-4 h-4 text-brand-600" />}
                className="bg-white text-brand-700 hover:bg-blue-50 font-bold w-full md:w-auto px-8 shadow-md"
              >
                Get Started
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* Main Footer Links */}
      <div className="academy-container pb-12 pt-16">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-10">
          {/* Brand Info Column */}
          <div className="lg:col-span-2 space-y-4">
            <Logo inverted />
            <p className="text-slate-400 text-sm leading-relaxed max-w-sm">
              Empowering ambitious individuals through career-aligned curriculum, industry mentors,
              and live hands-on project experience.
            </p>
            <div className="space-y-2 text-xs text-slate-400 pt-2">
              <div className="flex items-center gap-2">
                <MapPin className="w-4 h-4 text-brand-400 shrink-0" />
                <span>Tech Knowledge Park, Cyber Hub, Innovation Way</span>
              </div>
              <div className="flex items-center gap-2">
                <Mail className="w-4 h-4 text-brand-400 shrink-0" />
                <span>admissions@nexarats.academy</span>
              </div>
              <div className="flex items-center gap-2">
                <Phone className="w-4 h-4 text-brand-400 shrink-0" />
                <span>+1 (800) 845-NEXA</span>
              </div>
            </div>
          </div>

          {/* Quick Links Columns */}
          {FOOTER_QUICK_LINKS.map((column) => (
            <div key={column.title} className="space-y-3">
              <h4 className="text-sm font-semibold text-white tracking-wider uppercase">
                {column.title}
              </h4>
              <ul className="space-y-2.5">
                {column.links.map((link) => (
                  <li key={link.label}>
                    {link.href.startsWith('/') && !link.href.includes('#') ? (
                      <Link
                        to={link.href}
                        className="text-sm text-slate-400 hover:text-white transition-colors"
                      >
                        {link.label}
                      </Link>
                    ) : (
                      <a
                        href={link.href}
                        className="text-sm text-slate-400 hover:text-white transition-colors"
                      >
                        {link.label}
                      </a>
                    )}
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        {/* Bottom Bar */}
        <div className="mt-12 pt-8 border-t border-slate-800 flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-slate-400">
          <p>© {CURRENT_YEAR} Nexarats Academy. All rights reserved.</p>
          <div className="flex items-center gap-6">
            <a href="#hero" className="hover:text-white transition-colors">
              Privacy Policy
            </a>
            <a href="#hero" className="hover:text-white transition-colors">
              Terms of Service
            </a>
            <a href="#hero" className="hover:text-white transition-colors">
              Cookie Settings
            </a>
          </div>
          <p className="flex items-center gap-1">
            Built with <Heart className="w-3.5 h-3.5 text-rose-500 fill-rose-500 inline" /> for learners worldwide
          </p>
        </div>
      </div>
    </footer>
  )
}
