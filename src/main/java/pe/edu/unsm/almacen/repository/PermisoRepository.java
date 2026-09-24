package pe.edu.unsm.almacen.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.unsm.almacen.entity.Permiso;

public interface PermisoRepository extends JpaRepository<Permiso, Integer> {

    @Query("SELECT p FROM Permiso p JOIN FETCH p.modulo m WHERE p.perfil.idPerfil = :idPerfil AND (:estado IS NULL OR p.estadoPermiso = :estado) ORDER BY m.orden ASC")
    List<Permiso> findByPerfil_IdAndEstado(@Param("idPerfil") Integer idPerfil, @Param("estado") Integer estado);

    @Query("SELECT p FROM Permiso p JOIN FETCH p.modulo m WHERE p.perfil.idPerfil = :idPerfil ORDER BY m.orden ASC")
    List<Permiso> findByPerfil_Id(@Param("idPerfil") Integer idPerfil);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Permiso p JOIN p.modulo m "
            + "WHERE p.perfil.idPerfil = :idPerfil AND m.codigo = :codigo "
            + "AND p.estadoPermiso = 1 AND m.estado = 1")
    boolean existsActiveByPerfilAndCodigo(@Param("idPerfil") Integer idPerfil, @Param("codigo") String codigo);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM Permiso p WHERE p.perfil.idPerfil = :idPerfil")
    void deleteByPerfil_Id(@Param("idPerfil") Integer idPerfil);
}
