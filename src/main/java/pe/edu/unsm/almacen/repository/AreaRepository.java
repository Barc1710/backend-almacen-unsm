package pe.edu.unsm.almacen.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.unsm.almacen.entity.Area;

public interface AreaRepository extends JpaRepository<Area, Integer> {

    List<Area> findByEstadoOrderByNombreAsc(String estado);

    @Query("SELECT a FROM Area a WHERE (:filtro IS NULL OR :filtro = '' OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')))")
    Page<Area> buscar(@Param("filtro") String filtro, Pageable pageable);
}
