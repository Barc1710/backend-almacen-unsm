package pe.edu.unsm.almacen.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.unsm.almacen.entity.Permiso;

public interface PermisoRepository extends JpaRepository<Permiso, Integer> {

    List<Permiso> findByPerfil_IdPerfil(Integer idPerfil);
}
