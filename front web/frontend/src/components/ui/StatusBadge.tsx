import type { PedidoEstado } from '../../types';
import { twMerge } from 'tailwind-merge';

const statusColors: Record<PedidoEstado, string> = {
    PENDIENTE: 'bg-yellow-100 text-yellow-800 border-yellow-200',
    ASIGNADO: 'bg-blue-100 text-blue-800 border-blue-200',
    PICKING: 'bg-purple-100 text-purple-800 border-purple-200',
    PICKING_ASIGNADO: 'bg-blue-100 text-blue-800 border-blue-200',
    EN_PICKING: 'bg-purple-100 text-purple-800 border-purple-200',
    PICKING_COMPLETADO: 'bg-indigo-100 text-indigo-800 border-indigo-200',
    FACTURADO: 'bg-green-100 text-green-800 border-green-200',
    REVISION_FACTURA: 'bg-teal-100 text-teal-800 border-teal-200',
    TRANSPORTE_ASIGNADO: 'bg-orange-100 text-orange-800 border-orange-200',
    EN_TRANSPORTE: 'bg-amber-100 text-amber-800 border-amber-200',
    ENTREGADO: 'bg-emerald-100 text-emerald-800 border-emerald-200',
    EGRESADO: 'bg-gray-100 text-gray-800 border-gray-200',
    CANCELADO: 'bg-red-100 text-red-800 border-red-200',
};

export const StatusBadge = ({ estado }: { estado: PedidoEstado }) => {
    return (
        <span className={twMerge(
            "px-2.5 py-0.5 rounded-full text-xs font-medium border",
            statusColors[estado] || 'bg-gray-100 text-gray-800 border-gray-200'
        )}>
            {estado.replace('_', ' ')}
        </span>
    );
};
