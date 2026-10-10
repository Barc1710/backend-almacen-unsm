package pe.edu.unsm.almacen.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.response.KardexMovimientoResponse;
import pe.edu.unsm.almacen.entity.Egreso;
import pe.edu.unsm.almacen.entity.Ingreso;
import pe.edu.unsm.almacen.entity.KardexMovimiento;
import pe.edu.unsm.almacen.entity.TipoMovimiento;
import pe.edu.unsm.almacen.exception.ResourceNotFoundException;
import pe.edu.unsm.almacen.repository.ArticuloRepository;
import pe.edu.unsm.almacen.repository.EgresoRepository;
import pe.edu.unsm.almacen.repository.IngresoRepository;
import pe.edu.unsm.almacen.repository.KardexMovimientoRepository;
import pe.edu.unsm.almacen.service.IKardexService;

@Service
@RequiredArgsConstructor
public class KardexServiceImpl implements IKardexService {

    private final KardexMovimientoRepository kardexMovimientoRepository;
    private final ArticuloRepository articuloRepository;
    private final EgresoRepository egresoRepository;
    private final IngresoRepository ingresoRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<KardexMovimientoResponse> listarPorArticulo(
            Integer idArticulo,
            LocalDate desde,
            LocalDate hasta,
            TipoMovimiento tipoMovimiento,
            Pageable pageable) {
        if (!articuloRepository.existsById(idArticulo)) {
            throw new ResourceNotFoundException("Artículo no encontrado con ID: " + idArticulo);
        }

        LocalDateTime desdeDateTime = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime hastaDateTime = hasta != null ? hasta.atTime(23, 59, 59) : null;

        Pageable pageableAjustado = pageable;
        if (pageable.getSort().isUnsorted()) {
            pageableAjustado = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, KardexMovimiento::getFechaHora, KardexMovimiento::getId)
            );
        } else {
            pageableAjustado = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    pageable.getSort().and(Sort.by(Sort.Direction.DESC, KardexMovimiento::getId))
            );
        }

        Page<KardexMovimiento> page = kardexMovimientoRepository.filtrarMovimientos(
                idArticulo,
                tipoMovimiento,
                desdeDateTime,
                hastaDateTime,
                pageableAjustado
        );

        Map<Integer, Egreso> egresosMap = precargarEgresos(page.getContent());
        Map<Integer, Ingreso> ingresosMap = precargarIngresos(page.getContent());
        return PageResponse.of(page.map(k -> construirKardexResponse(k, egresosMap, ingresosMap)));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<KardexMovimientoResponse> listarGeneral(
            LocalDate desde,
            LocalDate hasta,
            TipoMovimiento tipoMovimiento,
            Pageable pageable) {
        LocalDateTime desdeDateTime = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime hastaDateTime = hasta != null ? hasta.atTime(23, 59, 59) : null;

        Pageable pageableAjustado = pageable;
        if (pageable.getSort().isUnsorted()) {
            pageableAjustado = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, KardexMovimiento::getFechaHora, KardexMovimiento::getId)
            );
        }

        Page<KardexMovimiento> page = kardexMovimientoRepository.filtrarMovimientos(
                null,
                tipoMovimiento,
                desdeDateTime,
                hastaDateTime,
                pageableAjustado
        );

        Map<Integer, Egreso> egresosMap = precargarEgresos(page.getContent());
        Map<Integer, Ingreso> ingresosMap = precargarIngresos(page.getContent());
        return PageResponse.of(page.map(k -> construirKardexResponse(k, egresosMap, ingresosMap)));
    }

    private Map<Integer, Egreso> precargarEgresos(List<KardexMovimiento> movimientos) {
        if (movimientos == null || movimientos.isEmpty()) {
            return Map.of();
        }
        Set<Integer> egresoIds = movimientos.stream()
                .filter(k -> (k.getTipoMovimiento() == TipoMovimiento.EGRESO
                        || k.getTipoMovimiento() == TipoMovimiento.REVERSO_EGRESO
                        || k.getTipoMovimiento() == TipoMovimiento.BAJA)
                        && k.getDocumentoId() != null)
                .map(KardexMovimiento::getDocumentoId)
                .collect(Collectors.toSet());

        if (egresoIds.isEmpty()) {
            return Map.of();
        }

        return egresoRepository.findAllById(egresoIds).stream()
                .collect(Collectors.toMap(Egreso::getId, Function.identity(), (e1, e2) -> e1));
    }

    private Map<Integer, Ingreso> precargarIngresos(List<KardexMovimiento> movimientos) {
        if (movimientos == null || movimientos.isEmpty()) {
            return Map.of();
        }
        Set<Integer> ingresoIds = movimientos.stream()
                .filter(k -> (k.getTipoMovimiento() == TipoMovimiento.INGRESO
                        || k.getTipoMovimiento() == TipoMovimiento.REVERSO_INGRESO)
                        && k.getDocumentoId() != null)
                .map(KardexMovimiento::getDocumentoId)
                .collect(Collectors.toSet());

        if (ingresoIds.isEmpty()) {
            return Map.of();
        }

        return ingresoRepository.findAllById(ingresoIds).stream()
                .collect(Collectors.toMap(Ingreso::getId, Function.identity(), (i1, i2) -> i1));
    }

    private KardexMovimientoResponse construirKardexResponse(
            KardexMovimiento k,
            Map<Integer, Egreso> egresosMap,
            Map<Integer, Ingreso> ingresosMap) {
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
            if (k.getTipoMovimiento() == TipoMovimiento.EGRESO
                    || k.getTipoMovimiento() == TipoMovimiento.REVERSO_EGRESO
                    || k.getTipoMovimiento() == TipoMovimiento.BAJA) {
                Egreso e = egresosMap != null ? egresosMap.get(k.getDocumentoId()) : null;
                if (e == null && egresoRepository != null) {
                    e = egresoRepository.findById(k.getDocumentoId()).orElse(null);
                }
                if (e != null) {
                    documentoReferencia = e.getNumeroCompleto();
                } else {
                    documentoReferencia = String.format("%s #%04d", k.getDocumentoTipo(), k.getDocumentoId());
                }
            } else if (k.getTipoMovimiento() == TipoMovimiento.INGRESO
                    || k.getTipoMovimiento() == TipoMovimiento.REVERSO_INGRESO) {
                Ingreso ing = ingresosMap != null ? ingresosMap.get(k.getDocumentoId()) : null;
                if (ing == null && ingresoRepository != null) {
                    ing = ingresoRepository.findById(k.getDocumentoId()).orElse(null);
                }
                if (ing != null) {
                    documentoReferencia = ing.getNumeroCompleto();
                } else {
                    documentoReferencia = String.format("%s #%04d", k.getDocumentoTipo() != null ? k.getDocumentoTipo() : "ING", k.getDocumentoId());
                }
            } else {
                documentoReferencia = String.format("%s #%04d", k.getDocumentoTipo(), k.getDocumentoId());
            }
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
