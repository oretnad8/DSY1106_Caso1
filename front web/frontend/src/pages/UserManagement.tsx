import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import client from '../api/client';
import type { Usuario } from '../types';
import { UserPlus, UserCheck, UserX, Edit2, Save, X, Shield, Mail } from 'lucide-react';
import { clsx } from 'clsx';

export const UserManagement = () => {
    const queryClient = useQueryClient();
    const [editingUser, setEditingUser] = useState<Partial<Usuario> | null>(null);
    const [isAdding, setIsAdding] = useState(false);

    // Fetch Users
    const { data: users, isLoading } = useQuery<Usuario[]>({
        queryKey: ['usuarios'],
        queryFn: async () => {
            const res = await client.get('/usuarios');
            return res.data;
        }
    });

    // Mutations
    const createMutation = useMutation({
        mutationFn: (newUser: Partial<Usuario>) => client.post('/usuarios', newUser),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['usuarios'] });
            setIsAdding(false);
        }
    });

    const updateMutation = useMutation({
        mutationFn: (user: Partial<Usuario>) => client.put(`/usuarios/${user.id}`, user),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['usuarios'] });
            setEditingUser(null);
        }
    });

    const toggleMutation = useMutation({
        mutationFn: (id: number) => client.put(`/usuarios/${id}/toggle-activo`),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['usuarios'] })
    });

    if (isLoading) return <div className="animate-pulse space-y-4">
        {[1, 2, 3].map(i => <div key={i} className="h-16 bg-gray-100 rounded-lg"></div>)}
    </div>;

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Gestión de Personal</h1>
                    <p className="text-gray-500 text-sm">Administración de usuarios internos y roles del sistema</p>
                </div>
                <button
                    onClick={() => setIsAdding(true)}
                    className="flex items-center gap-2 px-4 py-2 bg-corporate-blue text-white rounded-lg hover:bg-blue-800 transition-all font-medium"
                >
                    <UserPlus size={18} /> Nuevo Usuario
                </button>
            </div>

            <div className="grid gap-4">
                {isAdding && (
                    <div className="bg-blue-50 p-6 rounded-xl border-2 border-dashed border-blue-200 shadow-sm animate-in fade-in slide-in-from-top-4">
                        <div className="flex items-center gap-2 mb-4 text-blue-700 font-bold uppercase text-xs tracking-wider">
                            <UserPlus size={16} /> Crear Nuevo Usuario
                        </div>
                        <UserForm
                            onSave={(u) => createMutation.mutate(u)}
                            onCancel={() => setIsAdding(false)}
                            isLoading={createMutation.isPending}
                        />
                    </div>
                )}

                {users?.map(user => (
                    <div key={user.id} className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm flex items-center justify-between group hover:border-blue-200 transition-all">
                        {editingUser?.id === user.id ? (
                            <UserForm
                                initialData={user}
                                onSave={(u) => updateMutation.mutate(u)}
                                onCancel={() => setEditingUser(null)}
                                isLoading={updateMutation.isPending}
                            />
                        ) : (
                            <>
                                <div className="flex items-center gap-4">
                                    <div className={clsx(
                                        "w-10 h-10 rounded-full flex items-center justify-center font-bold",
                                        user.activo ? "bg-blue-50 text-blue-600" : "bg-gray-100 text-gray-400"
                                    )}>
                                        {user.nombre.charAt(0)}
                                    </div>
                                    <div>
                                        <div className="font-semibold text-gray-900 flex items-center gap-2">
                                            {user.nombre}
                                            {user.rol === 'ADMIN' && <Shield size={14} className="text-amber-500" />}
                                        </div>
                                        <div className="text-sm text-gray-500 flex items-center gap-1">
                                            <Mail size={12} /> {user.email} • <span className="font-medium text-corporate-blue">{user.rol}</span>
                                        </div>
                                    </div>
                                </div>
                                <div className="flex items-center gap-2 transition-all">
                                    <button
                                        onClick={() => setEditingUser(user)}
                                        className="p-2 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-all border border-transparent hover:border-blue-100"
                                        title="Editar"
                                    >
                                        <Edit2 size={18} />
                                    </button>
                                    <button
                                        onClick={() => toggleMutation.mutate(user.id)}
                                        className={clsx(
                                            "p-2 rounded-lg transition-all border border-transparent",
                                            user.activo ? "text-green-500 hover:bg-green-50 hover:border-green-100" : "text-gray-400 hover:bg-gray-100 hover:border-gray-200"
                                        )}
                                        title={user.activo ? "Bloquear/Desactivar" : "Desbloquear/Activar"}
                                    >
                                        {user.activo ? <UserCheck size={18} /> : <UserX size={18} />}
                                    </button>
                                </div>
                            </>
                        )}
                    </div>
                ))}
            </div>
        </div>
    );
};

