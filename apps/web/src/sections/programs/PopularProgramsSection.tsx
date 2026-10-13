import React from 'react'
import { Link } from 'react-router-dom'
import { ArrowRight, AlertCircle, RefreshCw } from 'lucide-react'
import { motion } from 'framer-motion'
import { ProgramCard } from './ProgramCard'
import { ProgramCardSkeleton } from './ProgramCardSkeleton'
import { usePrograms } from '@/lib/catalogApi'
import { resolveProgramImage } from '@/lib/programAssets'
import { cn } from '@/lib/utils'

interface PopularProgramsSectionProps {
  className?: string
}

export const PopularProgramsSection: React.FC<PopularProgramsSectionProps> = ({
  className,
}) => {
  const { data, isLoading, isError, refetch } = usePrograms({ featured: true })

  const programs = data?.items || []

  return (
    <section className={cn('py-1 sm:py-2', className)}>
      <div className="flex flex-col xl:flex-row gap-6 xl:gap-8 items-start">
        {/* Left Header Column */}
        <motion.div
          initial={{ opacity: 0, x: -15 }}
          whileInView={{ opacity: 1, x: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.5 }}
          className="w-full xl:w-60 2xl:w-64 shrink-0 space-y-4"
        >
          <div className="inline-flex items-center px-3 py-1 rounded-full bg-brand-50 text-brand-600 border border-brand-200/90 text-xs font-bold uppercase tracking-wider shadow-sm">
            Our Programs
          </div>

          <h2 className="font-heading font-extrabold text-2xl sm:text-3xl lg:text-4xl text-slate-900 tracking-tight leading-tight">
            Popular Programs
          </h2>

          <p className="text-sm text-slate-600 leading-relaxed">
            Learn in-demand skills with 100% placement-backed programs, real enterprise capstones, and direct referral drives.
          </p>

          <div className="pt-2">
            <Link
              to="/programs"
              id="view-all-programs-btn"
              className="group inline-flex items-center gap-2.5 px-5 py-2.5 rounded-full border border-slate-300 hover:border-brand-600 text-slate-800 hover:text-brand-600 font-semibold text-xs sm:text-sm bg-white hover:bg-slate-50 shadow-sm hover:shadow transition-all cursor-pointer active:scale-95"
            >
              <span>View All Programs</span>
              <ArrowRight className="w-3.5 h-3.5 text-brand-600 transition-transform group-hover:translate-x-1" />
            </Link>
          </div>
        </motion.div>

        {/* Right 4 Cards Grid */}
        <div className="flex-1 w-full grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-4.5">
          {isLoading && (
            <>
              {[0, 1, 2, 3].map((idx) => (
                <ProgramCardSkeleton key={idx} />
              ))}
            </>
          )}

          {isError && (
            <div className="col-span-full p-6 rounded-2xl bg-slate-50 border border-slate-200 text-center space-y-3">
              <AlertCircle className="w-6 h-6 text-amber-500 mx-auto" />
              <p className="text-xs text-slate-600">Unable to load featured programs at this moment.</p>
              <button
                type="button"
                onClick={() => refetch()}
                className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-bold text-brand-600 bg-brand-50 hover:bg-brand-100 transition-colors cursor-pointer"
              >
                <RefreshCw className="w-3 h-3" />
                <span>Retry</span>
              </button>
            </div>
          )}

          {!isLoading && !isError && programs.length === 0 && (
            <div className="col-span-full p-6 rounded-2xl bg-slate-50 border border-slate-200 text-center">
              <p className="text-xs text-slate-500">No featured programs currently published.</p>
            </div>
          )}

          {!isLoading &&
            !isError &&
            programs.map((program, index) => (
              <motion.div
                key={program.id}
                initial={{ opacity: 0, y: 15 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.4, delay: index * 0.08 }}
                className="h-full"
              >
                <ProgramCard
                  id={program.id}
                  slug={program.slug}
                  title={program.title}
                  description={program.shortDescription}
                  image={resolveProgramImage(program.imageAssetKey)}
                  duration={program.durationLabel}
                  level={program.level}
                  badge={program.badge ?? undefined}
                  category={program.categoryName}
                />
              </motion.div>
            ))}
        </div>
      </div>
    </section>
  )
}
