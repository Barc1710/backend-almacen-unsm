package pe.edu.unsm.almacen.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.unsm.almacen.entity.EncargadoAlmacen;

public interface EncargadoAlmacenRepository extends JpaRepository<EncargadoAlmacen, Integer> {

    @EntityGraph(attributePaths = {"usuario"})
    List<EncargadoAlmacen> findByEstadoOrderByApellidosAsc(String estado);

    Optional<EncargadoAlmacen> findFirstByEsTitularTrueAndEstado(String estado);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE EncargadoAlmacen e SET e.esTitular = false WHERE e.id <> :id")
    void desmarcarOtrosTitulares(@Param("id") Integer id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE EncargadoAlmacen e SET e.esTitular = false")
    void desmarcarTodosTitulares();

    @Query(value = "SELECT e FROM EncargadoAlmacen e LEFT JOIN FETCH e.usuario WHERE (:filtro IS NULL OR :filtro = '' OR " +
           "LOWER(e.nombres) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.apellidos) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(COALESCE(e.dni, '')) LIKE LOWER(CONCAT('%', :filtro, '%'))) AND " +
           "(:estado IS NULL OR :estado = '' OR e.estado = :estado)",
           countQuery = "SELECT COUNT(e) FROM EncargadoAlmacen e WHERE (:filtro IS NULL OR :filtro = '' OR " +
           "LOWER(e.nombres) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.apellidos) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(COALESCE(e.dni, '')) LIKE LOWER(CONCAT('%', :filtro, '%'))) AND " +
           "(:estado IS NULL OR :estado = '' OR e.estado = :estado)")
    Page<EncargadoAlmacen> buscar(
            @Param("filtro") String filtro,
            @Param("estado") String estado,
            Pageable pageable
    );
}
