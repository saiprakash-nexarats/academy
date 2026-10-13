import type { LucideIcon } from 'lucide-react'

/**
 * Nexarats Academy - Core TypeScript Type Definitions
 */

export interface NavItem {
  label: string
  href: string
  isExternal?: boolean
  badge?: string
}

export interface FooterLinkSection {
  title: string
  links: NavItem[]
}

export interface MetricStat {
  id: string
  label: string
  value: string
  icon: LucideIcon
}

export interface ProgramItem {
  id: string
  title: string
  description: string
  image: string
  duration: string
  level: string
  badge?: string
  category?: string
  rating?: number
}

export interface WhyChooseItem {
  id: string
  title: string
  description: string
  icon: LucideIcon
}

export interface PlatformMenuItem {
  label: string
  icon: LucideIcon
  active?: boolean
}

export interface LiveClassItem {
  id: string
  title: string
  time: string
  icon: LucideIcon
  iconBgColor: string
  iconTextColor: string
  actionLabel: string
  actionVariant: 'primary' | 'outline'
}

export interface ConnectedNodeItem {
  id: string
  title: string
  description: string
  icon: LucideIcon
}

export type CompanyBrand = 'microsoft' | 'amazon' | 'adobe' | 'google' | 'razorpay' | 'uber' | 'atlassian' | 'swiggy'

export interface TestimonialItem {
  id: string
  name: string
  role: string
  company: string
  companyBrand: CompanyBrand
  quote: string
  avatar: string
  rating?: number
}

export interface StudentProgressData {
  studentName: string
  greeting: string
  overallProgress: string
  progressPercent: number
  lessonsCompletedText: string
  currentLesson: {
    title: string
    course: string
    lessonStep: string
  }
}

export interface MentorItem {
  id: string
  name: string
  role: string
  company: string
  companyLogo?: string
  avatar: string
  rating: number
  studentsMentored: number
  experienceYears: number
  specialization: string[]
  bio: string
  availableDays?: string
}

export interface InquiryFormData {
  fullName: string
  email: string
  phone: string
  programOfInterest: string
  message: string
}

export type ButtonVariant = 'primary' | 'secondary' | 'outline' | 'ghost' | 'soft'
export type ButtonSize = 'sm' | 'md' | 'lg'
