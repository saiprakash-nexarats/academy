import { useQuery } from '@tanstack/react-query'
import type {
  ProgramSummaryDto,
  ProgramDetailDto,
  ProgramCategoryDto,
  PaginatedProgramsResponse,
  ProgramFilterParams,
} from '@nexarats/api-contracts'

export type { ProgramSummaryDto, ProgramDetailDto, ProgramCategoryDto, PaginatedProgramsResponse, ProgramFilterParams }

export const programQueryKeys = {
  all: ['programs'] as const,
  lists: () => [...programQueryKeys.all, 'list'] as const,
  list: (filters: ProgramFilterParams) => [...programQueryKeys.lists(), filters] as const,
  details: () => [...programQueryKeys.all, 'detail'] as const,
  detail: (slug: string) => [...programQueryKeys.details(), slug] as const,
  categories: () => ['program-categories'] as const,
}

export async function fetchPrograms(params: ProgramFilterParams = {}): Promise<PaginatedProgramsResponse> {
  const query = new URLSearchParams()
  if (params.category && params.category !== 'all') query.set('category', params.category)
  if (params.duration !== undefined && params.duration !== null) query.set('duration', params.duration.toString())
  if (params.featured !== undefined && params.featured !== null) query.set('featured', params.featured.toString())
  if (params.search) query.set('search', params.search)
  if (params.q) query.set('q', params.q)
  if (params.page !== undefined) query.set('page', params.page.toString())
  if (params.size !== undefined) query.set('size', params.size.toString())
  if (params.sort) query.set('sort', params.sort)

  const url = `/api/v1/academic/programs${query.toString() ? `?${query.toString()}` : ''}`
  const response = await fetch(url, {
    headers: {
      Accept: 'application/json',
    },
  })

  if (!response.ok) {
    throw new Error(`Failed to load programs: ${response.status} ${response.statusText}`)
  }

  return response.json()
}

export async function fetchProgramDetail(slug: string): Promise<ProgramDetailDto> {
  const response = await fetch(`/api/v1/academic/programs/${encodeURIComponent(slug)}`, {
    headers: {
      Accept: 'application/json',
    },
  })

  if (!response.ok) {
    if (response.status === 404) {
      throw new Error(`Program not found (${slug})`)
    }
    throw new Error(`Failed to load program details: ${response.status} ${response.statusText}`)
  }

  return response.json()
}

export async function fetchCategories(): Promise<ProgramCategoryDto[]> {
  const response = await fetch('/api/v1/academic/program-categories', {
    headers: {
      Accept: 'application/json',
    },
  })

  if (!response.ok) {
    throw new Error(`Failed to load categories: ${response.status} ${response.statusText}`)
  }

  return response.json()
}

export function usePrograms(params: ProgramFilterParams = {}) {
  return useQuery({
    queryKey: programQueryKeys.list(params),
    queryFn: () => fetchPrograms(params),
    staleTime: 5 * 60 * 1000, // 5 minutes
    retry: 1,
  })
}

export function useProgramDetail(slug?: string) {
  return useQuery({
    queryKey: slug ? programQueryKeys.detail(slug) : ['program-detail', 'none'],
    queryFn: () => fetchProgramDetail(slug!),
    enabled: Boolean(slug),
    staleTime: 5 * 60 * 1000,
    retry: 1,
  })
}

export function useCategories() {
  return useQuery({
    queryKey: programQueryKeys.categories(),
    queryFn: fetchCategories,
    staleTime: 15 * 60 * 1000, // 15 minutes
    retry: 1,
  })
}
