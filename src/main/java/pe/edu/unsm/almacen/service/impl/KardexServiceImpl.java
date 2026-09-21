package pe.edu.unsm.almacen.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.response.KardexMovimientoResponse;
import pe.edu.unsm.almacen.entity.KardexMovimiento;
import pe.edu.unsm.almacen.exception.ResourceNotFoundException;
import pe.edu.unsm.almacen.repository.ArticuloRepository;
import pe.edu.unsm.almacen.repository.KardexMovimientoRepository;
import pe.edu.unsm.almacen.service.IKardexService;

@Service
@RequiredArgsConstructor
@Slf4j
public class KardexServiceImpl implements IKardexService {

    private final KardexMovimientoRepository kardexMovimientoRepository;
    private final ArticuloRepository articuloRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<KardexMovimientoResponse> listarPorArticulo(
            Integer idArticulo,
            LocalDate desde,
            LocalDate hasta,
            Pageable pageable) {
        log.info("Consultando kardex contable para artículo ID: {}", idArticulo);

        // 1. Validar existencia del artículo
        if (!articuloRepository.existsById(idArticulo)) {
            throw new ResourceNotFoundException("Artículo no encontrado con ID: " + idArticulo);
        }

        // 2. Normalización segura de fechas (evitando redondeo de nanosegundos en MySQL 8.4)
        LocalDateTime desdeDateTime = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime hastaDateTime = hasta != null ? hasta.atTime(23, 59, 59) : null;

        // 3. Diferenciación de ordenamiento: Lectura contable cronológica natural (ASC)
        Pageable pageableAjustado = pageable;
        if (pageable.getSort().isUnsorted()) {
            pageableAjustado = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.ASC, "fechaHora").and(Sort.by(Sort.Direction.ASC, "id"))
            );
        }

        Page<KardexMovimiento> page = kardexMovimientoRepository.filtrarMovimientos(
                idArticulo,
                desdeDateTime,
                hastaDateTime,
                pageableAjustado
        );

        return PageResponse.of(page.map(this::construirKardexResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<KardexMovimientoResponse> listarGeneral(
            LocalDate desde,
            LocalDate hasta,
            Pageable pageable) {
        log.info("Consultando historial general de auditoría de movimientos de kardex");

        // 1. Normalización segura de fechas
        LocalDateTime desdeDateTime = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime hastaDateTime = hasta != null ? hasta.atTime(23, 59, 59) : null;

        // 2. Diferenciación de ordenamiento: Auditoría más reciente primero (DESC)
        Pageable pageableAjustado = pageable;
        if (pageable.getSort().isUnsorted()) {
            pageableAjustado = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "fechaHora").and(Sort.by(Sort.Direction.DESC, "id"))
            );
        }

        Page<KardexMovimiento> page = kardexMovimientoRepository.filtrarMovimientos(
                null,
                desdeDateTime,
                hastaDateTime,
                pageableAjustado
        );

        return PageResponse.of(page.map(this::construirKardexResponse));
    }

    private KardexMovimientoResponse construirKardexResponse(KardexMovimiento k) {
        Integer idArticulo = k.getArticulo() != null ? k.getArticulo().getId() : null;
        String codigoArticulo = k.getArticulo() != null ? k.getArticulo().getCodigo() : null;
        String descripcionArticulo = k.getArticulo() != null ? k.getArticulo().getDescripcion() : null;

        Integer idUsuario = k.getUsuario() != null ? k.getUsuario().getIdUsuario() : null;
        String usuarioResponsable = "-";
        if (k.getUsuario() != null) {
            String nom = k.getUsuario().getNombre();
            String ape = k.getUsuario().getApellido();
            String completo = ((nom != null ? nom : "") + " " + (ape != null ? ape : "")).trim();
            usuarioResponsable = !completo.isEmpty() ? completo : k.getUsuario().getUsuario();
        }

        String documentoReferencia = "-";
        if (k.getDocumentoTipo() != null && k.getDocumentoId() != null) {
            documentoReferencia = String.format("%s #%06d", k.getDocumentoTipo(), k.getDocumentoId());
        } else if (k.getDocumentoTipo() != null) {
            documentoReferencia = k.getDocumentoTipo();
        }

        return new KardexMovimientoResponse(
                k.getId(),
                idArticulo,
                codigoArticulo,
                descripcionArticulo,
                k.getFechaHora(),
                k.getTipoMovimiento(),
                k.getDocumentoTipo(),
                k.getDocumentoId(),
                documentoReferencia,
                k.getCantidadEntrada(),
                k.getCantidadSalida(),
                k.getSaldoResultante(),
                idUsuario,
                usuarioResponsable
        );
    }
}
