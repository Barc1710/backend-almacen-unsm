package pe.edu.unsm.almacen.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.unsm.almacen.entity.EncargadoAlmacen;

public interface EncargadoAlmacenRepository extends JpaRepository<EncargadoAlmacen, Integer> {

    List<EncargadoAlmacen> findByEstadoOrderByNombreAsc(String estado);

    Optional<EncargadoAlmacen> findFirstByEsTitularTrueAndEstado(String estado);

    @Query("SELECT e FROM EncargadoAlmacen e WHERE (:filtro IS NULL OR :filtro = '' OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')))")
    Page<EncargadoAlmacen> buscar(@Param("filtro") String filtro, Pageable pageable);
}
