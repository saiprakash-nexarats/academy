import type { NavItem } from '@/types'

export const PRIMARY_NAV_ITEMS: NavItem[] = [
  { label: 'Home', href: '/' },
  { label: 'Programs', href: '/programs' },
  { label: 'Courses', href: '/courses' },
  { label: 'Mentors', href: '/mentors' },
  { label: 'Success Stories', href: '/success-stories' },
  { label: 'About', href: '/about' },
  { label: 'Contact', href: '/contact' },
]

export const FOOTER_QUICK_LINKS: { title: string; links: NavItem[] }[] = [
  {
    title: 'Popular Programs',
    links: [
      { label: 'Full Stack Development', href: '/programs/full-stack' },
      { label: 'Data Science & AI', href: '/programs/data-science' },
      { label: 'UI/UX Design', href: '/programs/ui-ux' },
      { label: 'Digital Marketing', href: '/programs/digital-marketing' },
    ],
  },
  {
    title: 'Platform',
    links: [
      { label: 'Courses Library', href: '/courses' },
      { label: 'Mentorship', href: '/mentors' },
      { label: 'Platform Experience', href: '/#experience' },
      { label: 'Accreditation', href: '/about' },
    ],
  },
  {
    title: 'Company',
    links: [
      { label: 'About Us', href: '/about' },
      { label: 'Success Stories', href: '/success-stories' },
      { label: 'Why Choose Us', href: '/#why-choose' },
      { label: 'Contact Admissions', href: '/contact' },
    ],
  },
]
