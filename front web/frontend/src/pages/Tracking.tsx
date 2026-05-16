import { useQuery } from '@tanstack/react-query';
import client from '../api/client';
import type { Pedido, PedidoDetalle } from '../types';
import { StatusBadge } from '../components/ui/StatusBadge';
import { Search, ChevronRight, X, Camera, User, Truck, Package, Clock, FileText, MapPin } from 'lucide-react';
import { useState } from 'react';
import { clsx } from 'clsx';

/** Ensure a base64 string has the data URI prefix for <img src> */
const toImageSrc = (raw: string): string => {
    if (raw.startsWith('data:')) return raw;
    return `data:image/jpeg;base64,${raw}`;
};

const estadoLabel: Record<string, string> = {
    PENDIENTE: 'Pendiente',
    ASIGNADO: 'Asignado',
    PICKING: 'En Picking',
    PICKING_ASIGNADO: 'Picking Asignado',
    EN_PICKING: 'En Picking',
    PICKING_COMPLETADO: 'Picking Completado',
    FACTURADO: 'Facturado',
    REVISION_FACTURA: 'Revisión Factura',
    TRANSPORTE_ASIGNADO: 'Transporte Asignado',
    EN_TRANSPORTE: 'En Transporte',
    ENTREGADO: 'Entregado',
    EGRESADO: 'Egresado',
    CANCELADO: 'Cancelado',
};

