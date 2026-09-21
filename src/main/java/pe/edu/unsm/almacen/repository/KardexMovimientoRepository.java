package pe.edu.unsm.almacen.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.unsm.almacen.entity.KardexMovimiento;

public interface KardexMovimientoRepository extends JpaRepository<KardexMovimiento, Long> {

    @EntityGraph(attributePaths = {"articulo", "usuario"})
    List<KardexMovimiento> findByArticulo_IdOrderByFechaHoraDescIdDesc(Integer idArticulo);

    @Query(value = """
        SELECT k FROM KardexMovimiento k
        WHERE (:idArticulo IS NULL OR k.articulo.id = :idArticulo)
          AND (:desde IS NULL OR k.fechaHora >= :desde)
          AND (:hasta IS NULL OR k.fechaHora <= :hasta)
        """,
        countQuery = """
        SELECT COUNT(k) FROM KardexMovimiento k
        WHERE (:idArticulo IS NULL OR k.articulo.id = :idArticulo)
          AND (:desde IS NULL OR k.fechaHora >= :desde)
          AND (:hasta IS NULL OR k.fechaHora <= :hasta)
        """)
    @EntityGraph(attributePaths = {"articulo", "usuario"})
    Page<KardexMovimiento> filtrarMovimientos(
            @Param("idArticulo") Integer idArticulo,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            Pageable pageable
    );
}
