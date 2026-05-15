import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import client from '../api/client';
import type { Pedido } from '../types';
import { CheckCircle, AlertTriangle, X, ReceiptText, FileText } from 'lucide-react';
import { useState } from 'react';
import { clsx } from 'clsx';

export const Billing = () => {
    const queryClient = useQueryClient();
    const [selectedPedido, setSelectedPedido] = useState<Pedido | null>(null);
    const [showValidationModal, setShowValidationModal] = useState(false);
    const [activeTab, setActiveTab] = useState<'INVOICE' | 'EGRESS'>('INVOICE');

    // Invoice modal state
    const [showInvoiceModal, setShowInvoiceModal] = useState(false);
    const [invoicePedido, setInvoicePedido] = useState<Pedido | null>(null);
    const [invoiceNumber, setInvoiceNumber] = useState('');
    const [invoiceError, setInvoiceError] = useState('');

    const { data: allPedidos, isLoading } = useQuery<Pedido[]>({
        queryKey: ['pedidos', 'billing'],
        queryFn: async () => {
            const response = await client.get('/pedidos/todos');
            return Array.isArray(response.data) ? response.data : (response.data?.content || []);
        }
    });

    const pendingInvoice = allPedidos?.filter(p => p.estado === 'PICKING_COMPLETADO') || [];
    const pendingEgress = allPedidos?.filter(p => p.estado === 'FACTURADO') || [];

    const markInvoicedMutation = useMutation({
        mutationFn: ({ id, numeroFactura }: { id: number; numeroFactura: string }) =>
            client.put(`/pedidos/${id}/registrar-factura`, { numeroFactura }),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['pedidos'] });
            closeInvoiceModal();
        },
        onError: () => {
            setInvoiceError('Error al registrar la factura. Intente nuevamente.');
        }
    });

    const validateStockMutation = useMutation({
        mutationFn: (id: number) => client.post(`/pedidos/${id}/revision-factura`, {}),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['pedidos'] });
            setShowValidationModal(false);
            setSelectedPedido(null);
            alert('Stock validado y pedido egresado correctamente.');
        }
    });

    const openInvoiceModal = (pedido: Pedido) => {
        setInvoicePedido(pedido);
        setInvoiceNumber('');
        setInvoiceError('');
        setShowInvoiceModal(true);
    };

    const closeInvoiceModal = () => {
        setShowInvoiceModal(false);
        setInvoicePedido(null);
        setInvoiceNumber('');
        setInvoiceError('');
    };

    const handleSubmitInvoice = () => {
        const trimmed = invoiceNumber.trim();
        if (!trimmed) {
            setInvoiceError('El número de factura no puede estar vacío.');
            return;
        }
        if (!invoicePedido) return;
        setInvoiceError('');
        markInvoicedMutation.mutate({ id: invoicePedido.id, numeroFactura: trimmed });
    };

    if (isLoading) return <div className="p-8 font-medium">Cargando gestión de facturación...</div>;

    const displayList = activeTab === 'INVOICE' ? pendingInvoice : pendingEgress;

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-end">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Gestión de Facturación y Despacho</h1>
                    <p className="text-gray-500 text-sm">Flujo de salida de mercadería y legalización</p>
                </div>

                <div className="bg-gray-100 p-1 rounded-lg flex items-center gap-1 border border-gray-200">
                    <button
                        onClick={() => setActiveTab('INVOICE')}
                        className={clsx(
                            "px-4 py-1.5 text-xs font-bold rounded-md transition-all flex items-center gap-2",
                            activeTab === 'INVOICE' ? "bg-white text-corporate-blue shadow-sm font-semibold" : "text-gray-500 hover:text-gray-700"
                        )}
                    >
                        <ReceiptText size={14} />
                        Pendientes Factura ({pendingInvoice.length})
                    </button>
                    <button
                        onClick={() => setActiveTab('EGRESS')}
                        className={clsx(
                            "px-4 py-1.5 text-xs font-bold rounded-md transition-all flex items-center gap-2",
                            activeTab === 'EGRESS' ? "bg-white text-corporate-blue shadow-sm font-semibold" : "text-gray-500 hover:text-gray-700"
                        )}
                    >
                        <CheckCircle size={14} />
                        Pendientes Egreso ({pendingEgress.length})
                    </button>
                </div>
            </div>

            <div className="bg-white border border-gray-200 rounded-xl overflow-hidden shadow-sm">
                <table className="w-full text-left border-collapse">
                    <thead className="bg-gray-50 border-b border-gray-200 text-gray-600 text-sm font-semibold">
                        <tr>
                            <th className="px-6 py-4">ID Pedido</th>
                            <th className="px-6 py-4">Cliente</th>
                            <th className="px-6 py-4">{activeTab === 'INVOICE' ? 'Fecha Picking' : 'Nº Factura'}</th>
                            <th className="px-6 py-4 text-right">Acciones</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100 text-sm text-gray-700">
                        {displayList.map((pedido) => (
                            <tr key={pedido.id} className="hover:bg-gray-50 transition-colors">
                                <td className="px-6 py-4 font-semibold text-corporate-blue">#{pedido.id}</td>
                                <td className="px-6 py-4 font-medium">{pedido.cliente}</td>
                                <td className="px-6 py-4">
                                    {activeTab === 'INVOICE' ? (
                                        <span className="text-gray-500">{pedido.fechaCreacion}</span>
                                    ) : (
                                        <span className="flex items-center gap-1.5 text-blue-700 font-medium">
                                            <FileText size={14} /> {pedido.numeroFactura || '—'}
                                        </span>
                                    )}
                                </td>
                                <td className="px-6 py-4 text-right">
                                    {activeTab === 'INVOICE' ? (
                                        <button
                                            onClick={() => openInvoiceModal(pedido)}
                                            className="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-bold text-xs shadow-sm"
                                        >
                                            <ReceiptText size={16} />
                                            Registrar Factura
                                        </button>
                                    ) : (
                                        <button
                                            onClick={() => { setSelectedPedido(pedido); setShowValidationModal(true); }}
                                            className="inline-flex items-center gap-2 px-3 py-1.5 bg-green-50 text-green-600 rounded-lg hover:bg-green-100 transition-colors font-medium border border-green-100"
                                        >
                                            <CheckCircle size={16} /> Validar y Egresar
                                        </button>
                                    )}
                                </td>
                            </tr>
                        ))}
                        {displayList.length === 0 && (
                            <tr>
                                <td colSpan={4} className="px-6 py-12 text-center text-gray-500 italic">
                                    No hay pedidos {activeTab === 'INVOICE' ? 'pendientes de factura' : 'listos para egreso'} en este momento.
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>

            {/* Invoice Number Modal */}
            {showInvoiceModal && invoicePedido && (
                <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 z-50">
                    <div className="bg-white rounded-xl shadow-2xl w-full max-w-md overflow-hidden">
                        <div className="p-6 border-b flex justify-between items-center bg-gray-50">
                            <div>
                                <h3 className="text-lg font-bold text-gray-900">Registrar Factura</h3>
                                <p className="text-xs text-gray-500 mt-0.5">Pedido #{invoicePedido.id} — {invoicePedido.cliente}</p>
                            </div>
                            <button onClick={closeInvoiceModal} className="text-gray-400 hover:text-gray-600 transition-colors"><X size={20} /></button>
                        </div>
                        <div className="p-6 space-y-4">
                            <div>
                                <label htmlFor="invoiceNumber" className="block text-sm font-semibold text-gray-700 mb-1.5">
                                    Número de Factura
                                </label>
                                <input
                                    id="invoiceNumber"
                                    type="text"
                                    value={invoiceNumber}
                                    onChange={(e) => { setInvoiceNumber(e.target.value); setInvoiceError(''); }}
                                    onKeyDown={(e) => { if (e.key === 'Enter') handleSubmitInvoice(); }}
                                    placeholder="Ej: F-001234, BOL-5678, NV-0001"
                                    autoFocus
                                    className={clsx(
                                        "w-full px-4 py-2.5 border rounded-lg text-sm transition-colors outline-none focus:ring-2 focus:ring-blue-500/30",
                                        invoiceError ? "border-red-300 bg-red-50" : "border-gray-300 bg-white focus:border-blue-500"
                                    )}
                                />
                                {invoiceError && (
                                    <p className="text-xs text-red-600 mt-1.5 flex items-center gap-1">
                                        <AlertTriangle size={12} /> {invoiceError}
                                    </p>
                                )}
                                <p className="text-[11px] text-gray-400 mt-1.5">
                                    Ingrese el número de factura, boleta o nota de venta asociada a este pedido.
                                </p>
                            </div>
                        </div>
                        <div className="p-6 bg-gray-50 flex justify-end gap-3 border-t">
                            <button onClick={closeInvoiceModal} className="px-4 py-2 text-gray-600 font-medium text-sm hover:text-gray-800 transition-colors">
                                Cancelar
                            </button>
                            <button
                                onClick={handleSubmitInvoice}
                                disabled={markInvoicedMutation.isPending}
                                className="px-6 py-2 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 transition-colors shadow-md text-sm disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                {markInvoicedMutation.isPending ? 'Registrando...' : 'Registrar Factura'}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Validation Modal */}
            {showValidationModal && selectedPedido && (
                <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 z-50">
                    <div className="bg-white rounded-xl shadow-2xl w-full max-w-2xl overflow-hidden">
                        <div className="p-6 border-b flex justify-between items-center bg-gray-50">
                            <h3 className="text-lg font-bold">Validación de Stock vs Picking</h3>
                            <button onClick={() => setShowValidationModal(false)} className="text-gray-400 hover:text-gray-600 transition-colors"><X size={20} /></button>
                        </div>
                        <div className="p-6 space-y-6">
                            <div className="bg-blue-50 border border-blue-100 p-4 rounded-lg flex items-start gap-3">
                                <AlertTriangle className="text-blue-600 mt-1" size={20} />
                                <div className="text-sm text-blue-800">
                                    Verifique que las cantidades pickeadas coincidan con lo solicitado antes de confirmar el egreso de bodega.
                                </div>
                            </div>

                            <div className="border rounded-lg overflow-hidden">
                                <table className="w-full text-sm">
                                    <thead className="bg-gray-100 font-semibold text-gray-600">
                                        <tr>
                                            <th className="px-4 py-3 text-left">Producto</th>
                                            <th className="px-4 py-3 text-center">Solicitado</th>
                                            <th className="px-4 py-3 text-center">Pickeado</th>
                                            <th className="px-4 py-3 text-center">Estado</th>
                                        </tr>
                                    </thead>
                                    <tbody className="divide-y">
                                        {(selectedPedido.detalles || []).map((d) => {
                                            const diff = (d.cantidadPickeada || 0) - d.cantidadSolicitada;
                                            return (
                                                <tr key={d.sku}>
                                                    <td className="px-4 py-3">
                                                        <div className="font-medium">{d.descripcion}</div>
                                                        <div className="text-xs text-gray-400 font-mono">{d.sku}</div>
                                                    </td>
                                                    <td className="px-4 py-3 text-center font-semibold">{d.cantidadSolicitada}</td>
                                                    <td className="px-4 py-3 text-center font-semibold text-blue-600">{d.cantidadPickeada || 0}</td>
                                                    <td className="px-4 py-3 text-center">
                                                        {diff === 0 ? (
                                                            <span className="text-green-600 font-bold px-2 py-1 bg-green-50 rounded">OK</span>
                                                        ) : (
                                                            <span className="text-red-600 font-bold px-2 py-1 bg-red-50 rounded">{diff > 0 ? `+${diff}` : diff}</span>
                                                        )}
                                                    </td>
                                                </tr>
                                            );
                                        })}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                        <div className="p-6 bg-gray-50 flex justify-between items-center border-t">
                            <div className="text-[11px] text-gray-500 italic max-w-[250px]">
                                * Al confirmar, se descontará el stock físico permanentemente y el pedido pasará a EGRESADO.
                            </div>
                            <div className="flex gap-3">
                                <button onClick={() => setShowValidationModal(false)} className="px-4 py-2 text-gray-600 font-medium text-sm">Cancelar</button>
                                <button
                                    onClick={() => validateStockMutation.mutate(selectedPedido.id)}
                                    disabled={validateStockMutation.isPending}
                                    className="px-6 py-2 bg-green-600 text-white rounded-lg font-semibold hover:bg-green-700 transition-colors shadow-md text-sm"
                                >
                                    {validateStockMutation.isPending ? 'Procesando...' : 'Confirmar Egreso'}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};
