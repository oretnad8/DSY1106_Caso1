package com.smartlogix.aprobacionesservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "aprobaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Aprobacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false)
    private TipoMovimiento tipoMovimiento;

    @Column(length = 9, nullable = false)
    private String sku;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(columnDefinition = "TEXT")
    private String motivo;

    @ManyToOne
    @JoinColumn(name = "id_solicitante", nullable = false)
    private Usuario solicitante;

    @CreationTimestamp
    @Column(name = "fecha_solicitud", nullable = false, updatable = false)
    private LocalDateTime fechaSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado = Estado.PENDIENTE;

    @ManyToOne
    @JoinColumn(name = "id_aprobador")
    private Usuario aprobador;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "id_ubicacion_origen")
    private Integer idUbicacionOrigen;

    @Column(name = "id_ubicacion_destino")
    private Integer idUbicacionDestino;

    public enum TipoMovimiento {
        INGRESO, EGRESO, REUBICACION
    }

    public enum Estado {
        PENDIENTE, APROBADO, RECHAZADO
    }
}
