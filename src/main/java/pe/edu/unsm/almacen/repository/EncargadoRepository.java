package pe.edu.unsm.almacen.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.unsm.almacen.entity.Encargado;

public interface EncargadoRepository extends JpaRepository<Encargado, Integer> {

    List<Encargado> findByEstadoOrderByApellidosAsc(String estado);

    @Query("SELECT e FROM Encargado e WHERE (:filtro IS NULL OR :filtro = '' OR " +
           "LOWER(e.nombres) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.apellidos) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(COALESCE(e.dni, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(COALESCE(e.ambiente, '')) LIKE LOWER(CONCAT('%', :filtro, '%')))")
    Page<Encargado> buscar(@Param("filtro") String filtro, Pageable pageable);
}
