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
import pe.edu.unsm.almacen.entity.Ingreso;

public interface IngresoRepository extends JpaRepository<Ingreso, Integer> {

    @EntityGraph(attributePaths = {"proveedor", "usuario"})
    @Query(value = "SELECT i FROM Ingreso i WHERE " +
           "(:idProveedor IS NULL OR i.proveedor.id = :idProveedor) AND " +
           "(:desde IS NULL OR i.fecha >= :desde) AND " +
           "(:hasta IS NULL OR i.fecha <= :hasta) " +
           "ORDER BY i.fecha DESC, i.id DESC",
           countQuery = "SELECT COUNT(i) FROM Ingreso i WHERE " +
           "(:idProveedor IS NULL OR i.proveedor.id = :idProveedor) AND " +
           "(:desde IS NULL OR i.fecha >= :desde) AND " +
           "(:hasta IS NULL OR i.fecha <= :hasta)")
    Page<Ingreso> listarPaginado(
            @Param("idProveedor") Integer idProveedor,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"proveedor", "usuario"})
    @Override
    Optional<Ingreso> findById(Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Ingreso i WHERE i.id = :id")
    Optional<Ingreso> findByIdWithLock(@Param("id") Integer id);

    boolean existsByNumeroOrdenCompraAndEstado(String numeroOrdenCompra, String estado);

    @Query("SELECT MAX(i.correlativo) FROM Ingreso i WHERE i.prefijo = :prefijo")
    Integer obtenerMaximoCorrelativo(@Param("prefijo") String prefijo);

    boolean existsByPrefijoAndCorrelativo(String prefijo, Integer correlativo);
}
