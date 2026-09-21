package pe.edu.unsm.almacen.repository;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.unsm.almacen.entity.DetalleEgreso;

public interface DetalleEgresoRepository extends JpaRepository<DetalleEgreso, Integer> {

    @EntityGraph(attributePaths = {"articulo"})
    List<DetalleEgreso> findByEgreso_IdOrderByIdAsc(Integer idEgreso);
}
