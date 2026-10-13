/**
 * Nexarats Academy - Academic Program Catalog API Contracts
 * Enterprise Type Definitions for Web (React) and Mobile (Flutter/Dart compatible)
 */

export interface ProgramCategoryDto {
  id: string
  slug: string
  name: string
  description?: string | null
  displayOrder: number
}

export interface ProgramSummaryDto {
  id: string
  slug: string
  title: string
  shortDescription: string
  imageAssetKey: string
  durationValue: number
  durationUnit: string
  durationLabel: string
  level: string
  badge?: string | null
  rating: number
  categorySlug: string
  categoryName: string
  isFeatured: boolean
  displayOrder: number
}

export interface ProgramDetailDto {
  id: string
  slug: string
  title: string
  shortDescription: string
  detailedDescription?: string | null
  imageAssetKey: string
  durationValue: number
  durationUnit: string
  durationLabel: string
  level: string
  badge?: string | null
  rating: number
  categorySlug: string
  categoryName: string
  curriculumOverview?: string | null
  careerOutcomes?: string | null
  isFeatured: boolean
  displayOrder: number
  publishedAt?: string | null
}

export interface PaginatedProgramsResponse {
  items: ProgramSummaryDto[]
  total: number
  page: number
  size: number
  totalPages: number
}

export interface ProgramFilterParams {
  category?: string
  duration?: number
  featured?: boolean
  search?: string
  q?: string
  page?: number
  size?: number
  sort?: string
}
