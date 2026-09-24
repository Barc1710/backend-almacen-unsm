package pe.edu.unsm.almacen.repository;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.unsm.almacen.entity.Articulo;
import pe.edu.unsm.almacen.entity.Familia;
import pe.edu.unsm.almacen.entity.Marca;
import pe.edu.unsm.almacen.entity.Ubicacion;
import pe.edu.unsm.almacen.entity.UnidadMedida;

public interface ArticuloRepository extends JpaRepository<Articulo, Integer> {

    @EntityGraph(attributePaths = {"familia", "marca", "ubicacion", "unidadMedida"})
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

    @EntityGraph(attributePaths = {"familia", "marca", "ubicacion", "unidadMedida"})
    @Query("SELECT a FROM Articulo a WHERE a.estado = '1' AND a.activo = true AND " +
           "(:soloConStock = false OR a.saldo > 0) AND " +
           "(:termino IS NULL OR :termino = '' OR LOWER(a.codigo) LIKE LOWER(CONCAT('%', :termino, '%')) OR LOWER(a.descripcion) LIKE LOWER(CONCAT('%', :termino, '%'))) " +
           "ORDER BY a.descripcion ASC")
    List<Articulo> buscarPredictivo(
            @Param("termino") String termino,
            @Param("soloConStock") boolean soloConStock,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"familia", "marca", "ubicacion", "unidadMedida"})
    @Override
    Optional<Articulo> findById(Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"familia", "marca", "ubicacion", "unidadMedida"})
    @Query("SELECT a FROM Articulo a WHERE a.id = :id")
    Optional<Articulo> findByIdWithLock(@Param("id") Integer id);

    boolean existsByCodigo(String codigo);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Articulo a SET a.descripcion = :desc, a.unidadMedida = :unidadMedida, a.familia = :familia, a.marca = :marca, a.ubicacion = :ubicacion, " +
           "a.cantidadMinima = :min, a.precio = :precio, a.detalle = :det WHERE a.id = :id")
    int actualizarDatosMaestros(
            @Param("id") Integer id,
            @Param("desc") String desc,
            @Param("unidadMedida") UnidadMedida unidadMedida,
            @Param("familia") Familia familia,
            @Param("marca") Marca marca,
            @Param("ubicacion") Ubicacion ubicacion,
            @Param("min") BigDecimal min,
            @Param("precio") BigDecimal precio,
            @Param("det") String det
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Articulo a SET a.estado = :estado WHERE a.id = :id")
    int actualizarEstado(@Param("id") Integer id, @Param("estado") String estado);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Articulo a SET a.activo = :activo WHERE a.id = :id")
    int actualizarActivo(@Param("id") Integer id, @Param("activo") Boolean activo);
}
