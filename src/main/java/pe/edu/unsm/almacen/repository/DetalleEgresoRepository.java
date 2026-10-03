package pe.edu.unsm.almacen.repository;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.unsm.almacen.entity.DetalleEgreso;

public interface DetalleEgresoRepository extends JpaRepository<DetalleEgreso, Integer> {

    @EntityGraph(attributePaths = {"articulo"})
    List<DetalleEgreso> findByEgreso_IdOrderByIdAsc(Integer idEgreso);
    // Mismo redondeo monetario por línea que en las respuestas detalladas.
    @Query("SELECT d.egreso.id, COALESCE(SUM(ROUND(d.cantidad * d.precio, 2)), 0.00) FROM DetalleEgreso d "
            + "WHERE d.egreso.id IN :ids GROUP BY d.egreso.id")
    List<Object[]> sumarTotalesPorEgresoIds(@Param("ids") List<Integer> ids);
}
