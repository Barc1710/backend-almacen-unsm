package pe.edu.unsm.almacen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.unsm.almacen.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
}
