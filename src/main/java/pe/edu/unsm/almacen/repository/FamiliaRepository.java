package pe.edu.unsm.almacen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.unsm.almacen.entity.Familia;

public interface FamiliaRepository extends JpaRepository<Familia, Integer> {
}
