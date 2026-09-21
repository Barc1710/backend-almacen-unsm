package pe.edu.unsm.almacen.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "articulo",
        uniqueConstraints = @UniqueConstraint(name = "uq_articulo_codigo", columnNames = {"codigo"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Articulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "codigo", nullable = false, length = 20)
    private String codigo;

    @Column(name = "descripcion", nullable = false, length = 255)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_familia", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_articulo_familia"))
    private Familia familia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_marca", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_articulo_marca"))
    private Marca marca;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_ubicacion", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_articulo_ubicacion"))
    private Ubicacion ubicacion;

    @Column(name = "saldo", nullable = false, precision = 12, scale = 2)
    @ColumnDefault("0.00")
    private BigDecimal saldo;

    @Column(name = "cantidad_minima", nullable = false, precision = 12, scale = 2)
    @ColumnDefault("10.00")
    private BigDecimal cantidadMinima;

    @Column(name = "precio", nullable = false, precision = 12, scale = 2)
    @ColumnDefault("0.00")
    private BigDecimal precio;

    @Column(name = "activo", nullable = false, columnDefinition = "tinyint(1)")
    @ColumnDefault("1")
    private Boolean activo;

    @Column(name = "estado", nullable = false, length = 1, columnDefinition = "char(1)")
    @ColumnDefault("'1'")
    private String estado;

    @Column(name = "detalle", nullable = true, length = 255)
    @ColumnDefault("''")
    private String detalle;

    @Column(name = "fecha", nullable = true, columnDefinition = "datetime")
    @ColumnDefault("CURRENT_TIMESTAMP")
    private LocalDateTime fecha;

    @PrePersist
    public void prePersist() {
        if (this.saldo == null) {
            this.saldo = BigDecimal.ZERO;
        }
        if (this.estado == null || this.estado.isBlank()) {
            this.estado = "1";
        }
        if (this.activo == null) {
            this.activo = true;
        }
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
        if (this.detalle == null) {
            this.detalle = "";
        }
    }
}
