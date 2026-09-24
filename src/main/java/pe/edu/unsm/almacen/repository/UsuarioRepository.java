package pe.edu.unsm.almacen.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.unsm.almacen.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    @EntityGraph(attributePaths = {"perfil"})
    Optional<Usuario> findByUsuario(String usuario);

    @EntityGraph(attributePaths = {"perfil"})
    Optional<Usuario> findByUsuarioAndEstado(String usuario, String estado);

    @EntityGraph(attributePaths = {"perfil"})
    @Query("SELECT u FROM Usuario u WHERE " +
           "(:filtro IS NULL OR :filtro = '' OR " +
           "LOWER(u.usuario) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(u.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(u.apellido) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(COALESCE(u.dni, '')) LIKE LOWER(CONCAT('%', :filtro, '%'))) AND " +
           "(:idPerfil IS NULL OR u.perfil.idPerfil = :idPerfil) AND " +
           "(:estado IS NULL OR :estado = '' OR u.estado = :estado)")
    Page<Usuario> buscar(
            @Param("filtro") String filtro,
            @Param("idPerfil") Integer idPerfil,
            @Param("estado") String estado,
            Pageable pageable
    );
}
