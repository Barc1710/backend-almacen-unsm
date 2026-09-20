package pe.edu.unsm.almacen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.unsm.almacen.entity.Egreso;

public interface EgresoRepository extends JpaRepository<Egreso, Integer> {
}
