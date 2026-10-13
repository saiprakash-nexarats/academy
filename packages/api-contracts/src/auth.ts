/**
 * Nexarats Academy - Authentication & Identity API Contracts
 * Enterprise Type Definitions for Web (React) and Mobile (Flutter/Dart compatible)
 */

export interface UserSummary {
  id: string
  email: string
  firstName: string
  lastName: string
  avatarUrl?: string | null
  fullName?: string
}

export interface TenantSummary {
  id: string
  slug: string
  name: string
  status: string
}

export interface LoginRequest {
  email: string
  password: string
  tenantSlug?: string | null
  clientType?: 'WEB' | 'MOBILE' | 'API'
}

export interface LoginResponse {
  accessToken: string
  expiresIn: number
  user: UserSummary
  tenant: TenantSummary
  roles: string[]
  permissions: string[]
  refreshToken?: string | null
}

export interface TokenRefreshResponse {
  accessToken: string
  expiresIn: number
  refreshToken?: string | null
}

export interface UserProfileResponse {
  id: string
  email: string
  firstName: string
  lastName: string
  avatarUrl?: string | null
  status: string
  tenant: TenantSummary
  roles: string[]
  permissions: string[]
}

export interface RegisterRequest {
  email: string
  password: string
  firstName: string
  lastName: string
  phone?: string | null
  tenantSlug?: string | null
}

export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
}

export interface ForgotPasswordRequest {
  email: string
}

export interface ResetPasswordRequest {
  token: string
  newPassword: string
}

export interface SwitchTenantRequest {
  tenantId: string
}

export interface BootstrapAdminRequest {
  bootstrapSecret: string
  email: string
  password: string
  firstName: string
  lastName: string
  tenantSlug: string
  tenantName: string
}

export interface ProblemDetail {
  type?: string
  title: string
  status: number
  detail: string
  instance?: string
  correlationId?: string
  timestamp?: string
  invalidFields?: Record<string, string>
}
