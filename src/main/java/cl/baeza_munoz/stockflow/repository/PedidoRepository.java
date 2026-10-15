package cl.baeza_munoz.stockflow.repository;

import cl.baeza_munoz.stockflow.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    
    // Spring Data JPA crea la consulta SQL automáticamente basándose en el nombre del método
    List<Pedido> findByClienteId(Long clienteId);
}