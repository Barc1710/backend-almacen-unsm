package pe.edu.unsm.almacen.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.unsm.almacen.entity.Modulo;

public interface ModuloRepository extends JpaRepository<Modulo, Integer> {

    @Query("SELECT m FROM Modulo m WHERE m.codigo IS NOT NULL AND (:estado IS NULL OR m.estado = :estado) ORDER BY m.orden ASC, m.nombre ASC")
    List<Modulo> findByEstadoOrderByOrdenAsc(@Param("estado") Integer estado);

    boolean existsByCodigoAndEstado(String codigo, Integer estado);
}
