package cl.baeza_munoz.stockflow.repository;

import cl.baeza_munoz.stockflow.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    
    // Este método resuelve el error en obtenerAlertasDeStock() en el controller
    @Query("SELECT p FROM Producto p WHERE p.stockActual < p.stockMinimo")
    List<Producto> findProductosConStockBajo();
}