package pe.edu.unsm.almacen.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.unsm.almacen.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    List<Cliente> findByEstadoOrderByNombreAsc(String estado);

    @Query("SELECT c FROM Cliente c WHERE (:filtro IS NULL OR :filtro = '' OR " +
           "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(COALESCE(c.dni, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(COALESCE(c.correo, '')) LIKE LOWER(CONCAT('%', :filtro, '%')))")
    Page<Cliente> buscar(@Param("filtro") String filtro, Pageable pageable);
}
