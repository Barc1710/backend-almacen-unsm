package pe.edu.unsm.almacen.repository;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.unsm.almacen.entity.DetalleIngreso;

public interface DetalleIngresoRepository extends JpaRepository<DetalleIngreso, Integer> {

    @EntityGraph(attributePaths = {"articulo"})
    List<DetalleIngreso> findByIngreso_IdOrderByIdAsc(Integer idIngreso);
    // Mismo redondeo monetario por línea que en las respuestas detalladas.
    @Query("SELECT d.ingreso.id, COALESCE(SUM(ROUND(d.cantidad * d.precio, 2)), 0.00) FROM DetalleIngreso d "
            + "WHERE d.ingreso.id IN :ids GROUP BY d.ingreso.id")
    List<Object[]> sumarTotalesPorIngresoIds(@Param("ids") List<Integer> ids);
}
