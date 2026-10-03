package pe.edu.unsm.almacen.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.unsm.almacen.entity.Marca;

public interface MarcaRepository extends JpaRepository<Marca, Integer> {

    List<Marca> findByEstadoOrderByNombreAsc(String estado);

    @Query("SELECT m FROM Marca m WHERE (:filtro IS NULL OR :filtro = '' OR LOWER(m.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')))")
    Page<Marca> buscar(@Param("filtro") String filtro, Pageable pageable);
}
