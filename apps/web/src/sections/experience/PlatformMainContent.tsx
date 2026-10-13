import React from 'react'
import { Search, Bell, Play } from 'lucide-react'
import studentAnanyaImg from '@/assets/student-ananya.jpg'
import { UPCOMING_LIVE_CLASSES, ANANYA_STUDENT_PROGRESS } from '@/data'
import { useModals } from '@/context'
import { cn } from '@/lib/utils'

interface PlatformMainContentProps {
  className?: string
}

export const PlatformMainContent: React.FC<PlatformMainContentProps> = ({ className }) => {
  const { openDemoModal, openLoginModal } = useModals()

  return (
    <div className={cn('flex-1 bg-white p-4 sm:p-5 flex flex-col justify-between overflow-hidden', className)}>
      {/* Top Header Bar */}
      <div className="flex items-center justify-between gap-3 pb-3 border-b border-slate-100">
        {/* Search Mockup */}
        <div className="relative flex-1 max-w-xs">
          <Search className="w-3.5 h-3.5 text-slate-400 absolute left-2.5 top-2.5" />
          <input
            type="text"
            readOnly
            value="Search courses, mentors..."
            className="w-full pl-8 pr-3 py-1.5 text-[11px] bg-slate-50 border border-slate-200/80 rounded-lg text-slate-400 focus:outline-none pointer-events-none truncate"
          />
        </div>

        {/* User and Notification Controls */}
        <div className="flex items-center gap-2.5 shrink-0">
          <div
            onClick={openDemoModal}
            className="relative p-1.5 text-slate-500 hover:text-slate-800 rounded-lg hover:bg-slate-100 cursor-pointer"
          >
            <Bell className="w-4 h-4" />
            <span className="absolute top-1 right-1 w-2 h-2 bg-rose-500 rounded-full ring-2 ring-white" />
          </div>

          <div
            onClick={openLoginModal}
            className="flex items-center gap-2 cursor-pointer group"
          >
            <div className="relative">
              <img
                src={studentAnanyaImg}
                alt="Ananya Sharma"
                className="w-7 h-7 rounded-full object-cover border border-slate-200 group-hover:border-brand-500 shadow-sm transition-colors"
              />
              <span className="absolute bottom-0 right-0 w-2 h-2 bg-emerald-500 rounded-full ring-1 ring-white" />
            </div>
          </div>
        </div>
      </div>

      {/* Greeting and Progress Banner */}
      <div className="py-2.5 sm:py-3">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-1.5 sm:gap-2">
          <div>
            <h4 className="font-heading font-extrabold text-sm sm:text-base text-slate-900 leading-tight">
              {ANANYA_STUDENT_PROGRESS.greeting}
            </h4>
            <p className="text-[11px] text-slate-500 mt-0.5">
              Keep learning, keep growing.
            </p>
          </div>

          <div className="sm:text-right">
            <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">
              Overall Progress
            </span>
            <div className="text-xs font-bold text-brand-600">
              {ANANYA_STUDENT_PROGRESS.overallProgress}
            </div>
          </div>
        </div>

        {/* Progress Bar Widget */}
        <div className="mt-2 p-2.5 sm:p-3 rounded-xl bg-slate-50 border border-slate-200/80">
          <div className="flex items-center justify-between text-[11px] mb-1.5">
            <span className="font-bold text-slate-800">My Progress</span>
            <span className="text-slate-500 font-medium">{ANANYA_STUDENT_PROGRESS.lessonsCompletedText}</span>
          </div>
          <div className="w-full h-2 bg-slate-200 rounded-full overflow-hidden">
            <div
              className="h-full bg-brand-600 rounded-full transition-all duration-500"
              style={{ width: `${ANANYA_STUDENT_PROGRESS.progressPercent}%` }}
            />
          </div>
        </div>
      </div>

      {/* Dual Cards Grid: Continue Learning + Upcoming Live Classes */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-1">
        {/* Continue Learning Widget */}
        <div
          onClick={openDemoModal}
          className="p-3 rounded-xl border border-slate-200/80 bg-white flex flex-col justify-between hover:border-brand-400 hover:shadow-sm transition-all cursor-pointer group"
        >
          <div className="text-xs font-bold text-slate-900 mb-2">
            Continue Learning
          </div>
          <div className="flex items-center gap-3">
            <div className="w-11 h-11 rounded-xl bg-brand-600 hover:bg-brand-700 flex items-center justify-center text-white shrink-0 shadow-sm relative transition-colors cursor-pointer">
              <Play className="w-4 h-4 fill-white" />
            </div>
            <div className="min-w-0">
              <div className="text-xs font-bold text-slate-900 group-hover:text-brand-600 transition-colors truncate">
                {ANANYA_STUDENT_PROGRESS.currentLesson.title}
              </div>
              <div className="text-[10px] text-slate-500 mt-0.5 truncate">
                {ANANYA_STUDENT_PROGRESS.currentLesson.course}
              </div>
              <div className="text-[10px] text-brand-600 font-semibold mt-1">
                {ANANYA_STUDENT_PROGRESS.currentLesson.lessonStep}
              </div>
            </div>
          </div>
        </div>

        {/* Upcoming Live Classes Widget */}
        <div className="p-3 rounded-xl border border-slate-200/80 bg-white space-y-2 hover:border-brand-200 transition-colors">
          <div className="flex items-center justify-between text-xs">
            <span className="font-bold text-slate-900">Upcoming Live Classes</span>
            <span
              onClick={openDemoModal}
              className="text-[10px] font-bold text-brand-600 cursor-pointer hover:underline"
            >
              See All
            </span>
          </div>

          <div className="space-y-1.5">
            {UPCOMING_LIVE_CLASSES.map((item) => {
              const Icon = item.icon
              return (
                <div key={item.id} className="flex items-center justify-between gap-2 p-1.5 rounded-lg bg-slate-50 text-[11px]">
                  <div className="flex items-center gap-2 min-w-0">
                    <div className={cn('w-6 h-6 rounded-md flex items-center justify-center shrink-0', item.iconBgColor, item.iconTextColor)}>
                      <Icon className="w-3.5 h-3.5" />
                    </div>
                    <div className="min-w-0">
                      <div className="font-bold text-slate-900 truncate">{item.title}</div>
                      <div className="text-[10px] text-slate-500">{item.time}</div>
                    </div>
                  </div>
                  <button
                    type="button"
                    onClick={openDemoModal}
                    className={cn(
                      'px-2.5 py-1 font-semibold text-[10px] rounded-md shrink-0 cursor-pointer active:scale-95 transition-all',
                      item.actionVariant === 'primary'
                        ? 'bg-brand-600 hover:bg-brand-700 text-white font-bold shadow-sm'
                        : 'border border-slate-200 hover:border-slate-300 text-slate-700 hover:bg-white'
                    )}
                  >
                    {item.actionLabel}
                  </button>
                </div>
              )
            })}
          </div>
        </div>
      </div>
    </div>
  )
}
