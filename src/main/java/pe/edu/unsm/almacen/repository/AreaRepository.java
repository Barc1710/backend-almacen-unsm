package pe.edu.unsm.almacen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.unsm.almacen.entity.Area;

public interface AreaRepository extends JpaRepository<Area, Integer> {
}
