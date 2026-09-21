package pe.edu.unsm.almacen.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.unsm.almacen.entity.KardexMovimiento;

public interface KardexMovimientoRepository extends JpaRepository<KardexMovimiento, Long> {

    @EntityGraph(attributePaths = {"articulo", "usuario"})
    List<KardexMovimiento> findByArticulo_IdOrderByFechaHoraDescIdDesc(Integer idArticulo);

    @EntityGraph(attributePaths = {"articulo", "usuario"})
    Page<KardexMovimiento> findByArticulo_IdOrderByFechaHoraDescIdDesc(Integer idArticulo, Pageable pageable);
}
