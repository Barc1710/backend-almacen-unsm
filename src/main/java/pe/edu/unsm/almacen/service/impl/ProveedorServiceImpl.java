package pe.edu.unsm.almacen.service.impl;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.ProveedorRequest;
import pe.edu.unsm.almacen.dto.response.ProveedorResponse;
import pe.edu.unsm.almacen.entity.Proveedor;
import pe.edu.unsm.almacen.exception.DuplicateResourceException;
import pe.edu.unsm.almacen.exception.ResourceNotFoundException;
import pe.edu.unsm.almacen.repository.ProveedorRepository;
import pe.edu.unsm.almacen.service.IProveedorService;

@Service
@RequiredArgsConstructor
public class ProveedorServiceImpl implements IProveedorService {

    private final ProveedorRepository proveedorRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProveedorResponse> listarPaginado(String filtro, Pageable pageable) {
        String filtroLimpio = limpiar(filtro);
        Page<Proveedor> page = proveedorRepository.buscar(filtroLimpio, pageable);
        return PageResponse.of(page.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProveedorResponse> listarActivos() {
        return proveedorRepository.findByEstadoOrderByRazonSocialAsc("1")
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProveedorResponse obtenerPorId(Integer id) {
        return proveedorRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con id: " + id));
    }

    @Override
    @Transactional
    public ProveedorResponse crear(ProveedorRequest request) {
        String rucLimpio = limpiar(request.ruc());
        if (rucLimpio != null) {
            List<Proveedor> existentes = proveedorRepository.findByRuc(rucLimpio);
            if (existentes.stream().anyMatch(p -> "1".equals(p.getEstado()))) {
                throw new DuplicateResourceException("Ya existe un proveedor activo con el RUC: " + rucLimpio);
            }
            Optional<Proveedor> inactivo = existentes.stream()
                    .filter(p -> "0".equals(p.getEstado()))
                    .findFirst();
            if (inactivo.isPresent()) {
                Proveedor aReactivar = inactivo.get();
                actualizarDatos(aReactivar, rucLimpio, request);
                aReactivar.setEstado("1");
                return mapToResponse(proveedorRepository.save(aReactivar));
            }
        }

        Proveedor proveedor = Proveedor.builder()
                .estado("1")
                .build();
        actualizarDatos(proveedor, rucLimpio, request);
        return mapToResponse(proveedorRepository.save(proveedor));
    }

    @Override
    @Transactional
    public ProveedorResponse actualizar(Integer id, ProveedorRequest request) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con id: " + id));

        String rucLimpio = limpiar(request.ruc());
        if (rucLimpio != null && proveedorRepository.existsByRucAndIdNotAndEstado(rucLimpio, id, "1")) {
            throw new DuplicateResourceException("Ya existe otro proveedor activo con el RUC: " + rucLimpio);
        }

        actualizarDatos(proveedor, rucLimpio, request);
        return mapToResponse(proveedorRepository.save(proveedor));
    }

    private void actualizarDatos(Proveedor proveedor, String ruc, ProveedorRequest request) {
        proveedor.setRuc(ruc);
        proveedor.setRazonSocial(request.razonSocial().trim());
        proveedor.setTelefono(limpiar(request.telefono()));
        proveedor.setCorreo(limpiar(request.correo()));
        proveedor.setDireccion(limpiar(request.direccion()));
        proveedor.setContacto(limpiar(request.contacto()));
        proveedor.setBanco(limpiar(request.banco()));
        proveedor.setCuentaCorriente(limpiar(request.cuentaCorriente()));
    }

    private String limpiar(String val) {
        return val != null && !val.trim().isEmpty() ? val.trim() : null;
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        cambiarEstado(id, "0");
    }

    @Override
    @Transactional
    public void cambiarEstado(Integer id, String nuevoEstado) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con id: " + id));
        proveedor.setEstado(nuevoEstado);
        proveedorRepository.save(proveedor);
    }

    private ProveedorResponse mapToResponse(Proveedor proveedor) {
        return new ProveedorResponse(
                proveedor.getId(),
                proveedor.getRuc(),
                proveedor.getRazonSocial(),
                proveedor.getTelefono(),
                proveedor.getCorreo(),
                proveedor.getDireccion(),
                proveedor.getContacto(),
                proveedor.getBanco(),
                proveedor.getCuentaCorriente(),
                proveedor.getEstado()
        );
    }
}
