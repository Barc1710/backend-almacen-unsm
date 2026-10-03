package pe.edu.unsm.almacen.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.unsm.almacen.entity.Familia;

public interface FamiliaRepository extends JpaRepository<Familia, Integer> {

    List<Familia> findByEstadoOrderByNombreAsc(String estado);

    @Query("SELECT f FROM Familia f WHERE (:filtro IS NULL OR :filtro = '' OR " +
           "LOWER(f.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(f.inicial) LIKE LOWER(CONCAT('%', :filtro, '%')))")
    Page<Familia> buscar(@Param("filtro") String filtro, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Familia f WHERE f.id = :id")
    Optional<Familia> findByIdWithLock(@Param("id") Integer id);
}
