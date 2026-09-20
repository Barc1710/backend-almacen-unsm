package pe.edu.unsm.almacen.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "kardex_movimiento",
        indexes = @Index(name = "idx_kardex_articulo_fecha", columnList = "id_articulo, fecha_hora"))
@Getter
@Setter
@NoArgsConstructor
public class KardexMovimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_articulo", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_kardex_articulo"))
    private Articulo articulo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, columnDefinition = "enum('SALDO_INICIAL', 'INGRESO', 'EGRESO', 'REVERSO_EGRESO', 'AJUSTE')")
    private TipoMovimiento tipoMovimiento;

    @Column(name = "documento_tipo", nullable = false, length = 20)
    private String documentoTipo;

    @Column(name = "documento_id", nullable = true)
    private Integer documentoId;

    @Column(name = "cantidad_entrada", nullable = false, precision = 12, scale = 2)
    @ColumnDefault("0.00")
    private BigDecimal cantidadEntrada;

    @Column(name = "cantidad_salida", nullable = false, precision = 12, scale = 2)
    @ColumnDefault("0.00")
    private BigDecimal cantidadSalida;

    @Column(name = "saldo_resultante", nullable = false, precision = 12, scale = 2)
    private BigDecimal saldoResultante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario", nullable = false,
            foreignKey = @ForeignKey(name = "fk_kardex_usuario"))
    private Usuario usuario;

    @Column(name = "fecha_hora", nullable = false, columnDefinition = "datetime")
    @ColumnDefault("CURRENT_TIMESTAMP")
    private LocalDateTime fechaHora;
}
