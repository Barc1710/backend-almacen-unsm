package pe.edu.unsm.almacen.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.unsm.almacen.entity.Encargado;

public interface EncargadoRepository extends JpaRepository<Encargado, Integer> {

    @EntityGraph(attributePaths = {"usuario"})
    List<Encargado> findByEstadoOrderByApellidosAsc(String estado);

    @Query(value = "SELECT e FROM Encargado e LEFT JOIN FETCH e.usuario WHERE (:filtro IS NULL OR :filtro = '' OR " +
           "LOWER(e.nombres) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.apellidos) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(COALESCE(e.dni, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(COALESCE(e.cargo, '')) LIKE LOWER(CONCAT('%', :filtro, '%'))) AND " +
           "(:estado IS NULL OR :estado = '' OR e.estado = :estado)",
           countQuery = "SELECT COUNT(e) FROM Encargado e WHERE (:filtro IS NULL OR :filtro = '' OR " +
           "LOWER(e.nombres) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.apellidos) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(COALESCE(e.dni, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(COALESCE(e.cargo, '')) LIKE LOWER(CONCAT('%', :filtro, '%'))) AND " +
           "(:estado IS NULL OR :estado = '' OR e.estado = :estado)")
    Page<Encargado> buscar(
            @Param("filtro") String filtro,
            @Param("estado") String estado,
            Pageable pageable
    );
}
