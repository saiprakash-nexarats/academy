import React from 'react'
import { Link } from 'react-router-dom'
import { Star, ArrowRight, Award } from 'lucide-react'
import { motion } from 'framer-motion'
import { MENTORS_DATA } from '@/data'
import { CompanyIcon } from '@/components/common'
import { useModals } from '@/context'
import { cn } from '@/lib/utils'

interface MentorsSectionProps {
  className?: string
}

export const MentorsSection: React.FC<MentorsSectionProps> = ({ className }) => {
  const { openDemoModal } = useModals()

  return (
    <section className={cn('py-8 sm:py-12', className)}>
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-4 mb-8">
        <div>
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-brand-50 text-brand-600 border border-brand-200/90 text-xs font-bold uppercase tracking-wider shadow-sm mb-3">
            <Award className="w-3.5 h-3.5 text-brand-600" />
            <span>Expert Mentorship</span>
          </div>
          <h2 className="font-heading font-extrabold text-2xl sm:text-3xl lg:text-4xl text-slate-900 tracking-tight">
            Learn Directly From <span className="text-brand-600">Industry Leaders</span>
          </h2>
          <p className="text-sm text-slate-600 mt-2 max-w-2xl leading-relaxed">
            Personalized 1-on-1 feedback, weekly code reviews, and career guidance from senior staff engineers at top tech companies.
          </p>
        </div>

        <Link
          to="/mentors"
          className="group inline-flex items-center gap-2 px-5 py-2.5 rounded-full border border-slate-300 hover:border-brand-500 bg-white hover:bg-slate-50 text-slate-800 hover:text-brand-600 font-semibold text-xs sm:text-sm shadow-sm transition-all cursor-pointer self-start md:self-auto shrink-0"
        >
          <span>View All 80+ Mentors</span>
          <ArrowRight className="w-3.5 h-3.5 text-brand-600 transition-transform group-hover:translate-x-1" />
        </Link>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5">
        {MENTORS_DATA.slice(0, 4).map((mentor, index) => (
          <motion.div
            key={mentor.id}
            initial={{ opacity: 0, y: 15 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.4, delay: index * 0.08 }}
            className="group bg-white rounded-2xl border border-slate-200/90 shadow-card hover:shadow-card-hover hover:border-brand-300 transition-all p-5 flex flex-col justify-between"
          >
            <div>
              <div className="flex items-center gap-3.5 mb-4">
                <img
                  src={mentor.avatar}
                  alt={mentor.name}
                  className="w-14 h-14 rounded-full object-cover border-2 border-brand-100 shadow-sm shrink-0"
                />
                <div className="min-w-0">
                  <h3 className="font-heading font-bold text-base text-slate-900 truncate group-hover:text-brand-600 transition-colors">
                    {mentor.name}
                  </h3>
                  <p className="text-xs text-slate-500 truncate">{mentor.role}</p>
                  <div className="inline-flex items-center gap-1 mt-0.5 px-2 py-0.5 rounded-md bg-blue-50 text-brand-700 font-bold text-[10px]">
                    <CompanyIcon name={mentor.company} className="w-3 h-3 shrink-0" />
                    <span>{mentor.company}</span>
                  </div>
                </div>
              </div>

              <p className="text-xs text-slate-600 leading-relaxed line-clamp-2 mb-4">
                {mentor.bio}
              </p>

              <div className="flex flex-wrap gap-1 mb-4">
                {mentor.specialization.slice(0, 2).map((skill) => (
                  <span
                    key={skill}
                    className="px-2 py-0.5 rounded-md bg-slate-100 text-slate-600 text-[10px] font-medium"
                  >
                    {skill}
                  </span>
                ))}
              </div>
            </div>

            <div className="pt-3 border-t border-slate-100 flex items-center justify-between text-xs text-slate-500">
              <div className="flex items-center gap-1 text-amber-500 font-bold">
                <Star className="w-3.5 h-3.5 fill-amber-400 text-amber-400" />
                <span>{mentor.rating}</span>
                <span className="text-slate-400 font-normal text-[10px]">({mentor.studentsMentored}+)</span>
              </div>

              <button
                type="button"
                onClick={openDemoModal}
                className="text-xs font-bold text-brand-600 hover:text-brand-700 cursor-pointer hover:underline"
              >
                Book 1:1 Call
              </button>
            </div>
          </motion.div>
        ))}
      </div>
    </section>
  )
}
