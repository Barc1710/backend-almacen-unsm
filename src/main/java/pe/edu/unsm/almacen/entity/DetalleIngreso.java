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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "detalle_ingreso")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleIngreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_ingreso", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_det_ingreso_cabecera"))
    private Ingreso ingreso;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_articulo", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_det_ingreso_articulo"))
    private Articulo articulo;

    @Column(name = "cantidad", nullable = false, precision = 12, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "precio", nullable = false, precision = 12, scale = 2)
    @ColumnDefault("0.00")
    private BigDecimal precio;

    @Column(name = "saldo", nullable = false, precision = 12, scale = 2)
    private BigDecimal saldo;

    @Column(name = "fecha", nullable = false, columnDefinition = "datetime")
    @ColumnDefault("CURRENT_TIMESTAMP")
    private LocalDateTime fecha;

    @Column(name = "tipo", nullable = false, length = 1, columnDefinition = "char(1)")
    @ColumnDefault("'i'")
    private String tipo;

    @PrePersist
    public void prePersist() {
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
        if (this.tipo == null || this.tipo.isBlank()) {
            this.tipo = "i";
        }
        if (this.precio == null) {
            this.precio = BigDecimal.ZERO;
        }
    }
}
