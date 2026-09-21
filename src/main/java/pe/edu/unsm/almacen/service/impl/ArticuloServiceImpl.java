package pe.edu.unsm.almacen.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.ArticuloCreateRequest;
import pe.edu.unsm.almacen.dto.request.ArticuloUpdateRequest;
import pe.edu.unsm.almacen.dto.response.ArticuloResponse;
import pe.edu.unsm.almacen.dto.response.ArticuloResumenResponse;
import pe.edu.unsm.almacen.entity.Articulo;
import pe.edu.unsm.almacen.entity.Familia;
import pe.edu.unsm.almacen.entity.Marca;
import pe.edu.unsm.almacen.entity.Ubicacion;
import pe.edu.unsm.almacen.exception.DuplicateResourceException;
import pe.edu.unsm.almacen.exception.ResourceNotFoundException;
import pe.edu.unsm.almacen.repository.ArticuloRepository;
import pe.edu.unsm.almacen.repository.FamiliaRepository;
import pe.edu.unsm.almacen.repository.MarcaRepository;
import pe.edu.unsm.almacen.repository.UbicacionRepository;
import pe.edu.unsm.almacen.service.IArticuloService;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticuloServiceImpl implements IArticuloService {

    private final ArticuloRepository articuloRepository;
    private final FamiliaRepository familiaRepository;
    private final MarcaRepository marcaRepository;
    private final UbicacionRepository ubicacionRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ArticuloResponse> listar(
            String filtro,
            String codigo,
            String descripcion,
            Integer idFamilia,
            String estado,
            Pageable pageable) {
        Page<Articulo> page = articuloRepository.listarPaginado(filtro, codigo, descripcion, idFamilia, estado, pageable);
        return PageResponse.of(page.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloResumenResponse> buscarPredictivo(String termino) {
        List<Articulo> articulos = articuloRepository.buscarPredictivo(termino, PageRequest.of(0, 20));
        return articulos.stream()
                .map(this::mapToResumenResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ArticuloResponse obtenerPorId(Integer id) {
        return articuloRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Artículo no encontrado con ID: " + id));
    }

    @Override
    @Transactional
    public ArticuloResponse crear(ArticuloCreateRequest request) {
        String codigoLimpio = request.codigo().trim().toUpperCase();

        if (articuloRepository.existsByCodigo(codigoLimpio)) {
            throw new DuplicateResourceException("Ya existe un artículo registrado con el código: " + codigoLimpio);
        }

        Familia familia = familiaRepository.findById(request.idFamilia())
                .orElseThrow(() -> new ResourceNotFoundException("Familia no encontrada con ID: " + request.idFamilia()));

        Marca marca = marcaRepository.findById(request.idMarca())
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada con ID: " + request.idMarca()));

        Ubicacion ubicacion = ubicacionRepository.findById(request.idUbicacion())
                .orElseThrow(() -> new ResourceNotFoundException("Ubicación no encontrada con ID: " + request.idUbicacion()));

        Articulo articulo = Articulo.builder()
                .codigo(codigoLimpio)
                .descripcion(request.descripcion().trim())
                .familia(familia)
                .marca(marca)
                .ubicacion(ubicacion)
                .saldo(BigDecimal.ZERO)
                .cantidadMinima(request.cantidadMinima())
                .precio(request.precio())
                .activo(true)
                .estado("1")
                .detalle(request.detalle() != null ? request.detalle().trim() : "")
                .fecha(LocalDateTime.now())
                .build();

        Articulo guardado = articuloRepository.save(articulo);
        log.info("Artículo creado con éxito: ID={}, Código={}", guardado.getId(), guardado.getCodigo());

        return mapToResponse(guardado);
    }

    @Override
    @Transactional
    public ArticuloResponse actualizar(Integer id, ArticuloUpdateRequest request) {
        Articulo articulo = articuloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artículo no encontrado con ID: " + id));

        Familia familia = familiaRepository.findById(request.idFamilia())
                .orElseThrow(() -> new ResourceNotFoundException("Familia no encontrada con ID: " + request.idFamilia()));

        Marca marca = marcaRepository.findById(request.idMarca())
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada con ID: " + request.idMarca()));

        Ubicacion ubicacion = ubicacionRepository.findById(request.idUbicacion())
                .orElseThrow(() -> new ResourceNotFoundException("Ubicación no encontrada con ID: " + request.idUbicacion()));

        articulo.setDescripcion(request.descripcion().trim());
        articulo.setFamilia(familia);
        articulo.setMarca(marca);
        articulo.setUbicacion(ubicacion);
        articulo.setCantidadMinima(request.cantidadMinima());
        articulo.setPrecio(request.precio());
        articulo.setDetalle(request.detalle() != null ? request.detalle().trim() : "");

        Articulo actualizado = articuloRepository.save(articulo);
        log.info("Artículo actualizado con éxito: ID={}", actualizado.getId());

        return mapToResponse(actualizado);
    }

    @Override
    @Transactional
    public void cambiarEstado(Integer id, String nuevoEstado) {
        Articulo articulo = articuloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artículo no encontrado con ID: " + id));

        articulo.setEstado(nuevoEstado);
        articuloRepository.save(articulo);
        log.info("Estado de artículo ID={} cambiado a '{}'", id, nuevoEstado);
    }

    @Override
    @Transactional
    public ArticuloResponse toggleActivo(Integer id) {
        Articulo articulo = articuloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artículo no encontrado con ID: " + id));

        boolean nuevoActivo = !Boolean.TRUE.equals(articulo.getActivo());
        articulo.setActivo(nuevoActivo);
        Articulo actualizado = articuloRepository.save(articulo);
        log.info("Operatividad de artículo ID={} modificada a activo={}", id, nuevoActivo);

        return mapToResponse(actualizado);
    }

    private ArticuloResponse mapToResponse(Articulo a) {
        return new ArticuloResponse(
                a.getId(),
                a.getCodigo(),
                a.getDescripcion(),
                a.getFamilia() != null ? a.getFamilia().getId() : null,
                a.getFamilia() != null ? a.getFamilia().getNombre() : null,
                a.getMarca() != null ? a.getMarca().getId() : null,
                a.getMarca() != null ? a.getMarca().getNombre() : null,
                a.getUbicacion() != null ? a.getUbicacion().getId() : null,
                a.getUbicacion() != null ? a.getUbicacion().getNombre() : null,
                a.getSaldo(),
                a.getCantidadMinima(),
                a.getPrecio(),
                a.getActivo(),
                a.getEstado(),
                a.getDetalle(),
                a.getFecha()
        );
    }

    private ArticuloResumenResponse mapToResumenResponse(Articulo a) {
        return new ArticuloResumenResponse(
                a.getId(),
                a.getCodigo(),
                a.getDescripcion(),
                a.getSaldo(),
                a.getPrecio(),
                a.getFamilia() != null ? a.getFamilia().getNombre() : null,
                a.getMarca() != null ? a.getMarca().getNombre() : null,
                a.getUbicacion() != null ? a.getUbicacion().getNombre() : null,
                a.getActivo()
        );
    }
}