export const Tracking = () => {
    const [searchTerm, setSearchTerm] = useState('');
    const [statusFilter, setStatusFilter] = useState('ALL');
    const [showAll, setShowAll] = useState(false);

    // Detail modal
    const [selectedPedidoId, setSelectedPedidoId] = useState<number | null>(null);
    const [showPhotoModal, setShowPhotoModal] = useState(false);

    const { data: pedidos, isLoading, error } = useQuery<Pedido[]>({
        queryKey: ['pedidos', showAll ? 'todos' : 'pendientes'],
        queryFn: async () => {
            const endpoint = showAll ? '/pedidos/todos' : '/pedidos/pendientes';
            const response = await client.get(endpoint);
            return Array.isArray(response.data) ? response.data : (response.data?.content || []);
        }
    });

    const { data: detalle, isLoading: detalleLoading } = useQuery<PedidoDetalle>({
        queryKey: ['pedido-detalle', selectedPedidoId],
        queryFn: async () => {
            const response = await client.get(`/pedidos/${selectedPedidoId}`);
            return response.data;
        },
        enabled: !!selectedPedidoId,
    });

    const filteredPedidos = pedidos?.filter(p => {
        const clienteNom = p.cliente || '';
        const pedidoId = p.id?.toString() || '';

        const matchesSearch = clienteNom.toLowerCase().includes(searchTerm.toLowerCase()) ||
            pedidoId.includes(searchTerm);

        const matchesStatus = statusFilter === 'ALL' || p.estado === statusFilter;
        return matchesSearch && matchesStatus;
    });

    if (isLoading) return <div className="animate-pulse flex flex-col gap-4">
        <div className="h-10 bg-gray-200 rounded w-full"></div>
        <div className="h-64 bg-gray-200 rounded w-full"></div>
    </div>;

    if (error) return <div className="bg-red-50 text-red-600 p-4 rounded-lg">Error cargando pedidos</div>;

    const formatTimestamp = (ts: string) => {
        try {
            const d = new Date(ts);
            return d.toLocaleDateString('es-CL', { day: '2-digit', month: '2-digit', year: 'numeric' })
                + ' ' + d.toLocaleTimeString('es-CL', { hour: '2-digit', minute: '2-digit' });
        } catch { return ts; }
    };

    return (
        <div className="space-y-6">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Monitor de Seguimiento</h1>
                    <p className="text-gray-500 text-sm">Visualización del ciclo de vida de las Notas de Venta</p>
                </div>

                <div className="flex items-center gap-3">
                    <div className="bg-gray-100 p-1 rounded-lg flex items-center gap-1 border border-gray-200">
                        <button
                            onClick={() => setShowAll(false)}
                            className={clsx(
                                "px-3 py-1.5 text-xs font-semibold rounded-md transition-all",
                                !showAll ? "bg-white text-corporate-blue shadow-sm" : "text-gray-500 hover:text-gray-700"
                            )}
                        >
                            Pendientes
                        </button>
                        <button
                            onClick={() => setShowAll(true)}
                            className={clsx(
                                "px-3 py-1.5 text-xs font-semibold rounded-md transition-all",
                                showAll ? "bg-white text-corporate-blue shadow-sm" : "text-gray-500 hover:text-gray-700"
                            )}
                        >
                            Histórico
                        </button>
                    </div>
                    <div className="relative">
                        <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
                        <input
                            type="text"
                            placeholder="Buscar cliente o NV..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="pl-10 pr-4 py-2 bg-white border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-blue-500 w-64 text-sm"
                        />
                    </div>
                    <select
                        value={statusFilter}
                        onChange={(e) => setStatusFilter(e.target.value)}
                        className="px-4 py-2 bg-white border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-blue-500 cursor-pointer text-sm"
                    >
                        <option value="ALL">Estados</option>
                        <option value="PENDIENTE">Pendientes</option>
                        <option value="PICKING">En Picking</option>
                        <option value="PICKING_COMPLETADO">Cerrados</option>
                        <option value="FACTURADO">Facturados</option>
                        <option value="EGRESADO">Egresados</option>
                        <option value="ENTREGADO">Entregados</option>
                        <option value="CANCELADO">Cancelados</option>
                    </select>
                </div>
            </div>

            <div className="bg-white border border-gray-200 rounded-xl overflow-hidden shadow-sm">
                <table className="w-full text-left border-collapse">
                    <thead className="bg-gray-50 border-b border-gray-200 text-gray-600 text-sm font-semibold">
                        <tr>
                            <th className="px-6 py-4">ID / Fecha</th>
                            <th className="px-6 py-4">Cliente</th>
                            <th className="px-6 py-4">Vendedor</th>
                            <th className="px-6 py-4">Operador</th>
                            <th className="px-6 py-4">Estado</th>
                            <th className="px-6 py-4 text-right">Acciones</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100 text-sm text-gray-700">
                        {filteredPedidos?.map((pedido) => (
                            <tr key={pedido.id} className="hover:bg-gray-50 transition-colors cursor-pointer" onClick={() => setSelectedPedidoId(pedido.id)}>
                                <td className="px-6 py-4">
                                    <div className="font-semibold text-corporate-blue">#{pedido.id}</div>
                                    <div className="text-xs text-gray-400">{pedido.fechaCreacion}</div>
                                </td>
                                <td className="px-6 py-4 font-medium">{pedido.cliente}</td>
                                <td className="px-6 py-4">{pedido.vendedorNombre || pedido.vendedorId}</td>
                                <td className="px-6 py-4">
                                    {pedido.operadorNombre ? (
                                        <div className="flex items-center gap-2">
                                            <div className="w-6 h-6 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center text-[10px] font-bold">
                                                {pedido.operadorNombre.charAt(0)}
                                            </div>
                                            {pedido.operadorNombre}
                                        </div>
                                    ) : (
                                        <span className="text-gray-400 italic">Sin asignar</span>
                                    )}
                                </td>
                                <td className="px-6 py-4">
                                    <StatusBadge estado={pedido.estado} />
                                </td>
                                <td className="px-6 py-4 text-right">
                                    <button
                                        onClick={(e) => { e.stopPropagation(); setSelectedPedidoId(pedido.id); }}
                                        className="p-2 text-gray-400 hover:text-corporate-blue hover:bg-gray-100 rounded-lg transition-all"
                                        title="Ver Detalles"
                                    >
                                        <ChevronRight size={18} />
                                    </button>
                                </td>
                            </tr>
                        ))}
                        {filteredPedidos?.length === 0 && (
                            <tr>
                                <td colSpan={6} className="px-6 py-12 text-center text-gray-500">
                                    No se encontraron pedidos con los criterios seleccionados
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>

            {/* Detail Modal */}
            {selectedPedidoId && (
                <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 z-50">
                    <div className="bg-white rounded-xl shadow-2xl w-full max-w-4xl max-h-[90vh] overflow-hidden flex flex-col">
                        {/* Header */}
                        <div className="p-6 border-b flex justify-between items-center bg-gray-50 shrink-0">
                            <div>
                                <h3 className="text-lg font-bold text-gray-900">
                                    Detalle NV #{selectedPedidoId}
                                </h3>
                                {detalle && (
                                    <p className="text-sm text-gray-500 mt-0.5">
                                        {detalle.cliente} — <span className="font-medium">{estadoLabel[detalle.estadoActual] || detalle.estadoActual}</span>
                                    </p>
                                )}
                            </div>
                            <button onClick={() => { setSelectedPedidoId(null); setShowPhotoModal(false); }} className="text-gray-400 hover:text-gray-600 transition-colors">
                                <X size={20} />
                            </button>
                        </div>

                        {/* Body */}
                        <div className="overflow-y-auto flex-1 p-6 space-y-6">
                            {detalleLoading ? (
                                <div className="flex flex-col items-center justify-center py-12 gap-3">
                                    <div className="w-8 h-8 border-3 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
                                    <p className="text-sm text-gray-500">Cargando detalle...</p>
                                </div>
                            ) : detalle ? (
                                <>
                                    {/* Info Cards Row */}
                                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                                        <div className="bg-gray-50 border border-gray-200 rounded-lg p-4">
                                            <div className="text-xs text-gray-500 mb-1">Estado</div>
                                            <StatusBadge estado={detalle.estadoActual as any} />
                                        </div>
                                        <div className="bg-gray-50 border border-gray-200 rounded-lg p-4">
                                            <div className="text-xs text-gray-500 mb-1">Nº Factura</div>
                                            <div className="text-sm font-semibold text-gray-800 flex items-center gap-1.5">
                                                <FileText size={14} className="text-blue-600" />
                                                {detalle.numeroFactura || <span className="text-gray-400 italic font-normal">Sin factura</span>}
                                            </div>
                                        </div>
                                        <div className="bg-gray-50 border border-gray-200 rounded-lg p-4">
                                            <div className="text-xs text-gray-500 mb-1">Fecha Creación</div>
                                            <div className="text-sm font-medium text-gray-800">{formatTimestamp(detalle.fechaCreacion)}</div>
                                        </div>
                                        <div className="bg-gray-50 border border-gray-200 rounded-lg p-4">
                                            <div className="text-xs text-gray-500 mb-1">Fecha Entrega</div>
                                            <div className="text-sm font-medium text-gray-800">
                                                {detalle.fechaEntrega ? formatTimestamp(detalle.fechaEntrega) : <span className="text-gray-400 italic">Pendiente</span>}
                                            </div>
                                        </div>
                                    </div>

                                    {/* Delivery Photo */}
                                    {detalle.fotoEntrega && (
                                        <div className="bg-gray-50 border border-gray-200 rounded-lg p-4">
                                            <div className="mb-3">
                                                <h4 className="text-sm font-bold text-gray-700 flex items-center gap-2">
                                                    <Camera size={16} className="text-emerald-600" /> Foto de Entrega
                                                </h4>
                                            </div>
                                            <img
                                                src={toImageSrc(detalle.fotoEntrega)}
                                                alt="Foto de entrega"
                                                className="w-full max-w-xs rounded-lg border border-gray-200 shadow-sm cursor-pointer hover:opacity-90 transition-opacity"
                                                onClick={() => setShowPhotoModal(true)}
                                            />
                                        </div>
                                    )}

                                    {/* Responsible Parties */}
                                    <div>
                                        <h4 className="text-sm font-bold text-gray-700 mb-3">Responsables</h4>
                                        <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                                            <div className="flex items-center gap-3 bg-blue-50 border border-blue-100 rounded-lg p-3">
                                                <div className="w-9 h-9 rounded-full bg-blue-600 text-white flex items-center justify-center shrink-0">
                                                    <User size={16} />
                                                </div>
                                                <div>
                                                    <div className="text-[11px] text-blue-600 font-semibold uppercase tracking-wide">Vendedor</div>
                                                    <div className="text-sm font-medium text-gray-800">{detalle.vendedor?.nombre || <span className="text-gray-400 italic">—</span>}</div>
                                                </div>
                                            </div>
                                            <div className="flex items-center gap-3 bg-purple-50 border border-purple-100 rounded-lg p-3">
                                                <div className="w-9 h-9 rounded-full bg-purple-600 text-white flex items-center justify-center shrink-0">
                                                    <Package size={16} />
                                                </div>
                                                <div>
                                                    <div className="text-[11px] text-purple-600 font-semibold uppercase tracking-wide">Operador Picking</div>
                                                    <div className="text-sm font-medium text-gray-800">{detalle.operadorPicking?.nombre || <span className="text-gray-400 italic">—</span>}</div>
                                                </div>
                                            </div>
                                            <div className="flex items-center gap-3 bg-orange-50 border border-orange-100 rounded-lg p-3">
                                                <div className="w-9 h-9 rounded-full bg-orange-600 text-white flex items-center justify-center shrink-0">
                                                    <Truck size={16} />
                                                </div>
                                                <div>
                                                    <div className="text-[11px] text-orange-600 font-semibold uppercase tracking-wide">Operador Transporte</div>
                                                    <div className="text-sm font-medium text-gray-800">{detalle.operadorTransporte?.nombre || <span className="text-gray-400 italic">—</span>}</div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                    {/* Items Table */}
                                    <div>
                                        <h4 className="text-sm font-bold text-gray-700 mb-3">Items del Pedido</h4>
                                        <div className="border border-gray-200 rounded-lg overflow-hidden">
                                            <table className="w-full text-sm">
                                                <thead className="bg-gray-100 text-gray-600 font-semibold">
                                                    <tr>
                                                        <th className="px-4 py-3 text-left">Producto</th>
                                                        <th className="px-4 py-3 text-center">Ubicación</th>
                                                        <th className="px-4 py-3 text-center">Solicitado</th>
                                                        <th className="px-4 py-3 text-center">Pickeado</th>
                                                    </tr>
                                                </thead>
                                                <tbody className="divide-y divide-gray-100">
                                                    {detalle.detalles.map((d, i) => (
                                                        <tr key={i} className="hover:bg-gray-50">
                                                            <td className="px-4 py-3">
                                                                <div className="font-medium text-gray-800">{d.descripcion}</div>
                                                                <div className="text-xs text-gray-400 font-mono">{d.sku}</div>
                                                            </td>
                                                            <td className="px-4 py-3 text-center">
                                                                {d.ubicacionSugerida ? (
                                                                    <span className="inline-flex items-center gap-1 text-xs font-medium text-gray-600 bg-gray-100 px-2 py-0.5 rounded">
                                                                        <MapPin size={10} /> {d.ubicacionSugerida}
                                                                    </span>
                                                                ) : '—'}
                                                            </td>
                                                            <td className="px-4 py-3 text-center font-semibold">{d.cantidadSolicitada}</td>
                                                            <td className="px-4 py-3 text-center font-semibold">
                                                                <span className={clsx(
                                                                    (d.cantidadPickeada ?? 0) === d.cantidadSolicitada ? 'text-green-600' : 'text-orange-600'
                                                                )}>
                                                                    {d.cantidadPickeada ?? 0}
                                                                </span>
                                                            </td>
                                                        </tr>
                                                    ))}
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>

                                    {/* Timeline */}
                                    {detalle.historial && detalle.historial.length > 0 && (
                                        <div>
                                            <h4 className="text-sm font-bold text-gray-700 mb-3 flex items-center gap-2">
                                                <Clock size={14} /> Historial de Estados
                                            </h4>
                                            <div className="relative pl-6">
                                                {/* Vertical line */}
                                                <div className="absolute left-[11px] top-2 bottom-2 w-0.5 bg-gray-200"></div>

                                                <div className="space-y-4">
                                                    {[...detalle.historial].sort((a, b) => new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime()).map((h, idx) => {
                                                        const isLast = idx === detalle.historial.length - 1;
                                                        return (
                                                            <div key={idx} className="relative flex items-start gap-3">
                                                                {/* Node */}
                                                                <div className={clsx(
                                                                    "absolute -left-6 top-1 w-[14px] h-[14px] rounded-full border-2 shrink-0 z-10",
                                                                    isLast
                                                                        ? "bg-emerald-500 border-emerald-500"
                                                                        : "bg-white border-gray-300"
                                                                )}></div>

                                                                <div className="flex-1 min-w-0">
                                                                    <div className="flex items-center gap-2 flex-wrap">
                                                                        <span className={clsx(
                                                                            "text-xs font-bold px-2 py-0.5 rounded",
                                                                            isLast ? "bg-emerald-100 text-emerald-800" : "bg-gray-100 text-gray-700"
                                                                        )}>
                                                                            {estadoLabel[h.estado] || h.estado.replace(/_/g, ' ')}
                                                                        </span>
                                                                        <span className="text-[11px] text-gray-400">{formatTimestamp(h.timestamp)}</span>
                                                                    </div>
                                                                    <div className="text-xs text-gray-500 mt-0.5">
                                                                        por <span className="font-medium text-gray-700">{h.usuarioNombre}</span>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                        );
                                                    })}
                                                </div>
                                            </div>
                                        </div>
                                    )}
                                </>
                            ) : (
                                <div className="text-center text-gray-500 py-12">No se pudo cargar el detalle del pedido.</div>
                            )}
                        </div>

                        {/* Footer */}
                        <div className="p-4 border-t bg-gray-50 flex justify-end shrink-0">
                            <button onClick={() => { setSelectedPedidoId(null); setShowPhotoModal(false); }} className="px-5 py-2 text-gray-600 font-medium text-sm hover:text-gray-800 transition-colors">
                                Cerrar
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Fullscreen Photo Modal */}
            {showPhotoModal && detalle?.fotoEntrega && (
                <div className="fixed inset-0 bg-black/90 flex items-center justify-center z-[60]" onClick={() => setShowPhotoModal(false)}>
                    <button onClick={() => setShowPhotoModal(false)} className="absolute top-4 right-4 text-white/70 hover:text-white transition-colors z-10">
                        <X size={28} />
                    </button>
                    <img
                        src={toImageSrc(detalle.fotoEntrega)}
                        alt="Foto de entrega"
                        className="max-w-[90vw] max-h-[90vh] object-contain rounded-lg shadow-2xl"
                        onClick={(e) => e.stopPropagation()}
                    />
                </div>
            )}
        </div>
    );
};
