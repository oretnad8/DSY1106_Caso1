import { Outlet, Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
    LayoutDashboard,
    ShoppingCart,
    Activity,
    FileText,
    LogOut,
    User,
    Users,
    Shield
} from 'lucide-react';
import { clsx } from 'clsx'; // Assuming clsx is installed, if not we will fix
import { twMerge } from 'tailwind-merge';

export const MainLayout = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const navItems = [
        {
            label: 'Dashboard',
            path: '/',
            icon: LayoutDashboard,
            roles: ['ADMIN', 'JEFE', 'SUPERVISOR']
        },
        {
            label: 'Nueva Nota Venta',
            path: '/sales-notes',
            icon: ShoppingCart,
            roles: ['ADMIN', 'JEFE', 'SUPERVISOR']
        },
        {
            label: 'Monitor',
            path: '/tracking',
            icon: Activity,
            roles: ['ADMIN', 'JEFE', 'SUPERVISOR']
        },
        {
            label: 'Facturación',
            path: '/billing',
            icon: FileText,
            roles: ['ADMIN', 'JEFE']
        },
        {
            label: 'Gestión Usuarios',
            path: '/users',
            icon: Shield,
            roles: ['ADMIN']
        },
        {
            label: 'Gestión Vendedores',
            path: '/vendedores',
            icon: Users,
            roles: ['ADMIN']
        },
    ];

    return (
        <div className="flex h-screen bg-gray-100 dark:bg-gray-900 font-sans">
            {/* Sidebar */}
            <aside className="w-64 bg-white dark:bg-gray-800 border-r border-gray-200 dark:border-gray-700 flex flex-col">
                <div className="p-6 border-b border-gray-200 dark:border-gray-700">
                    <h1 className="text-2xl font-bold text-corporate-blue dark:text-blue-400">SmartLogix</h1>
                </div>

                <nav className="flex-1 p-4 space-y-1">
                    {navItems.filter(item => item.roles.includes(user?.role || '')).map((item) => {
                        const Icon = item.icon;
                        const isActive = location.pathname === item.path;
                        return (
                            <Link
                                key={item.path}
                                to={item.path}
                                className={twMerge(
                                    clsx(
                                        "flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium transition-colors",
                                        isActive
                                            ? "bg-blue-50 text-corporate-blue dark:bg-blue-900/20 dark:text-blue-400"
                                            : "text-gray-600 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-800"
                                    )
                                )}
                            >
                                <Icon size={20} />
                                {item.label}
                            </Link>
                        );
                    })}
                </nav>

                <div className="p-4 border-t border-gray-200 dark:border-gray-700">
                    <div className="flex items-center gap-3 px-4 py-3 mb-2">
                        <div className="w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center">
                            <User size={16} className="text-gray-500" />
                        </div>
                        <div>
                            <p className="text-sm font-medium text-gray-900 dark:text-white">{user?.username}</p>
                            <p className="text-xs text-gray-500">{user?.role}</p>
                        </div>
                    </div>
                    <button
                        onClick={handleLogout}
                        className="w-full flex items-center gap-3 px-4 py-2 text-sm text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                    >
                        <LogOut size={18} />
                        Cerrar Sesión
                    </button>
                </div>
            </aside>

            {/* Main Content */}
            <main className="flex-1 overflow-auto p-8">
                <Outlet />
            </main>
        </div>
    );
};
