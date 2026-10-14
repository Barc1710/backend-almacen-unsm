package pe.edu.unsm.almacen.service.impl;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.ClienteRequest;
import pe.edu.unsm.almacen.dto.response.ClienteResponse;
import pe.edu.unsm.almacen.entity.Cliente;
import pe.edu.unsm.almacen.exception.DuplicateResourceException;
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
        String filtroLimpio = limpiar(filtro);
        Page<Cliente> page = clienteRepository.buscar(filtroLimpio, pageable);
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
        String dniLimpio = limpiar(request.dni());
        if (dniLimpio != null) {
            List<Cliente> existentes = clienteRepository.findByDni(dniLimpio);
            if (existentes.stream().anyMatch(c -> "1".equals(c.getEstado()))) {
                throw new DuplicateResourceException("Ya existe un cliente activo con el DNI: " + dniLimpio);
            }
            Optional<Cliente> inactivo = existentes.stream()
                    .filter(c -> "0".equals(c.getEstado()))
                    .findFirst();
            if (inactivo.isPresent()) {
                Cliente aReactivar = inactivo.get();
                actualizarDatos(aReactivar, dniLimpio, request);
                aReactivar.setEstado("1");
                return mapToResponse(clienteRepository.save(aReactivar));
            }
        }

        Cliente cliente = Cliente.builder()
                .estado("1")
                .build();
        actualizarDatos(cliente, dniLimpio, request);
        return mapToResponse(clienteRepository.save(cliente));
    }

    @Override
    @Transactional
    public ClienteResponse actualizar(Integer id, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));

        String dniLimpio = limpiar(request.dni());
        if (dniLimpio != null && clienteRepository.existsByDniAndIdNotAndEstado(dniLimpio, id, "1")) {
            throw new DuplicateResourceException("Ya existe otro cliente activo con el DNI: " + dniLimpio);
        }

        actualizarDatos(cliente, dniLimpio, request);
        return mapToResponse(clienteRepository.save(cliente));
    }

    private void actualizarDatos(Cliente cliente, String dni, ClienteRequest request) {
        cliente.setDni(dni);
        cliente.setNombre(request.nombre().trim());
        cliente.setTelefono(limpiar(request.telefono()));
        cliente.setCorreo(limpiar(request.correo()));
        cliente.setDireccion(limpiar(request.direccion()));
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
                cliente.getCorreo(),
                cliente.getDireccion(),
                cliente.getEstado()
        );
    }
}
