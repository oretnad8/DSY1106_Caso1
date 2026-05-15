import { createContext, useContext, useState, type ReactNode } from 'react';

interface User {
    id: number;
    username: string;
    role: string;
    token: string;
}

interface AuthContextType {
    user: User | null;
    login: (user: User) => void;
    logout: () => void;
    isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
    const [user, setUser] = useState<User | null>(() => {
        const stored = localStorage.getItem('smartlogix_user');
        return stored ? JSON.parse(stored) : null;
    });

    const login = (userData: User) => {
        setUser(userData);
        localStorage.setItem('smartlogix_user', JSON.stringify(userData));
    };

    const logout = () => {
        setUser(null);
        localStorage.removeItem('smartlogix_user');
    };

    return (
        <AuthContext.Provider value={{ user, login, logout, isAuthenticated: !!user }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) throw new Error('useAuth must be used within an AuthProvider');
    return context;
};
