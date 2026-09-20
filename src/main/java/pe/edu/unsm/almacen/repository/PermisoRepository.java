package pe.edu.unsm.almacen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.unsm.almacen.entity.Permiso;

public interface PermisoRepository extends JpaRepository<Permiso, Integer> {
}
