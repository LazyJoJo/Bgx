import { useCallback, useEffect, useMemo, useState } from 'react'
import { AUTH_TOKEN_KEY, USER_ID_KEY } from '../constants/auth'

export interface AuthState {
    token: string | null
    userId: string | null
}

// 自定义事件类型声明
type AuthChangeEventListener = (event: Event) => void

export function useAuth() {
    const [token, setToken] = useState<string | null>(() => localStorage.getItem(AUTH_TOKEN_KEY))
    const [userId, setUserId] = useState<string | null>(() => localStorage.getItem(USER_ID_KEY))

    const isAuthenticated = !!token

    // 监听 auth-change 事件，保持响应式
    useEffect(() => {
        const handleAuthChange: AuthChangeEventListener = () => {
            setToken(localStorage.getItem(AUTH_TOKEN_KEY))
            setUserId(localStorage.getItem(USER_ID_KEY))
        }
        window.addEventListener('auth-change', handleAuthChange)
        return () => window.removeEventListener('auth-change', handleAuthChange)
    }, [])

    const login = useCallback((newToken: string, newUserId: string) => {
        localStorage.setItem(AUTH_TOKEN_KEY, newToken)
        localStorage.setItem(USER_ID_KEY, newUserId)
        // Dispatch custom event to notify components of auth changes
        window.dispatchEvent(new Event('auth-change'))
    }, [])

    const logout = useCallback(() => {
        localStorage.removeItem(AUTH_TOKEN_KEY)
        localStorage.removeItem(USER_ID_KEY)
        window.dispatchEvent(new Event('auth-change'))
    }, [])

    return useMemo(
        () => ({
            isAuthenticated,
            token,
            userId,
            login,
            logout,
        }),
        [isAuthenticated, token, userId, login, logout]
    )
}
