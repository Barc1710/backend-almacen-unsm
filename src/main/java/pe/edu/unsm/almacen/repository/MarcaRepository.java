package pe.edu.unsm.almacen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.unsm.almacen.entity.Marca;

public interface MarcaRepository extends JpaRepository<Marca, Integer> {
}
