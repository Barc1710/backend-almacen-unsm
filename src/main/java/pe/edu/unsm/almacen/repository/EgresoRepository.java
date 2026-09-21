package pe.edu.unsm.almacen.repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.unsm.almacen.entity.Egreso;

public interface EgresoRepository extends JpaRepository<Egreso, Integer> {

    @Query("SELECT COALESCE(MAX(e.correlativo), 0) FROM Egreso e WHERE e.prefijo = :prefijo")
    Integer obtenerUltimoCorrelativo(@Param("prefijo") String prefijo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Egreso> findFirstByPrefijoOrderByCorrelativoDesc(String prefijo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Egreso e WHERE e.id = :id")
    Optional<Egreso> findByIdWithLock(@Param("id") Integer id);

    @Query(value = """
        SELECT e FROM Egreso e
        WHERE (:idCliente IS NULL OR e.cliente.id = :idCliente)
          AND (:idArea IS NULL OR e.area.id = :idArea)
          AND (:desde IS NULL OR e.fecha >= :desde)
          AND (:hasta IS NULL OR e.fecha <= :hasta)
          AND (:estado IS NULL OR e.estado = :estado)
        ORDER BY e.fecha DESC, e.id DESC
        """,
        countQuery = """
        SELECT COUNT(e) FROM Egreso e
        WHERE (:idCliente IS NULL OR e.cliente.id = :idCliente)
          AND (:idArea IS NULL OR e.area.id = :idArea)
          AND (:desde IS NULL OR e.fecha >= :desde)
          AND (:hasta IS NULL OR e.fecha <= :hasta)
          AND (:estado IS NULL OR e.estado = :estado)
        """)
    @EntityGraph(attributePaths = {"cliente", "encargado", "area", "encargadoAlmacen"})
    Page<Egreso> listarPaginado(
            @Param("idCliente") Integer idCliente,
            @Param("idArea") Integer idArea,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("estado") String estado,
            Pageable pageable
    );

    @Override
    @EntityGraph(attributePaths = {"cliente", "encargado", "area", "encargadoAlmacen"})
    Optional<Egreso> findById(Integer id);
}
