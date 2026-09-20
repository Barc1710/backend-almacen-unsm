package pe.edu.unsm.almacen.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.ProveedorRequest;
import pe.edu.unsm.almacen.dto.response.ProveedorResponse;
import pe.edu.unsm.almacen.entity.Proveedor;
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
        Page<Proveedor> page = proveedorRepository.buscar(filtro, pageable);
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
        Proveedor proveedor = Proveedor.builder()
                .ruc(request.ruc() != null ? request.ruc().trim() : null)
                .razonSocial(request.razonSocial().trim())
                .telefono(request.telefono() != null ? request.telefono().trim() : null)
                .celular(request.celular() != null ? request.celular().trim() : null)
                .correo(request.correo() != null ? request.correo().trim() : null)
                .direccion(request.direccion() != null ? request.direccion().trim() : null)
                .contacto(request.contacto() != null ? request.contacto().trim() : null)
                .banco(request.banco() != null ? request.banco().trim() : null)
                .cuentaCorriente(request.cuentaCorriente() != null ? request.cuentaCorriente().trim() : null)
                .estado("1")
                .build();
        return mapToResponse(proveedorRepository.save(proveedor));
    }

    @Override
    @Transactional
    public ProveedorResponse actualizar(Integer id, ProveedorRequest request) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con id: " + id));

        proveedor.setRuc(request.ruc() != null ? request.ruc().trim() : null);
        proveedor.setRazonSocial(request.razonSocial().trim());
        proveedor.setTelefono(request.telefono() != null ? request.telefono().trim() : null);
        proveedor.setCelular(request.celular() != null ? request.celular().trim() : null);
        proveedor.setCorreo(request.correo() != null ? request.correo().trim() : null);
        proveedor.setDireccion(request.direccion() != null ? request.direccion().trim() : null);
        proveedor.setContacto(request.contacto() != null ? request.contacto().trim() : null);
        proveedor.setBanco(request.banco() != null ? request.banco().trim() : null);
        proveedor.setCuentaCorriente(request.cuentaCorriente() != null ? request.cuentaCorriente().trim() : null);

        return mapToResponse(proveedorRepository.save(proveedor));
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
                proveedor.getCelular(),
                proveedor.getCorreo(),
                proveedor.getDireccion(),
                proveedor.getContacto(),
                proveedor.getBanco(),
                proveedor.getCuentaCorriente(),
                proveedor.getEstado()
        );
    }
}
