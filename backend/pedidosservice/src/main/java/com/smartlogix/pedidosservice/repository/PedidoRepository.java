package com.smartlogix.pedidosservice.repository;

import com.smartlogix.pedidosservice.model.EstadoPedido;
import com.smartlogix.pedidosservice.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByEstadoActual(EstadoPedido estado);

    List<Pedido> findByEstadoActualIn(List<EstadoPedido> estados);

    List<Pedido> findByOperadorIdAndEstadoActualIn(Integer operadorId, List<EstadoPedido> estados);
}

