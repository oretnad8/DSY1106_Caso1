import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { ProductSearch } from '../components/pedidos/ProductSearch';
import type { Producto, DetallePedido, Vendedor } from '../types';
import { Trash2, Send, Save, User as UserIcon, Plus, Users } from 'lucide-react';
import client from '../api/client';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';

export const SalesNotes = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [cliente, setCliente] = useState('');
    const [vendedorId, setVendedorId] = useState(0);
    const [detalles, setDetalles] = useState<DetallePedido[]>([]);
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState(false);

    // Fetch Vendedores
    const { data: vendedores } = useQuery<Vendedor[]>({
        queryKey: ['vendedores-select'],
        queryFn: async () => {
            const res = await client.get('/pedidos/vendedores/all');
            return res.data;
        }
    });



    const handleAddProduct = (p: Producto) => {
        const existing = detalles.find(d => d.sku === p.sku);
        if (existing) {
            setDetalles(detalles.map(d => d.sku === p.sku
                ? { ...d, cantidadSolicitada: d.cantidadSolicitada + 1 }
                : d
            ));
        } else {
            // Sugerir la primera ubicación encontrada en el buscador
            const sugerencia = p.ubicaciones && p.ubicaciones.length > 0
                ? p.ubicaciones[0].codigoUbicacion
                : 'SIN_UBICACION';

            setDetalles([...detalles, {
                sku: p.sku,
                descripcion: p.descripcion,
                cantidadSolicitada: 1,
                ubicacionSugerida: sugerencia
            }]);
        }
    };

    const handleRemoveProduct = (sku: string) => {
        setDetalles(detalles.filter(d => d.sku !== sku));
    };

    const handleUpdateQuantity = (sku: string, qty: number) => {
        if (qty < 1) return;
        setDetalles(detalles.map(d => d.sku === sku ? { ...d, cantidadSolicitada: qty } : d));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (detalles.length === 0) return alert('Debe agregar al menos un producto');

        setLoading(true);
        try {
            const payload = {
                cliente: cliente,
                vendedor_id: vendedorId, // Cambiado de vendedorId a vendedor_id
                detalles
            };

            await client.post('/pedidos', payload);
            setSuccess(true);
            setTimeout(() => navigate('/tracking'), 2000);
        } catch (error) {
            console.error('Error creating order:', error);
            alert('Error al crear la nota de venta');
        } finally {
            setLoading(false);
        }
    };

    if (success) {
        return (
            <div className="flex flex-col items-center justify-center h-full py-12">
                <div className="w-16 h-16 bg-green-100 text-green-600 rounded-full flex items-center justify-center mb-4">
                    <Save size={32} />
                </div>
                <h2 className="text-2xl font-bold text-gray-900">¡Nota de Venta Creada!</h2>
                <p className="text-gray-500 mt-2">Redirigiendo al monitor de estados...</p>
            </div>
        );
    }

    return (
        <div className="max-w-4xl mx-auto space-y-8">
            <div>
                <h1 className="text-2xl font-bold text-gray-900">Nueva Nota de Venta</h1>
                <p className="text-gray-500 text-sm">Supervisor: {user?.username} • ID Vendedor: {vendedorId}</p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-6">
                {/* Client Info */}
                <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm space-y-4">
                    <h2 className="text-sm font-semibold text-gray-400 uppercase tracking-wider flex items-center gap-2">
                        <UserIcon size={16} /> Información del Cliente
                    </h2>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Nombre o Razón Social</label>
                        <input
                            type="text"
                            required
                            value={cliente}
                            onChange={(e) => setCliente(e.target.value)}
                            className="w-full px-4 py-2 border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-blue-500"
                            placeholder="Nombre del cliente..."
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1 flex items-center gap-2">
                            <Users size={14} className="text-gray-400" /> Seleccionar Vendedor
                        </label>
                        <select
                            required
                            value={vendedorId}
                            onChange={(e) => setVendedorId(parseInt(e.target.value))}
                            className="w-full px-4 py-2 border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-blue-500 bg-white"
                        >
                            <option value={0}>-- Seleccione un Vendedor --</option>
                            {vendedores?.filter(v => v.activo).map(v => (
                                <option key={v.id} value={v.id}>
                                    {v.nombre}
                                </option>
                            ))}
                        </select>
                    </div>
                </div>

                {/* Product Selection */}
                <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm space-y-4">
                    <h2 className="text-sm font-semibold text-gray-400 uppercase tracking-wider flex items-center gap-2">
                        <Plus size={16} /> Detalle de Productos
                    </h2>

                    <ProductSearch onSelect={handleAddProduct} />

                    <div className="mt-6 border rounded-lg overflow-hidden">
                        <table className="w-full text-sm">
                            <thead className="bg-gray-50 text-gray-600 font-medium border-b">
                                <tr>
                                    <th className="px-4 py-3 text-left">SKU</th>
                                    <th className="px-4 py-3 text-left">Descripción</th>
                                    <th className="px-4 py-3 text-center w-32">Cantidad</th>
                                    <th className="px-4 py-3 text-right">Acción</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y">
                                {detalles.map((d) => (
                                    <tr key={d.sku}>
                                        <td className="px-4 py-3 font-mono text-xs">{d.sku}</td>
                                        <td className="px-4 py-3">{d.descripcion}</td>
                                        <td className="px-4 py-3">
                                            <div className="flex items-center justify-center gap-2">
                                                <input
                                                    type="number"
                                                    value={d.cantidadSolicitada}
                                                    onChange={(e) => handleUpdateQuantity(d.sku, parseInt(e.target.value) || 0)}
                                                    className="w-16 px-2 py-1 border rounded text-center outline-none focus:ring-1 focus:ring-blue-500"
                                                />
                                            </div>
                                        </td>
                                        <td className="px-4 py-3 text-right">
                                            <button
                                                type="button"
                                                onClick={() => handleRemoveProduct(d.sku)}
                                                className="p-1 text-red-400 hover:text-red-600 hover:bg-red-50 rounded transition-all"
                                            >
                                                <Trash2 size={18} />
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                                {detalles.length === 0 && (
                                    <tr>
                                        <td colSpan={4} className="px-4 py-10 text-center text-gray-400 italic">
                                            No hay productos agregados.
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>

                <div className="flex justify-end gap-3">
                    <button
                        type="button"
                        onClick={() => navigate('/tracking')}
                        className="px-6 py-2 text-gray-600 font-medium hover:bg-gray-100 rounded-lg transition-colors"
                    >
                        Cancelar
                    </button>
                    <button
                        type="submit"
                        disabled={loading || detalles.length === 0}
                        className="flex items-center gap-2 px-8 py-2 bg-corporate-blue hover:bg-blue-800 text-white font-semibold rounded-lg shadow-md hover:shadow-lg transition-all disabled:opacity-50"
                    >
                        <Send size={18} />
                        {loading ? 'Enviando...' : 'Emitir Nota de Venta'}
                    </button>
                </div>
            </form>
        </div>
    );
};
