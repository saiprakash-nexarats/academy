import {
  GraduationCap,
  Clock,
  Layers,
  ShieldCheck,
  LineChart,
  Video,
  Code2,
  UserCheck,
} from 'lucide-react'
import type { WhyChooseItem } from '@/types'

export const WHY_CHOOSE_LEFT: WhyChooseItem[] = [
  {
    id: 'career-guarantee',
    title: '100% Job Assurance Guarantee',
    description: 'Dedicated placement cell with guaranteed interview calls.',
    icon: ShieldCheck,
  },
  {
    id: 'mentors',
    title: 'Mentors from Top MNCs',
    description: '1:1 guidance from engineers at Google, Amazon & Microsoft.',
    icon: GraduationCap,
  },
  {
    id: 'projects',
    title: 'Production Capstone Projects',
    description: 'Build enterprise-grade software that recruiters notice.',
    icon: Layers,
  },
  {
    id: 'flexible',
    title: 'Flexible Cohort Schedules',
    description: 'Study at your own pace with weekend and evening batches.',
    icon: Clock,
  },
]

export const WHY_CHOOSE_RIGHT: WhyChooseItem[] = [
  {
    id: 'mock-interviews',
    title: '1:1 Big Tech Mock Interviews',
    description: 'Practice coding & system design until you pass hiring bars.',
    icon: UserCheck,
  },
  {
    id: 'live-classes',
    title: 'Interactive Live Classes',
    description: 'Ask real-time doubts and build architectures collaboratively.',
    icon: Video,
  },
  {
    id: 'real-projects',
    title: 'Portfolio & Resume Optimization',
    description: 'ATS-tailored resumes and verified GitHub credentials.',
    icon: Code2,
  },
  {
    id: 'progress',
    title: 'Transparent Growth Tracking',
    description: 'Measure skill mastery with real-time competency metrics.',
    icon: LineChart,
  },
]

