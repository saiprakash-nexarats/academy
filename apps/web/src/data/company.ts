import { ShieldCheck, Target, Users, Award } from 'lucide-react'

export const COMPANY_ABOUT_DATA = {
  mission: 'To bridge the gap between traditional tech education and modern industry standards through elite mentorship, outcome-driven learning, and real-world project portfolios.',
  vision: 'Empowering 1 Million ambitious engineers, designers, and tech leaders worldwide to achieve career breakthroughs.',
  stats: [
    { label: 'Enrolled Learners', value: '50K+', subtext: 'Across 30+ countries' },
    { label: 'Industry Mentors', value: '80+', subtext: 'From FAANG & top tech' },
    { label: 'Placement Rate', value: '95%', subtext: 'Within 6 months of graduation' },
    { label: 'Hiring Partners', value: '500+', subtext: 'Top tech enterprises & startups' },
  ],
  pillars: [
    {
      icon: Target,
      title: 'Industry-Aligned Curriculum',
      description: 'Courses co-designed with engineering leaders from Google, Amazon, and Microsoft to teach in-demand skills.',
    },
    {
      icon: Users,
      title: '1:1 Expert Mentorship',
      description: 'Weekly personalized code reviews, portfolio teardowns, and career strategy coaching.',
    },
    {
      icon: Award,
      title: 'Verified Certification',
      description: 'Accredited certificates recognized by leading employers globally to validate your technical mastery.',
    },
    {
      icon: ShieldCheck,
      title: 'Dedicated Career Placement',
      description: 'Resume optimization, mock technical interviews, and direct referral pipeline to 500+ hiring partners.',
    },
  ],
}

export const CONTACT_INFO_DATA = {
  office: {
    title: 'Admissions Headquarters',
    address: 'Nexarats Tech Tower, Outer Ring Road, Bangalore, Karnataka 560103',
    phone: '+91 (800) 456-7890',
    email: 'admissions@nexarats.academy',
    hours: 'Monday – Saturday: 9:00 AM – 8:00 PM IST',
  },
  support: {
    title: 'Student Support Desk',
    email: 'support@nexarats.academy',
    phone: '+91 (800) 456-7891',
    hours: '24/7 Dedicated Learner Assistance',
  },
  inquiryPrograms: [
    'Full Stack Development (6 Months)',
    'Data Science & AI (6 Months)',
    'UI/UX Design (6 Months)',
    'Digital Marketing (4 Months)',
    'Cloud & DevOps Engineering (5 Months)',
    'Generative AI & LLM Systems (4 Months)',
  ],
}

export const FAQ_DATA = [
  {
    q: 'Do I need prior coding experience to join?',
    a: 'No! Our programs are structured with beginner-friendly foundational ramps that quickly transition to advanced industry architectures.',
  },
  {
    q: 'How does 1:1 mentorship work?',
    a: 'You are paired with an experienced industry practitioner who evaluates your weekly capstone submissions, conducts live 1-on-1 calls, and provides career direction.',
  },
  {
    q: 'What is the refund or deferral policy?',
    a: 'We offer a 100% money-back guarantee within the first 14 days of cohort commencement if you feel the program is not the right fit.',
  },
  {
    q: 'Will I receive a verifiable certificate?',
    a: 'Yes, upon passing capstone assessments and project defenses, you receive an accredited digital credential with verifiable cryptographic hash.',
  },
]
