package pe.edu.unsm.almacen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.unsm.almacen.entity.Perfil;

public interface PerfilRepository extends JpaRepository<Perfil, Integer> {
}
