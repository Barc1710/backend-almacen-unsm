package pe.edu.unsm.almacen.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.unsm.almacen.entity.CorrelativoFamilia;

public interface CorrelativoFamiliaRepository extends JpaRepository<CorrelativoFamilia, Integer> {

    Optional<CorrelativoFamilia> findByFamilia_Id(Integer idFamilia);
}
