package pe.edu.unsm.almacen.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.unsm.almacen.entity.Ubicacion;

public interface UbicacionRepository extends JpaRepository<Ubicacion, Integer> {

    List<Ubicacion> findByEstadoOrderByNombreAsc(String estado);

    @Query("SELECT u FROM Ubicacion u WHERE (:filtro IS NULL OR :filtro = '' OR " +
           "LOWER(u.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(COALESCE(u.descripcion, '')) LIKE LOWER(CONCAT('%', :filtro, '%')))")
    Page<Ubicacion> buscar(@Param("filtro") String filtro, Pageable pageable);
}
