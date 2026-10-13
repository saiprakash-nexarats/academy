import { TrendingUp, Building2, ShieldCheck, Award } from 'lucide-react'
import type { MetricStat } from '@/types'

export const STATS_DATA: MetricStat[] = [
  {
    id: 'placement-assurance',
    value: '100%',
    label: 'Job Assurance Support',
    icon: ShieldCheck,
  },
  {
    id: 'hiring-partners',
    value: '500+',
    label: 'Active Hiring Partners',
    icon: Building2,
  },
  {
    id: 'placement-rate',
    value: '95%',
    label: 'Placement Success Rate',
    icon: TrendingUp,
  },
  {
    id: 'avg-package',
    value: '₹14.5 L',
    label: 'Average Starting Package',
    icon: Award,
  },
]



