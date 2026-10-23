package cl.baeza_munoz.stockflow.repository;

import cl.baeza_munoz.stockflow.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    
    List<Pedido> findByClienteId(Long clienteId);

    List<Pedido> findByClienteIdOrderByFechaCreacionDesc(Long clienteId);

    long countByEstadoIgnoreCase(String estado);

    List<Pedido> findTop5ByOrderByFechaCreacionDesc();

    List<Pedido> findAllByOrderByFechaCreacionDesc();
}