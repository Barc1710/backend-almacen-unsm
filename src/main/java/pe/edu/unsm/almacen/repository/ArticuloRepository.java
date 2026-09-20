package pe.edu.unsm.almacen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.unsm.almacen.entity.Articulo;

public interface ArticuloRepository extends JpaRepository<Articulo, Integer> {
}
