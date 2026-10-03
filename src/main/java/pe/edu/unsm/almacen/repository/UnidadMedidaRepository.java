package pe.edu.unsm.almacen.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.unsm.almacen.entity.UnidadMedida;

public interface UnidadMedidaRepository extends JpaRepository<UnidadMedida, Integer> {

    List<UnidadMedida> findByEstadoOrderByNombreAsc(String estado);

    boolean existsByCodigoSunat(String codigoSunat);

    boolean existsByCodigoSunatAndIdNot(String codigoSunat, Integer id);

    @Query(value = """
        SELECT u FROM UnidadMedida u WHERE
        (:filtro IS NULL OR :filtro = '' OR
         LOWER(u.codigoSunat) LIKE LOWER(CONCAT('%', :filtro, '%')) OR
         LOWER(u.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) OR
         LOWER(u.simbolo) LIKE LOWER(CONCAT('%', :filtro, '%')))
        """,
        countQuery = """
        SELECT COUNT(u) FROM UnidadMedida u WHERE
        (:filtro IS NULL OR :filtro = '' OR
         LOWER(u.codigoSunat) LIKE LOWER(CONCAT('%', :filtro, '%')) OR
         LOWER(u.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) OR
         LOWER(u.simbolo) LIKE LOWER(CONCAT('%', :filtro, '%')))
        """)
    Page<UnidadMedida> buscar(@Param("filtro") String filtro, Pageable pageable);
}