const UserForm = ({ initialData, onSave, onCancel, isLoading }: {
    initialData?: Partial<Usuario>,
    onSave: (u: Partial<Usuario>) => void,
    onCancel: () => void,
    isLoading: boolean
}) => {
    const [formData, setFormData] = useState({
        nombre: initialData?.nombre || '',
        email: initialData?.email || '',
        rol: initialData?.rol || 'OPERADOR',
        password: '',
        activo: initialData?.activo ?? true
    });

    return (
        <div className="w-full grid grid-cols-1 md:grid-cols-5 gap-4 items-end">
            <div className="space-y-1">
                <label className="text-xs font-bold text-gray-400 uppercase">Nombre</label>
                <input
                    type="text"
                    value={formData.nombre}
                    onChange={e => setFormData({ ...formData, nombre: e.target.value })}
                    className="w-full px-3 py-2 border rounded-lg outline-none focus:ring-2 focus:ring-blue-500 text-sm"
                    placeholder="Nombre Completo"
                />
            </div>
            <div className="space-y-1">
                <label className="text-xs font-bold text-gray-400 uppercase">Email</label>
                <input
                    type="email"
                    value={formData.email}
                    onChange={e => setFormData({ ...formData, email: e.target.value })}
                    className="w-full px-3 py-2 border rounded-lg outline-none focus:ring-2 focus:ring-blue-500 text-sm"
                    placeholder="usuario@smartlogix.cl"
                />
            </div>
            <div className="space-y-1">
                <label className="text-xs font-bold text-gray-400 uppercase">Rol</label>
                <select
                    value={formData.rol}
                    onChange={e => setFormData({ ...formData, rol: e.target.value as any })}
                    className="w-full px-3 py-2 border rounded-lg outline-none focus:ring-2 focus:ring-blue-500 text-sm"
                >
                    <option value="ADMIN">ADMIN</option>
                    <option value="JEFE">JEFE</option>
                    <option value="SUPERVISOR">SUPERVISOR</option>
                    <option value="OPERADOR">OPERADOR</option>
                </select>
            </div>
            {!initialData && (
                <div className="space-y-1">
                    <label className="text-xs font-bold text-gray-400 uppercase">Password</label>
                    <input
                        type="password"
                        value={formData.password}
                        onChange={e => setFormData({ ...formData, password: e.target.value })}
                        className="w-full px-3 py-2 border rounded-lg outline-none focus:ring-2 focus:ring-blue-500 text-sm"
                        placeholder="Contraseña"
                    />
                </div>
            )}
            <div className="col-span-full md:col-span-1 flex justify-end gap-2">
                <button onClick={onCancel} className="p-2 text-gray-400 hover:bg-gray-100 rounded-lg">
                    <X size={20} />
                </button>
                <button
                    disabled={isLoading}
                    onClick={() => onSave({ ...initialData, ...formData })}
                    className="p-2 bg-green-500 text-white rounded-lg hover:bg-green-600 disabled:opacity-50"
                >
                    <Save size={20} />
                </button>
            </div>
        </div>
    );
};
