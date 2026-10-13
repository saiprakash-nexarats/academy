import { createContext, useContext } from 'react'
import type {
  UserProfileResponse,
  TenantSummary,
  UserSummary,
} from '@nexarats/api-contracts'

export interface AuthContextType {
  user: UserProfileResponse | UserSummary | null
  tenant: TenantSummary | null
  roles: string[]
  permissions: string[]
  isAuthenticated: boolean
  isLoading: boolean
  accessToken: string | null
  login: (email: string, password: string, tenantSlug?: string) => Promise<void>
  logout: () => Promise<void>
  refreshSession: () => Promise<boolean>
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined)

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
