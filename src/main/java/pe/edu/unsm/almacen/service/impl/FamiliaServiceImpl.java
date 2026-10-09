package pe.edu.unsm.almacen.service.impl;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.FamiliaRequest;
import pe.edu.unsm.almacen.dto.response.FamiliaResponse;
import pe.edu.unsm.almacen.entity.Familia;
import pe.edu.unsm.almacen.exception.BusinessException;
import pe.edu.unsm.almacen.exception.DuplicateResourceException;
import pe.edu.unsm.almacen.exception.ResourceNotFoundException;
import pe.edu.unsm.almacen.repository.FamiliaRepository;
import pe.edu.unsm.almacen.service.IFamiliaService;

@Service
@RequiredArgsConstructor
public class FamiliaServiceImpl implements IFamiliaService {

    private final FamiliaRepository familiaRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FamiliaResponse> listarPaginado(String filtro, Pageable pageable) {
        Page<Familia> page = familiaRepository.buscar(filtro, pageable);
        return PageResponse.of(page.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FamiliaResponse> listarActivos() {
        return familiaRepository.findByEstadoOrderByNombreAsc("1")
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FamiliaResponse obtenerPorId(Integer id) {
        return familiaRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Familia no encontrada con id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public String sugerirInicial(String nombre) {
        Set<String> inicialesOcupadas = familiaRepository.findAllInicialesActivas();

        String limpio = "";
        if (nombre != null && !nombre.isBlank()) {
            limpio = Normalizer.normalize(nombre.trim(), Normalizer.Form.NFD)
                    .replaceAll("[^\\p{ASCII}]", "")
                    .replaceAll("[^a-zA-Z]", "")
                    .toUpperCase(Locale.ROOT);
        }

        if (!limpio.isEmpty()) {
            // Probar la primera letra (ej. 'C')
            String unaLetra = limpio.substring(0, 1);
            if (!inicialesOcupadas.contains(unaLetra)) {
                return unaLetra;
            }

            // Probar las 2 primeras letras (ej. 'CO')
            if (limpio.length() >= 2) {
                String dosLetras = limpio.substring(0, 2);
                if (!inicialesOcupadas.contains(dosLetras)) {
                    return dosLetras;
                }
            }

            // Probar 1ra letra + siguiente letra de la palabra
            char primera = unaLetra.charAt(0);
            for (int i = 2; i < limpio.length(); i++) {
                String candidata = "" + primera + limpio.charAt(i);
                if (!inicialesOcupadas.contains(candidata)) {
                    return candidata;
                }
            }

            // Probar 1ra letra + 'A'..'Z'
            for (char c = 'A'; c <= 'Z'; c++) {
                String candidata = "" + primera + c;
                if (!inicialesOcupadas.contains(candidata)) {
                    return candidata;
                }
            }
        }

        // Probar letras individuales 'A'..'Z'
        for (char c = 'A'; c <= 'Z'; c++) {
            String candidata = String.valueOf(c);
            if (!inicialesOcupadas.contains(candidata)) {
                return candidata;
            }
        }

        // Probar combinaciones AA..ZZ
        for (char c1 = 'A'; c1 <= 'Z'; c1++) {
            for (char c2 = 'A'; c2 <= 'Z'; c2++) {
                String candidata = "" + c1 + c2;
                if (!inicialesOcupadas.contains(candidata)) {
                    return candidata;
                }
            }
        }

        throw new BusinessException("No hay iniciales disponibles en el sistema.");
    }

    @Override
    @Transactional
    public FamiliaResponse crear(FamiliaRequest request) {
        String nombreLimpio = request.nombre().trim();

        // 1. Validar si ya existe activa con el mismo nombre
        List<Familia> existentesPorNombre = familiaRepository.findByNombreIgnoreCase(nombreLimpio);
        boolean nombreActivoExiste = existentesPorNombre.stream()
                .anyMatch(f -> "1".equals(f.getEstado()));
        if (nombreActivoExiste) {
            throw new DuplicateResourceException("El nombre ya está registrado.");
        }

        // 2. Reactivación si existe previamente dada de baja
        Optional<Familia> inactivaPorNombre = existentesPorNombre.stream()
                .filter(f -> "0".equals(f.getEstado()))
                .findFirst();

        if (inactivaPorNombre.isPresent()) {
            Familia aReactivar = inactivaPorNombre.get();
            String inicialFinal;

            if (request.inicial() != null && !request.inicial().isBlank()) {
                inicialFinal = request.inicial().trim().toUpperCase(Locale.ROOT);
                if (!inicialFinal.equalsIgnoreCase(aReactivar.getInicial())) {
                    validarFormatoInicialNueva(inicialFinal);
                }
                if (familiaRepository.existsByInicialIgnoreCaseAndIdNotAndEstado(inicialFinal, aReactivar.getId(), "1")) {
                    throw new DuplicateResourceException("La inicial ya está registrada.");
                }
            } else {
                if (familiaRepository.existsByInicialIgnoreCaseAndIdNotAndEstado(aReactivar.getInicial(), aReactivar.getId(), "1")) {
                    inicialFinal = sugerirInicial(nombreLimpio);
                } else {
                    inicialFinal = aReactivar.getInicial();
                }
            }

            aReactivar.setNombre(nombreLimpio);
            aReactivar.setInicial(inicialFinal);
            aReactivar.setEstado("1");
            return mapToResponse(familiaRepository.save(aReactivar));
        }

        // 3. Registro nuevo
        String inicialFinal;
        if (request.inicial() == null || request.inicial().isBlank()) {
            inicialFinal = sugerirInicial(nombreLimpio);
        } else {
            inicialFinal = request.inicial().trim().toUpperCase(Locale.ROOT);
            validarFormatoInicialNueva(inicialFinal);
        }

        List<Familia> existentesPorInicial = familiaRepository.findByInicialIgnoreCase(inicialFinal);
        boolean inicialActivaExiste = existentesPorInicial.stream()
                .anyMatch(f -> "1".equals(f.getEstado()));
        if (inicialActivaExiste) {
            throw new DuplicateResourceException("La inicial ya está registrada.");
        }

        Familia familia = Familia.builder()
                .nombre(nombreLimpio)
                .inicial(inicialFinal)
                .correlativo(1)
                .estado("1")
                .build();

        return mapToResponse(familiaRepository.save(familia));
    }

    @Override
    @Transactional
    public FamiliaResponse actualizar(Integer id, FamiliaRequest request) {
        Familia familia = familiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Familia no encontrada con id: " + id));

        String nombreLimpio = request.nombre().trim();
        String inicialFinal;

        if (request.inicial() == null || request.inicial().isBlank()) {
            inicialFinal = familia.getInicial();
        } else {
            inicialFinal = request.inicial().trim().toUpperCase(Locale.ROOT);
            if (!inicialFinal.equalsIgnoreCase(familia.getInicial())) {
                validarFormatoInicialNueva(inicialFinal);
            }
        }

        if (familiaRepository.existsByNombreIgnoreCaseAndIdNotAndEstado(nombreLimpio, id, "1")) {
            throw new DuplicateResourceException("El nombre ya está registrado.");
        }

        if (familiaRepository.existsByInicialIgnoreCaseAndIdNotAndEstado(inicialFinal, id, "1")) {
            throw new DuplicateResourceException("La inicial ya está registrada.");
        }

        familia.setNombre(nombreLimpio);
        familia.setInicial(inicialFinal);
        return mapToResponse(familiaRepository.save(familia));
    }

    private void validarFormatoInicialNueva(String inicial) {
        if (inicial.length() > 2) {
            throw new BusinessException("La inicial debe tener como máximo 2 letras.");
        }
        if (!inicial.matches("^[A-Z]{1,2}$")) {
            throw new BusinessException("La inicial debe contener solo letras (máx. 2).");
        }
    }

    @Override
    @Transactional
    public void cambiarEstado(Integer id, String nuevoEstado) {
        Familia familia = familiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Familia no encontrada con id: " + id));
        familia.setEstado(nuevoEstado);
        familiaRepository.save(familia);
    }

    private FamiliaResponse mapToResponse(Familia familia) {
        return new FamiliaResponse(
                familia.getId(),
                familia.getNombre(),
                familia.getInicial(),
                familia.getCorrelativo(),
                familia.getEstado()
        );
    }
}
