package cl.baeza_munoz.stockflow.repository;

import cl.baeza_munoz.stockflow.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}