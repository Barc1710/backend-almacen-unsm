package pe.edu.unsm.almacen.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.ClienteRequest;
import pe.edu.unsm.almacen.dto.response.ClienteResponse;
import pe.edu.unsm.almacen.entity.Cliente;
import pe.edu.unsm.almacen.exception.ResourceNotFoundException;
import pe.edu.unsm.almacen.repository.ClienteRepository;
import pe.edu.unsm.almacen.service.IClienteService;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements IClienteService {

    private final ClienteRepository clienteRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClienteResponse> listarPaginado(String filtro, Pageable pageable) {
        Page<Cliente> page = clienteRepository.buscar(filtro, pageable);
        return PageResponse.of(page.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> listarActivos() {
        return clienteRepository.findByEstadoOrderByNombreAsc("1")
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse obtenerPorId(Integer id) {
        return clienteRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));
    }

    @Override
    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        Cliente cliente = Cliente.builder()
                .nombre(request.nombre().trim())
                .dni(request.dni() != null ? request.dni().trim() : null)
                .telefono(request.telefono() != null ? request.telefono().trim() : null)
                .celular(request.celular() != null ? request.celular().trim() : null)
                .correo(request.correo() != null ? request.correo().trim() : null)
                .direccion(request.direccion() != null ? request.direccion().trim() : null)
                .estado("1")
                .build();
        return mapToResponse(clienteRepository.save(cliente));
    }

    @Override
    @Transactional
    public ClienteResponse actualizar(Integer id, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));

        cliente.setNombre(request.nombre().trim());
        cliente.setDni(request.dni() != null ? request.dni().trim() : null);
        cliente.setTelefono(request.telefono() != null ? request.telefono().trim() : null);
        cliente.setCelular(request.celular() != null ? request.celular().trim() : null);
        cliente.setCorreo(request.correo() != null ? request.correo().trim() : null);
        cliente.setDireccion(request.direccion() != null ? request.direccion().trim() : null);

        return mapToResponse(clienteRepository.save(cliente));
    }

    @Override
    @Transactional
    public void cambiarEstado(Integer id, String nuevoEstado) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));
        cliente.setEstado(nuevoEstado);
        clienteRepository.save(cliente);
    }

    private ClienteResponse mapToResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getDni(),
                cliente.getTelefono(),
                cliente.getCelular(),
                cliente.getCorreo(),
                cliente.getDireccion(),
                cliente.getEstado()
        );
    }
}
