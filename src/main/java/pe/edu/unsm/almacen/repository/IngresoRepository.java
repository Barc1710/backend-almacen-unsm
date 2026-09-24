package pe.edu.unsm.almacen.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.unsm.almacen.entity.Ingreso;

public interface IngresoRepository extends JpaRepository<Ingreso, Integer> {

    @EntityGraph(attributePaths = {"proveedor"})
    @Query(value = "SELECT i FROM Ingreso i WHERE " +
           "(:idProveedor IS NULL OR i.proveedor.id = :idProveedor) AND " +
           "(:desde IS NULL OR i.fecha >= :desde) AND " +
           "(:hasta IS NULL OR i.fecha <= :hasta) " +
           "ORDER BY i.fecha DESC",
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

    @EntityGraph(attributePaths = {"proveedor"})
    @Override
    Optional<Ingreso> findById(Integer id);

    boolean existsByNumeroOrdenCompraAndEstado(String numeroOrdenCompra, String estado);
}
