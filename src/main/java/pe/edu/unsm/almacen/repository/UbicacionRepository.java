package pe.edu.unsm.almacen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.unsm.almacen.entity.Ubicacion;

public interface UbicacionRepository extends JpaRepository<Ubicacion, Integer> {
}
