import {
  LayoutDashboard,
  BookOpen,
  Video,
  Briefcase,
  GraduationCap,
  Award,
  Settings,
  User,
  Calendar,
  LineChart,
  Code2,
  UserCheck,
} from 'lucide-react'
import type {
  PlatformMenuItem,
  LiveClassItem,
  ConnectedNodeItem,
  StudentProgressData,
} from '@/types'

export const PLATFORM_MENU_ITEMS: PlatformMenuItem[] = [
  { label: 'Dashboard', icon: LayoutDashboard, active: true },
  { label: 'My Courses', icon: BookOpen, active: false },
  { label: 'Live Classes', icon: Video, active: false },
  { label: 'Projects', icon: Briefcase, active: false },
  { label: 'Mentors', icon: GraduationCap, active: false },
  { label: 'Certificates', icon: Award, active: false },
  { label: 'Settings', icon: Settings, active: false },
]

export const ANANYA_STUDENT_PROGRESS: StudentProgressData = {
  studentName: 'Ananya',
  greeting: 'Good morning, Ananya! 👋',
  overallProgress: '75% Completed',
  progressPercent: 75,
  lessonsCompletedText: '10/16 Lessons Completed',
  currentLesson: {
    title: 'React Fundamentals',
    course: 'Full Stack Development',
    lessonStep: 'Lesson 11 of 16',
  },
}

export const UPCOMING_LIVE_CLASSES: LiveClassItem[] = [
  {
    id: 'class-1',
    title: 'Advanced React Concepts',
    time: 'Today at 10:00 AM',
    icon: Video,
    iconBgColor: 'bg-blue-100',
    iconTextColor: 'text-brand-600',
    actionLabel: 'Join',
    actionVariant: 'primary',
  },
  {
    id: 'class-2',
    title: 'Career Talk with Google Engineer',
    time: 'Tomorrow at 5:00 PM',
    icon: User,
    iconBgColor: 'bg-sky-100',
    iconTextColor: 'text-sky-600',
    actionLabel: 'Reminder',
    actionVariant: 'outline',
  },
  {
    id: 'class-3',
    title: 'UI/UX Design Workshop',
    time: 'Apr 26 - 3:00 PM',
    icon: Calendar,
    iconBgColor: 'bg-amber-100',
    iconTextColor: 'text-amber-600',
    actionLabel: 'Add',
    actionVariant: 'outline',
  },
]

export const CONNECTED_FEATURE_NODES: ConnectedNodeItem[] = [
  {
    id: 'progress',
    title: 'Track Your Progress',
    description: 'See your growth in real-time.',
    icon: LineChart,
  },
  {
    id: 'live',
    title: 'Join Live Classes',
    description: 'Interact with experts instantly.',
    icon: Video,
  },
  {
    id: 'projects',
    title: 'Build Real Projects',
    description: 'Gain hands-on experience.',
    icon: Code2,
  },
  {
    id: 'support',
    title: 'Get Mentor Support',
    description: 'Ask questions and get guidance.',
    icon: UserCheck,
  },
]
