import React, { useState, useEffect, useCallback, useRef } from 'react'
import type {
  LoginRequest,
  LoginResponse,
  UserProfileResponse,
  TenantSummary,
  UserSummary,
  TokenRefreshResponse,
} from '@nexarats/api-contracts'
import { AuthContext } from './authContextBase'

function getCsrfToken(): string | null {
  if (typeof document === 'undefined') return null
  const match = document.cookie.match(/(?:^|;\s*)(?:nexarats_csrf_token|XSRF-TOKEN)=([^;]*)/)
  return match ? decodeURIComponent(match[1]) : null
}

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserProfileResponse | UserSummary | null>(null)
  const [tenant, setTenant] = useState<TenantSummary | null>(null)
  const [roles, setRoles] = useState<string[]>([])
  const [permissions, setPermissions] = useState<string[]>([])
  const [accessToken, setAccessToken] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState<boolean>(true)

  // In-flight refresh promise to prevent concurrent refresh races
  const refreshPromiseRef = useRef<Promise<boolean> | null>(null)

  /**
   * Refreshes access token and restores user session.
   * Uses HttpOnly cookie, prevents replay races, and keeps tokens in memory.
   */
  const refreshSession = useCallback(async (): Promise<boolean> => {
    if (refreshPromiseRef.current) {
      return refreshPromiseRef.current
    }

    const performRefresh = async (): Promise<boolean> => {
      try {
        const csrfToken = getCsrfToken()
        const headers: Record<string, string> = {
          'Content-Type': 'application/json',
        }
        if (csrfToken) {
          headers['X-CSRF-Token'] = csrfToken
        }

        const response = await fetch('/api/v1/auth/refresh', {
          method: 'POST',
          headers,
          credentials: 'include', // Includes HttpOnly refresh token cookie
        })

        if (!response.ok) {
          setAccessToken(null)
          setUser(null)
          setTenant(null)
          setRoles([])
          setPermissions([])
          return false
        }

        const data: TokenRefreshResponse = await response.json()
        setAccessToken(data.accessToken)

        // Fetch authenticated user profile using newly issued access token
        const profileResp = await fetch('/api/v1/auth/me', {
          headers: {
            Authorization: `Bearer ${data.accessToken}`,
          },
        })

        if (profileResp.ok) {
          const profile: UserProfileResponse = await profileResp.json()
          setUser(profile)
          setTenant(profile.tenant)
          setRoles(profile.roles)
          setPermissions(profile.permissions)
          return true
        }

        return false
      } catch (err) {
        console.warn('Session refresh failed:', err)
        return false
      } finally {
        refreshPromiseRef.current = null
      }
    }

    refreshPromiseRef.current = performRefresh()
    return refreshPromiseRef.current
  }, [])

  /**
   * Authenticates user with email and password against /api/v1/auth/login.
   */
  const login = useCallback(
    async (email: string, password: string, tenantSlug?: string): Promise<void> => {
      setIsLoading(true)
      try {
        const payload: LoginRequest = {
          email: email.trim().toLowerCase(),
          password,
          tenantSlug: tenantSlug || undefined,
          clientType: 'WEB',
        }

        const response = await fetch('/api/v1/auth/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          credentials: 'include', // Receives HttpOnly cookie
          body: JSON.stringify(payload),
        })

        if (!response.ok) {
          const errorData = await response.json().catch(() => null)
          const message =
            errorData?.detail ||
            errorData?.title ||
            'Authentication failed. Please verify your credentials.'
          throw new Error(message)
        }

        const data: LoginResponse = await response.json()
        setAccessToken(data.accessToken)
        setUser(data.user)
        setTenant(data.tenant)
        setRoles(data.roles)
        setPermissions(data.permissions)
      } finally {
        setIsLoading(false)
      }
    },
    []
  )

  /**
   * Logs out user, invalidating session on backend and clearing memory state.
   */
  const logout = useCallback(async (): Promise<void> => {
    try {
      const csrfToken = getCsrfToken()
      const headers: Record<string, string> = {
        'Content-Type': 'application/json',
      }
      if (accessToken) {
        headers['Authorization'] = `Bearer ${accessToken}`
      }
      if (csrfToken) {
        headers['X-CSRF-Token'] = csrfToken
      }

      await fetch('/api/v1/auth/logout', {
        method: 'POST',
        headers,
        credentials: 'include',
      }).catch(() => {})
    } finally {
      setAccessToken(null)
      setUser(null)
      setTenant(null)
      setRoles([])
      setPermissions([])
    }
  }, [accessToken])

  // Silent session restoration on initial application mount
  useEffect(() => {
    const initAuth = async () => {
      try {
        await refreshSession()
      } finally {
        setIsLoading(false)
      }
    }
    initAuth()
  }, [refreshSession])

  const isAuthenticated = Boolean(accessToken && user)

  return (
    <AuthContext.Provider
      value={{
        user,
        tenant,
        roles,
        permissions,
        isAuthenticated,
        isLoading,
        accessToken,
        login,
        logout,
        refreshSession,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}
