package pe.edu.unsm.almacen.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.unsm.almacen.entity.Articulo;

public interface ArticuloRepository extends JpaRepository<Articulo, Integer> {

    @EntityGraph(attributePaths = {"familia", "marca", "ubicacion"})
    @Query(value = "SELECT a FROM Articulo a WHERE " +
           "(:filtro IS NULL OR :filtro = '' OR LOWER(a.codigo) LIKE LOWER(CONCAT('%', :filtro, '%')) OR LOWER(a.descripcion) LIKE LOWER(CONCAT('%', :filtro, '%'))) AND " +
           "(:codigo IS NULL OR :codigo = '' OR LOWER(a.codigo) LIKE LOWER(CONCAT('%', :codigo, '%'))) AND " +
           "(:descripcion IS NULL OR :descripcion = '' OR LOWER(a.descripcion) LIKE LOWER(CONCAT('%', :descripcion, '%'))) AND " +
           "(:idFamilia IS NULL OR a.familia.id = :idFamilia) AND " +
           "(:estado IS NULL OR :estado = '' OR a.estado = :estado)",
           countQuery = "SELECT COUNT(a) FROM Articulo a WHERE " +
           "(:filtro IS NULL OR :filtro = '' OR LOWER(a.codigo) LIKE LOWER(CONCAT('%', :filtro, '%')) OR LOWER(a.descripcion) LIKE LOWER(CONCAT('%', :filtro, '%'))) AND " +
           "(:codigo IS NULL OR :codigo = '' OR LOWER(a.codigo) LIKE LOWER(CONCAT('%', :codigo, '%'))) AND " +
           "(:descripcion IS NULL OR :descripcion = '' OR LOWER(a.descripcion) LIKE LOWER(CONCAT('%', :descripcion, '%'))) AND " +
           "(:idFamilia IS NULL OR a.familia.id = :idFamilia) AND " +
           "(:estado IS NULL OR :estado = '' OR a.estado = :estado)")
    Page<Articulo> listarPaginado(
            @Param("filtro") String filtro,
            @Param("codigo") String codigo,
            @Param("descripcion") String descripcion,
            @Param("idFamilia") Integer idFamilia,
            @Param("estado") String estado,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"familia", "marca", "ubicacion"})
    @Query("SELECT a FROM Articulo a WHERE a.estado = '1' AND a.activo = true AND " +
           "(:termino IS NULL OR :termino = '' OR LOWER(a.codigo) LIKE LOWER(CONCAT('%', :termino, '%')) OR LOWER(a.descripcion) LIKE LOWER(CONCAT('%', :termino, '%'))) " +
           "ORDER BY a.descripcion ASC")
    List<Articulo> buscarPredictivo(@Param("termino") String termino, Pageable pageable);

    @EntityGraph(attributePaths = {"familia", "marca", "ubicacion"})
    @Override
    Optional<Articulo> findById(Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Articulo a WHERE a.id = :id")
    Optional<Articulo> findByIdWithLock(@Param("id") Integer id);

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Integer id);
}
