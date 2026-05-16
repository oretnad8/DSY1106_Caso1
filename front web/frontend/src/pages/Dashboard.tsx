import { useQuery } from '@tanstack/react-query';
import client from '../api/client';
import type { PedidoStats } from '../types';
import {
    ResponsiveContainer,
    PieChart,
    Pie,
    Cell,
    Tooltip
} from 'recharts';
import { TrendingUp, Clock, Package, Users } from 'lucide-react';

/* ── Color mapping for each estado ── */
const STATE_COLORS: Record<string, string> = {
    PENDIENTE: '#eab308',
    PICKING_ASIGNADO: '#3b82f6',
    EN_PICKING: '#8b5cf6',
    PICKING_COMPLETADO: '#6366f1',
    FACTURADO: '#22c55e',
    REVISION_FACTURA: '#14b8a6',
    TRANSPORTE_ASIGNADO: '#f97316',
    EN_TRANSPORTE: '#f59e0b',
    ENTREGADO: '#10b981',
};

const STATE_LABELS: Record<string, string> = {
    PENDIENTE: 'Pendiente',
    PICKING_ASIGNADO: 'Picking Asignado',
    EN_PICKING: 'En Picking',
    PICKING_COMPLETADO: 'Picking Completado',
    FACTURADO: 'Facturado',
    REVISION_FACTURA: 'Revisión Factura',
    TRANSPORTE_ASIGNADO: 'Transporte Asignado',
    EN_TRANSPORTE: 'En Transporte',
    ENTREGADO: 'Entregado',
};

export const Dashboard = () => {
    const { data: stats, isLoading } = useQuery<PedidoStats>({
        queryKey: ['stats'],
        queryFn: async () => {
            const response = await client.get('/pedidos/stats');
            return response.data;
        }
    });

    if (isLoading) return <div className="p-8">Cargando estadísticas...</div>;
    if (!stats) return <div className="p-8">No hay datos de rendimiento disponibles.</div>;

    /* ── Pie chart data (only states with count > 0) ── */
    const pieData = Object.entries(stats.distribucionEstados)
        .filter(([, count]) => count > 0)
        .map(([state, count]) => ({
            name: STATE_LABELS[state] || state,
            value: count,
            color: STATE_COLORS[state] || '#9ca3af',
        }));

    /* ── Operator efficiency list sorted desc ── */
    const operadores = Object.values(stats.eficienciaPorOperador)
        .sort((a, b) => b.eficiencia - a.eficiencia);

    return (
        <div className="space-y-8">
            <div>
                <h1 className="text-2xl font-bold text-gray-900">Dashboard de Rendimiento</h1>
                <p className="text-gray-500 text-sm">Resumen ejecutivo del flujo de bodega</p>
            </div>

            {/* ── KPI Cards ── */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {/* Pedidos Totales */}
                <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
                    <div className="flex items-center gap-4">
                        <div className="p-3 bg-blue-50 text-blue-600 rounded-lg"><Package size={24} /></div>
                        <div>
                            <p className="text-sm text-gray-500 font-medium">Pedidos Totales</p>
                            <h3 className="text-2xl font-bold text-gray-900">{stats.pedidosTotales}</h3>
                        </div>
                    </div>
                </div>
                {/* Promedio Picking */}
                <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
                    <div className="flex items-center gap-4">
                        <div className="p-3 bg-purple-50 text-purple-600 rounded-lg"><Clock size={24} /></div>
                        <div>
                            <p className="text-sm text-gray-500 font-medium">Promedio Picking</p>
                            <h3 className="text-2xl font-bold text-gray-900">{stats.promedioPickingMinutos} min</h3>
                        </div>
                    </div>
                </div>
                {/* Eficiencia */}
                <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
                    <div className="flex items-center gap-4">
                        <div className="p-3 bg-green-50 text-green-600 rounded-lg"><TrendingUp size={24} /></div>
                        <div>
                            <p className="text-sm text-gray-500 font-medium">Eficiencia</p>
                            <h3 className="text-2xl font-bold text-gray-900">{stats.eficienciaGlobal}%</h3>
                        </div>
                    </div>
                </div>
                {/* Operadores Activos */}
                <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
                    <div className="flex items-center gap-4">
                        <div className="p-3 bg-yellow-50 text-yellow-600 rounded-lg"><Users size={24} /></div>
                        <div>
                            <p className="text-sm text-gray-500 font-medium">Operadores Activos</p>
                            <h3 className="text-2xl font-bold text-gray-900">{stats.operadoresActivos}</h3>
                        </div>
                    </div>
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                {/* ── Eficiencia por Operador ── */}
                <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
                    <h2 className="text-lg font-bold mb-6 text-gray-800">Eficiencia por Operador</h2>
                    {operadores.length > 0 ? (
                        <div className="space-y-5">
                            {operadores.map((op) => (
                                <div key={op.nombre}>
                                    <div className="flex items-center justify-between mb-1.5">
                                        <span className="text-sm font-medium text-gray-700">{op.nombre}</span>
                                        <span className="text-sm font-bold text-gray-900">{op.eficiencia.toFixed(1)}%</span>
                                    </div>
                                    <div className="w-full bg-gray-100 rounded-full h-3">
                                        <div
                                            className="h-3 rounded-full transition-all duration-500"
                                            style={{
                                                width: `${Math.min(op.eficiencia, 100)}%`,
                                                backgroundColor: op.eficiencia >= 70 ? '#10b981' : op.eficiencia >= 40 ? '#f59e0b' : '#ef4444',
                                            }}
                                        />
                                    </div>
                                    <div className="flex justify-between mt-1">
                                        <span className="text-xs text-gray-400">{op.entregados} entregados</span>
                                        <span className="text-xs text-gray-400">{op.totalAsignados} asignados</span>
                                    </div>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <div className="h-[300px] flex items-center justify-center text-gray-400">
                            Sin datos de desempeño
                        </div>
                    )}
                </div>

                {/* ── Distribución de Estados (Donut) ── */}
                <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
                    <h2 className="text-lg font-bold mb-6 text-gray-800">Distribución de Estados</h2>
                    {pieData.length > 0 ? (
                        <>
                            <div className="h-72">
                                <ResponsiveContainer width="100%" height="100%">
                                    <PieChart>
                                        <Pie
                                            data={pieData}
                                            cx="50%"
                                            cy="50%"
                                            innerRadius={55}
                                            outerRadius={95}
                                            paddingAngle={3}
                                            dataKey="value"
                                        >
                                            {pieData.map((entry, index) => (
                                                <Cell key={`cell-${index}`} fill={entry.color} />
                                            ))}
                                        </Pie>
                                        <Tooltip
                                            contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
                                            formatter={(value: number) => [`${value} pedidos`, '']}
                                        />
                                    </PieChart>
                                </ResponsiveContainer>
                            </div>
                            <div className="flex flex-wrap justify-center gap-x-5 gap-y-2 mt-2">
                                {pieData.map((item) => (
                                    <div key={item.name} className="flex items-center gap-1.5">
                                        <div className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: item.color }} />
                                        <span className="text-xs text-gray-500">{item.name} ({item.value})</span>
                                    </div>
                                ))}
                            </div>
                        </>
                    ) : (
                        <div className="h-72 flex items-center justify-center text-gray-400">
                            Sin datos de estados
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};
