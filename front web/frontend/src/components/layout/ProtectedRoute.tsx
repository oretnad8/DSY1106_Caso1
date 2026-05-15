import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

interface ProtectedRouteProps {
    allowedRoles?: ('ADMIN' | 'JEFE' | 'SUPERVISOR' | 'OPERADOR')[];
}

export const ProtectedRoute = ({ allowedRoles }: ProtectedRouteProps) => {
    const { user, isAuthenticated } = useAuth();

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    if (allowedRoles && !allowedRoles.includes(user?.role as any)) {
        return <Navigate to="/" replace />;
    }

    return <Outlet />;
};
