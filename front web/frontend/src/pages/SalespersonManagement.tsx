import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import client from '../api/client';
import type { Vendedor } from '../types';
import { Plus, Edit2, Trash2, Check, X, UserCircle } from 'lucide-react';
import { clsx } from 'clsx';

export const SalespersonManagement = () => {
    const queryClient = useQueryClient();
    const [isAdding, setIsAdding] = useState(false);
    const [editingVendedor, setEditingVendedor] = useState<Vendedor | null>(null);

    // Fetch Vendedores
    const { data: vendedores, isLoading } = useQuery<Vendedor[]>({
        queryKey: ['vendedores'],
        queryFn: async () => {
            const res = await client.get('/pedidos/vendedores/all');
            return res.data;
        }
    });

    // Mutations
    const createMutation = useMutation({
        mutationFn: (newV: Partial<Vendedor>) => client.post('/pedidos/vendedores', newV),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['vendedores'] });
            setIsAdding(false);
        }
    });

    const updateMutation = useMutation({
        mutationFn: (v: Vendedor) => client.put(`/pedidos/vendedores/${v.id}`, v),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['vendedores'] });
            setEditingVendedor(null);
        }
    });

    const deleteMutation = useMutation({
        mutationFn: (id: number) => client.delete(`/pedidos/vendedores/${id}`),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['vendedores'] })
    });

    if (isLoading) return <div className="space-y-4">
        {[1, 2, 3].map(i => <div key={i} className="h-16 bg-gray-50 rounded-lg animate-pulse"></div>)}
    </div>;

    return (
        <div className="max-w-3xl mx-auto space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Gestión de Vendedores</h1>
                    <p className="text-gray-500 text-sm">Administración de comerciales asignables a Notas de Venta</p>
                </div>
                <button
                    onClick={() => setIsAdding(true)}
                    className="flex items-center gap-2 px-4 py-2 bg-corporate-blue text-white rounded-lg hover:bg-blue-800 transition-all font-medium"
                >
                    <Plus size={18} /> Nuevo Vendedor
                </button>
            </div>

            <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
                <div className="divide-y divide-gray-100">
                    {isAdding && (
                        <VendedorRow
                            onSave={(v) => createMutation.mutate(v)}
                            onCancel={() => setIsAdding(false)}
                        />
                    )}

                    {vendedores?.map(v => (
                        <div key={v.id}>
                            {editingVendedor?.id === v.id ? (
                                <VendedorRow
                                    initialData={v}
                                    onSave={(val) => updateMutation.mutate(val as Vendedor)}
                                    onCancel={() => setEditingVendedor(null)}
                                />
                            ) : (
                                <div className="p-4 flex items-center justify-between group hover:bg-gray-50 transition-colors">
                                    <div className="flex items-center gap-4">
                                        <div className={clsx(
                                            "p-2 rounded-full",
                                            v.activo ? "bg-blue-50 text-blue-600" : "bg-gray-50 text-gray-400"
                                        )}>
                                            <UserCircle size={24} />
                                        </div>
                                        <div>
                                            <div className="font-semibold text-gray-900">{v.nombre}</div>
                                            <div className="flex items-center gap-2">
                                                <span className={clsx(
                                                    "w-2 h-2 rounded-full",
                                                    v.activo ? "bg-green-500" : "bg-gray-300"
                                                )}></span>
                                                <span className="text-xs text-gray-500">{v.activo ? 'Activo' : 'Inactivo'}</span>
                                            </div>
                                        </div>
                                    </div>
                                    <div className="flex items-center gap-2 transition-all">
                                        <button
                                            onClick={() => setEditingVendedor(v)}
                                            className="p-2 text-gray-400 hover:text-blue-600 hover:bg-white border hover:border-blue-100 rounded-lg shadow-sm transition-all"
                                            title="Editar"
                                        >
                                            <Edit2 size={16} />
                                        </button>
                                        <button
                                            onClick={() => {
                                                if (confirm('¿Seguro que desea desactivar este vendedor?')) {
                                                    deleteMutation.mutate(v.id);
                                                }
                                            }}
                                            className="p-2 text-gray-400 hover:text-red-600 hover:bg-white border hover:border-red-100 rounded-lg shadow-sm transition-all"
                                            title="Eliminar/Desactivar"
                                        >
                                            <Trash2 size={16} />
                                        </button>
                                    </div>
                                </div>
                            )}
                        </div>
                    ))}

                    {vendedores?.length === 0 && !isAdding && (
                        <div className="p-12 text-center text-gray-400 italic">
                            No hay vendedores registrados.
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

const VendedorRow = ({ initialData, onSave, onCancel }: {
    initialData?: Vendedor,
    onSave: (v: Partial<Vendedor>) => void,
    onCancel: () => void
}) => {
    const [nombre, setNombre] = useState(initialData?.nombre || '');
    const [activo, setActivo] = useState(initialData?.activo ?? true);

    return (
        <div className="p-4 bg-blue-50/50 flex items-center gap-4 animate-in fade-in slide-in-from-left-2">
            <input
                type="text"
                autoFocus
                value={nombre}
                onChange={e => setNombre(e.target.value)}
                placeholder="Nombre del Vendedor"
                className="flex-1 px-3 py-2 border rounded-lg shadow-sm outline-none focus:ring-2 focus:ring-blue-500 text-sm"
            />
            <div className="flex items-center gap-2 mr-4">
                <input
                    type="checkbox"
                    id="activo"
                    checked={activo}
                    onChange={e => setActivo(e.target.checked)}
                    className="w-4 h-4 text-blue-600 rounded"
                />
                <label htmlFor="activo" className="text-sm font-medium text-gray-600">Activo</label>
            </div>
            <div className="flex items-center gap-2">
                <button
                    onClick={() => onSave({ ...initialData, nombre, activo })}
                    className="p-2 bg-corporate-blue text-white rounded-lg hover:bg-blue-800 transition-all shadow-sm"
                >
                    <Check size={18} />
                </button>
                <button
                    onClick={onCancel}
                    className="p-2 bg-white text-gray-400 border border-gray-200 rounded-lg hover:bg-gray-50 transition-all shadow-sm"
                >
                    <X size={18} />
                </button>
            </div>
        </div>
    );
};
