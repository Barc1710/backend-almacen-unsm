package pe.edu.unsm.almacen.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.unsm.almacen.entity.Perfil;

public interface PerfilRepository extends JpaRepository<Perfil, Integer> {

    @Query("SELECT p FROM Perfil p WHERE (:estado IS NULL OR p.estadoPerfil = :estado) ORDER BY p.nombrePerfil ASC")
    List<Perfil> findByEstadoOrderByNombreAsc(@Param("estado") Integer estado);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Perfil p WHERE LOWER(p.nombrePerfil) = LOWER(:nombre)")
    boolean existsByNombre(@Param("nombre") String nombre);
}
