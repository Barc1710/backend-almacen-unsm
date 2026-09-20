package pe.edu.unsm.almacen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.unsm.almacen.entity.Ingreso;

public interface IngresoRepository extends JpaRepository<Ingreso, Integer> {
}
