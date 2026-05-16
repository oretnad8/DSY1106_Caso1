import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from './context/AuthContext';
import { MainLayout } from './components/layout/MainLayout';
import { ProtectedRoute } from './components/layout/ProtectedRoute';
import { Login } from './pages/Login';
import { Tracking } from './pages/Tracking';
import { Dashboard } from './pages/Dashboard';
import { SalesNotes } from './pages/SalesNotes';
import { Billing } from './pages/Billing';
import { UserManagement } from './pages/UserManagement';
import { SalespersonManagement } from './pages/SalespersonManagement';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
    }
  }
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<Login />} />

            <Route element={<ProtectedRoute />}>
              <Route element={<MainLayout />}>
                <Route path="/" element={<Dashboard />} />
                <Route path="/tracking" element={<Tracking />} />

                {/* Specific Roles */}
                <Route element={<ProtectedRoute allowedRoles={['JEFE', 'SUPERVISOR']} />}>
                  <Route path="/sales-notes" element={<SalesNotes />} />
                </Route>

                <Route element={<ProtectedRoute allowedRoles={['JEFE']} />}>
                  <Route path="/billing" element={<Billing />} />
                </Route>

                <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
                  <Route path="/users" element={<UserManagement />} />
                  <Route path="/vendedores" element={<SalespersonManagement />} />
                </Route>
              </Route>
            </Route>

            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  );
}

export default App;
