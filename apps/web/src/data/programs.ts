/**
 * NON-AUTHORITATIVE LEGACY FIXTURE
 * Note: As of Phase 3, PostgreSQL (Flyway V7) and Spring Boot REST APIs
 * (/api/v1/academic/programs) serve as the single source of truth for the
 * Nexarats Academy program catalog. This file is preserved strictly as a
 * migration fixture and test reference. Do not add production fallbacks to this file.
 */
import type { ProgramItem } from '@/types'
import courseFullstackImg from '@/assets/course-fullstack.jpg'
import courseDatascienceImg from '@/assets/course-datascience.jpg'
import courseUiuxImg from '@/assets/course-uiux.jpg'
import courseMarketingImg from '@/assets/course-marketing.jpg'

export const POPULAR_PROGRAMS: ProgramItem[] = [
  {
    id: 'full-stack',
    title: 'Full Stack Development',
    description: 'Master modern web technologies and build real-world applications.',
    image: courseFullstackImg,
    duration: '6 Months',
    level: 'Beginner to Advanced',
    badge: 'Bestseller',
    category: 'Software Engineering',
    rating: 4.9,
  },
  {
    id: 'data-science',
    title: 'Data Science & AI',
    description: 'Learn data analysis, machine learning and AI with hands-on projects.',
    image: courseDatascienceImg,
    duration: '6 Months',
    level: 'Beginner to Advanced',
    category: 'Artificial Intelligence',
    rating: 4.85,
  },
  {
    id: 'ui-ux',
    title: 'UI/UX Design',
    description: 'Create beautiful and user-friendly digital experiences.',
    image: courseUiuxImg,
    duration: '6 Months',
    level: 'Beginner to Advanced',
    category: 'Design',
    rating: 4.92,
  },
  {
    id: 'digital-marketing',
    title: 'Digital Marketing',
    description: 'Learn modern marketing strategies to grow brands online.',
    image: courseMarketingImg,
    duration: '4 Months',
    level: 'Beginner to Advanced',
    category: 'Marketing',
    rating: 4.8,
  },
]

export const ALL_PROGRAMS: ProgramItem[] = [
  ...POPULAR_PROGRAMS,
  {
    id: 'cloud-devops',
    title: 'Cloud & DevOps Engineering',
    description: 'Master AWS, Docker, Kubernetes, and automated CI/CD deployment pipelines.',
    image: courseFullstackImg,
    duration: '5 Months',
    level: 'Intermediate to Advanced',
    badge: 'High Demand',
    category: 'Software Engineering',
    rating: 4.88,
  },
  {
    id: 'mobile-app',
    title: 'Mobile App Development (React Native)',
    description: 'Build native iOS and Android apps with cross-platform excellence.',
    image: courseUiuxImg,
    duration: '4 Months',
    level: 'Beginner to Intermediate',
    category: 'Software Engineering',
    rating: 4.82,
  },
  {
    id: 'gen-ai-engineers',
    title: 'Generative AI & LLM Systems',
    description: 'Fine-tune LLMs, build RAG pipelines, and deploy production AI agents.',
    image: courseDatascienceImg,
    duration: '4 Months',
    level: 'Intermediate to Advanced',
    badge: 'Trending',
    category: 'Artificial Intelligence',
    rating: 4.96,
  },
  {
    id: 'product-management',
    title: 'Tech Product Management',
    description: 'Lead roadmaps, agile sprints, user stories, and high-impact product launches.',
    image: courseMarketingImg,
    duration: '4 Months',
    level: 'Beginner to Advanced',
    category: 'Marketing',
    rating: 4.79,
  },
]
