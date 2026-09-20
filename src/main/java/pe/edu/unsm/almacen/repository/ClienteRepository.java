package pe.edu.unsm.almacen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.unsm.almacen.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
}
