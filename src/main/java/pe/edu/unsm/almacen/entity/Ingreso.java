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
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.time.Year;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "ingreso",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_ingreso_prefijo_correlativo", columnNames = {"prefijo", "correlativo"}),
                @UniqueConstraint(name = "uq_ingreso_orden_compra", columnNames = {"numero_orden_compra"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proveedor", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ingreso_proveedor"))
    private Proveedor proveedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario",
            foreignKey = @ForeignKey(name = "fk_ingreso_usuario"))
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_encargado_almacen", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_ingreso_encargado_almacen"))
    private EncargadoAlmacen encargadoAlmacen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_encargado", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_ingreso_encargado"))
    private Encargado jefe;

    @Transient
    private String nombreEncargadoAlmacen;

    @Transient
    private String nombreJefe;

    @Column(name = "prefijo", nullable = false, length = 10)
    @ColumnDefault("''")
    private String prefijo;

    @Column(name = "correlativo", nullable = false)
    @ColumnDefault("0")
    private Integer correlativo;

    @Column(name = "numero_orden_compra", nullable = true, length = 50)
    private String numeroOrdenCompra;

    @Column(name = "descripcion", nullable = true, length = 255)
    private String descripcion;

    @Column(name = "fecha", nullable = false, columnDefinition = "datetime")
    @ColumnDefault("CURRENT_TIMESTAMP")
    private LocalDateTime fecha;

    @Column(name = "estado", nullable = false, length = 1, columnDefinition = "char(1)")
    @ColumnDefault("'1'")
    private String estado;

    @PrePersist
    public void prePersist() {
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
        if (this.estado == null || this.estado.isBlank()) {
            this.estado = "1";
        }
        if (this.prefijo == null) {
            this.prefijo = "";
        }
        if (this.correlativo == null) {
            this.correlativo = 0;
        }
    }

    public String getNumeroCompleto() {
        String p = (prefijo != null && !prefijo.isBlank())
                ? prefijo
                : "I" + String.format("%02d", Year.now().getValue() % 100);
        int c = correlativo != null ? correlativo : 0;
        return String.format("%s-%04d", p, c);
    }

    public String getNombreEncargadoAlmacen() {
        if (this.nombreEncargadoAlmacen != null && !this.nombreEncargadoAlmacen.isBlank()) {
            return this.nombreEncargadoAlmacen;
        }
        return this.encargadoAlmacen != null ? this.encargadoAlmacen.getNombre() : null;
    }

    public String getNombreJefe() {
        if (this.nombreJefe != null && !this.nombreJefe.isBlank()) {
            return this.nombreJefe;
        }
        return this.jefe != null ? this.jefe.getNombreCompleto() : null;
    }

    public Encargado getEncargado() {
        return this.jefe;
    }

    public void setEncargado(Encargado encargado) {
        this.jefe = encargado;
    }
}
