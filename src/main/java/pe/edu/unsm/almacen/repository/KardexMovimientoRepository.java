package pe.edu.unsm.almacen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.unsm.almacen.entity.KardexMovimiento;

public interface KardexMovimientoRepository extends JpaRepository<KardexMovimiento, Long> {
}
