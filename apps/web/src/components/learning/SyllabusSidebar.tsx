import React from 'react'
import { CheckCircle2, PlayCircle, Clock } from 'lucide-react'
import type { LearnerModuleDto, LearnerLessonDto } from '@nexarats/api-contracts'
import { cn } from '@/lib/utils'

interface SyllabusSidebarProps {
  modules: LearnerModuleDto[]
  activeLessonId: string
  overallProgressPercentage: number
  totalLessonsCount: number
  completedLessonsCount: number
  onSelectLesson: (lessonId: string) => void
}

export const SyllabusSidebar: React.FC<SyllabusSidebarProps> = ({
  modules,
  activeLessonId,
  overallProgressPercentage,
  totalLessonsCount,
  completedLessonsCount,
  onSelectLesson,
}) => {
  return (
    <div className="flex flex-col h-full bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
      {/* Sidebar Header & Course Progress */}
      <div className="p-5 border-b border-slate-100 bg-slate-50/70">
        <div className="flex items-center justify-between mb-2">
          <span className="text-xs font-bold uppercase tracking-wider text-slate-500">
            Course Syllabus
          </span>
          <span className="text-xs font-bold text-brand-600">
            {overallProgressPercentage}% Complete
          </span>
        </div>
        <div className="w-full h-2 bg-slate-200 rounded-full overflow-hidden mb-2">
          <div
            className="h-full bg-brand-600 rounded-full transition-all duration-500 ease-out"
            style={{ width: `${overallProgressPercentage}%` }}
          />
        </div>
        <div className="flex items-center justify-between text-[11px] text-slate-500">
          <span>{completedLessonsCount} of {totalLessonsCount} completed</span>
          <span className="font-semibold text-slate-700">{modules.length} Modules</span>
        </div>
      </div>

      {/* Module List */}
      <div className="flex-1 overflow-y-auto divide-y divide-slate-100 p-2 space-y-2">
        {modules.map((module) => (
          <div key={module.moduleId} className="pt-2">
            <div className="px-3 py-2 flex items-center justify-between text-xs font-bold text-slate-800">
              <span className="truncate">
                Module {module.moduleNumber}: {module.title}
              </span>
              <span className="text-[10px] font-semibold text-slate-400 shrink-0 ml-2">
                {module.durationLabel}
              </span>
            </div>

            {/* Lessons List in Module */}
            <div className="space-y-1 mt-1">
              {module.lessons.map((lesson: LearnerLessonDto) => {
                const isActive = lesson.lessonId === activeLessonId

                return (
                  <button
                    key={lesson.lessonId}
                    onClick={() => onSelectLesson(lesson.lessonId)}
                    className={cn(
                      'w-full text-left px-3 py-2.5 rounded-xl transition-all flex items-center gap-3 cursor-pointer select-none text-xs',
                      isActive
                        ? 'bg-brand-50 border border-brand-200/80 text-brand-900 shadow-sm'
                        : 'hover:bg-slate-50 text-slate-700'
                    )}
                  >
                    {/* Status Icon */}
                    <div className="shrink-0">
                      {lesson.completed ? (
                        <CheckCircle2 className="w-4 h-4 text-emerald-500 fill-emerald-50" />
                      ) : isActive ? (
                        <PlayCircle className="w-4 h-4 text-brand-600 fill-brand-100 animate-pulse" />
                      ) : (
                        <div className="w-4 h-4 rounded-full border border-slate-300 flex items-center justify-center text-[9px] font-bold text-slate-400">
                          {lesson.displayOrder}
                        </div>
                      )}
                    </div>

                    {/* Lesson Title & Duration */}
                    <div className="flex-1 min-w-0">
                      <div className={cn('truncate font-medium', isActive && 'font-bold text-brand-700')}>
                        {lesson.lessonTitle}
                      </div>
                      <div className="flex items-center gap-2 text-[10px] text-slate-400 mt-0.5">
                        <span className="uppercase tracking-wider">{lesson.lessonType}</span>
                        <span>•</span>
                        <span className="flex items-center gap-1">
                          <Clock className="w-3 h-3" />
                          {lesson.durationMinutes}m
                        </span>
                      </div>
                    </div>
                  </button>
                )
              })}
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
