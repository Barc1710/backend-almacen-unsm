package pe.edu.unsm.almacen.repository;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.unsm.almacen.entity.DetalleIngreso;

public interface DetalleIngresoRepository extends JpaRepository<DetalleIngreso, Integer> {

    @EntityGraph(attributePaths = {"articulo"})
    List<DetalleIngreso> findByIngreso_IdOrderByIdAsc(Integer idIngreso);
}
