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
import pe.edu.unsm.almacen.entity.TipoEgreso;

public interface EgresoRepository extends JpaRepository<Egreso, Integer> {

    @Query("SELECT COALESCE(MAX(e.correlativo), 0) FROM Egreso e WHERE e.prefijo = :prefijo")
    Integer obtenerMaximoCorrelativo(@Param("prefijo") String prefijo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Egreso e WHERE e.id = :id")
    Optional<Egreso> findByIdWithLock(@Param("id") Integer id);

    @Query(value = """
        SELECT e FROM Egreso e
        LEFT JOIN e.cliente c
        LEFT JOIN e.area a
        WHERE (:idCliente IS NULL OR c.id = :idCliente)
          AND (:idArea IS NULL OR a.id = :idArea)
          AND (:tipoEgreso IS NULL OR e.tipoEgreso = :tipoEgreso)
          AND (:desde IS NULL OR e.fecha >= :desde)
          AND (:hasta IS NULL OR e.fecha <= :hasta)
          AND (:estado IS NULL OR e.estado = :estado)
          AND (
               (:filtro IS NULL AND :correlativo IS NULL)
               OR
               (:correlativo IS NOT NULL AND e.correlativo = :correlativo AND (:prefijo IS NULL OR UPPER(e.prefijo) = UPPER(:prefijo)))
               OR
               (:filtro IS NOT NULL AND (
                   LOWER(e.prefijo) LIKE LOWER(CONCAT('%', :filtro, '%')) OR
                   (c.nombre IS NOT NULL AND LOWER(c.nombre) LIKE LOWER(CONCAT('%', :filtro, '%'))) OR
                   (a.nombre IS NOT NULL AND LOWER(a.nombre) LIKE LOWER(CONCAT('%', :filtro, '%'))) OR
                   (e.motivoBaja IS NOT NULL AND LOWER(e.motivoBaja) LIKE LOWER(CONCAT('%', :filtro, '%')))
               ))
          )
        ORDER BY e.fecha DESC, e.id DESC
        """,
        countQuery = """
        SELECT COUNT(e) FROM Egreso e
        LEFT JOIN e.cliente c
        LEFT JOIN e.area a
        WHERE (:idCliente IS NULL OR c.id = :idCliente)
          AND (:idArea IS NULL OR a.id = :idArea)
          AND (:tipoEgreso IS NULL OR e.tipoEgreso = :tipoEgreso)
          AND (:desde IS NULL OR e.fecha >= :desde)
          AND (:hasta IS NULL OR e.fecha <= :hasta)
          AND (:estado IS NULL OR e.estado = :estado)
          AND (
               (:filtro IS NULL AND :correlativo IS NULL)
               OR
               (:correlativo IS NOT NULL AND e.correlativo = :correlativo AND (:prefijo IS NULL OR UPPER(e.prefijo) = UPPER(:prefijo)))
               OR
               (:filtro IS NOT NULL AND (
                   LOWER(e.prefijo) LIKE LOWER(CONCAT('%', :filtro, '%')) OR
                   (c.nombre IS NOT NULL AND LOWER(c.nombre) LIKE LOWER(CONCAT('%', :filtro, '%'))) OR
                   (a.nombre IS NOT NULL AND LOWER(a.nombre) LIKE LOWER(CONCAT('%', :filtro, '%'))) OR
                   (e.motivoBaja IS NOT NULL AND LOWER(e.motivoBaja) LIKE LOWER(CONCAT('%', :filtro, '%')))
               ))
          )
        """)
    @EntityGraph(attributePaths = {"cliente", "encargado", "area", "encargadoAlmacen", "usuario"})
    Page<Egreso> listarPaginado(
            @Param("filtro") String filtro,
            @Param("correlativo") Integer correlativo,
            @Param("prefijo") String prefijo,
            @Param("idCliente") Integer idCliente,
            @Param("idArea") Integer idArea,
            @Param("tipoEgreso") TipoEgreso tipoEgreso,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("estado") String estado,
            Pageable pageable
    );

    @Override
    @EntityGraph(attributePaths = {"cliente", "encargado", "area", "encargadoAlmacen", "usuario"})
    Optional<Egreso> findById(Integer id);
}
