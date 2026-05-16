export type PedidoEstado =
    | 'PENDIENTE'
    | 'ASIGNADO'
    | 'PICKING'
    | 'PICKING_COMPLETADO'
    | 'FACTURADO'
    | 'EGRESADO'
    | 'ENTREGADO'
    | 'CANCELADO'
    | 'PICKING_ASIGNADO'
    | 'EN_PICKING'
    | 'REVISION_FACTURA'
    | 'TRANSPORTE_ASIGNADO'
    | 'EN_TRANSPORTE';

export interface DetallePedido {
    id?: number;
    sku: string;
    descripcion: string;
    cantidadSolicitada: number;
    cantidadPickeada?: number;
    ubicacionSugerida?: string;
}

export interface TrazabilidadItem {
    id: number;
    estado: PedidoEstado;
    usuario: string;
    fecha: string;
    comentario?: string;
}

export interface Pedido {
    id: number;
    cliente: string;
    vendedorId: number;
    vendedorNombre?: string;
    estado: PedidoEstado;
    fechaCreacion: string;
    operadorId?: number;
    operadorNombre?: string;
    transportistaNombre?: string;
    fotoEvidencia?: string;
    numeroFactura?: string;
    urlFactura?: string;
    detalles?: DetallePedido[];
    trazabilidad?: TrazabilidadItem[];
}

// --- PedidoDetalle types (GET /api/pedidos/{id}) ---

export interface ResponsableDTO {
    id: number | null;
    nombre: string | null;
}

export interface HistorialEstadoDTO {
    estado: string;
    timestamp: string;
    usuarioId: number;
    usuarioNombre: string;
}

export interface PedidoDetalle {
    id: number;
    cliente: string;
    fechaCreacion: string;
    fechaEntrega?: string | null;
    estadoActual: string;
    numeroFactura?: string | null;
    fotoEntrega?: string | null;
    vendedor?: ResponsableDTO | null;
    operadorPicking?: ResponsableDTO | null;
    operadorTransporte?: ResponsableDTO | null;
    detalles: DetallePedido[];
    historial: HistorialEstadoDTO[];
}

// --- Other types ---

export interface Producto {
    sku: string;
    nombre?: string;
    descripcion: string;
    stock: number;
    precio?: number;
    ubicaciones?: {
        codigoUbicacion: string;
        cantidad: number;
    }[];
}

export interface OperadorEficiencia {
    nombre: string;
    totalAsignados: number;
    entregados: number;
    eficiencia: number;
}

export interface PedidoStats {
    pedidosTotales: number;
    eficienciaGlobal: number;
    promedioPickingMinutos: number;
    operadoresActivos: number;
    eficienciaPorOperador: Record<string, OperadorEficiencia>;
    distribucionEstados: Record<string, number>;
}

export interface Usuario {
    id: number;
    nombre: string;
    email: string;
    rol: 'ADMIN' | 'JEFE' | 'SUPERVISOR' | 'OPERADOR';
    activo: boolean;
    password?: string;
}

export interface Vendedor {
    id: number;
    nombre: string;
    activo: boolean;
}
