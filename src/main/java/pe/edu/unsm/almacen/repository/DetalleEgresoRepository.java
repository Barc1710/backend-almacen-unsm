package pe.edu.unsm.almacen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.unsm.almacen.entity.DetalleEgreso;

public interface DetalleEgresoRepository extends JpaRepository<DetalleEgreso, Integer> {
}
