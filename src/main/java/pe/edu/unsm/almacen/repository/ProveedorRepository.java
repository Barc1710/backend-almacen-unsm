package pe.edu.unsm.almacen.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.unsm.almacen.entity.Proveedor;

public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {

    List<Proveedor> findByEstadoOrderByRazonSocialAsc(String estado);

    @Query("SELECT p FROM Proveedor p WHERE (:filtro IS NULL OR :filtro = '' OR " +
           "LOWER(p.razonSocial) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(COALESCE(p.ruc, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(COALESCE(p.contacto, '')) LIKE LOWER(CONCAT('%', :filtro, '%')))")
    Page<Proveedor> buscar(@Param("filtro") String filtro, Pageable pageable);
}
