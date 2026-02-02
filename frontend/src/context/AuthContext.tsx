import { createContext, useState, useEffect, ReactNode, useCallback } from 'react'
import { AuthResponse } from '../types'
import * as api from '../services/api'

interface AuthContextType {
  isAuthenticated: boolean
  isLoading: boolean
  username: string | null
  login: (username: string, password: string) => Promise<void>
  register: (username: string, password: string) => Promise<void>
  logout: () => void
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined)

const TOKEN_KEY = 'checklist-token'
const USERNAME_KEY = 'checklist-username'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const [username, setUsername] = useState<string | null>(null)

  useEffect(() => {
    const token = localStorage.getItem(TOKEN_KEY)
    const storedUsername = localStorage.getItem(USERNAME_KEY)
    if (token && storedUsername) {
      api.setAuthToken(token)
      setIsAuthenticated(true)
      setUsername(storedUsername)
    }
    setIsLoading(false)
  }, [])

  const handleAuthSuccess = useCallback((response: AuthResponse) => {
    localStorage.setItem(TOKEN_KEY, response.token)
    localStorage.setItem(USERNAME_KEY, response.username)
    api.setAuthToken(response.token)
    setIsAuthenticated(true)
    setUsername(response.username)
  }, [])

  const login = useCallback(async (username: string, password: string) => {
    const response = await api.login({ username, password })
    handleAuthSuccess(response)
  }, [handleAuthSuccess])

  const register = useCallback(async (username: string, password: string) => {
    const response = await api.register({ username, password })
    handleAuthSuccess(response)
  }, [handleAuthSuccess])

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USERNAME_KEY)
    api.setAuthToken(null)
    setIsAuthenticated(false)
    setUsername(null)
  }, [])

  return (
    <AuthContext.Provider
      value={{
        isAuthenticated,
        isLoading,
        username,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}
