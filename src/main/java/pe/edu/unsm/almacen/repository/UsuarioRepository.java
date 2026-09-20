package pe.edu.unsm.almacen.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.unsm.almacen.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    @EntityGraph(attributePaths = {"perfil"})
    Optional<Usuario> findByUsuario(String usuario);

    boolean existsByUsuario(String usuario);
}
