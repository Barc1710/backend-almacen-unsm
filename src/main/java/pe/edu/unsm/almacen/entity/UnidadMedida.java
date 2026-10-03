package pe.edu.unsm.almacen.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "unidad_medida",
        uniqueConstraints = @UniqueConstraint(name = "uq_unidad_medida_codigo", columnNames = {"codigo_sunat"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnidadMedida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "codigo_sunat", nullable = false, length = 10)
    private String codigoSunat;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "simbolo", nullable = false, length = 10)
    private String simbolo;

    @Column(name = "permite_decimales", nullable = false)
    @ColumnDefault("0")
    private Boolean permiteDecimales;

    @Column(name = "estado", nullable = false, length = 1, columnDefinition = "char(1)")
    @ColumnDefault("'1'")
    private String estado;

    @PrePersist
    public void prePersist() {
        if (this.permiteDecimales == null) {
            this.permiteDecimales = false;
        }
        if (this.estado == null || this.estado.isBlank()) {
            this.estado = "1";
        }
    }
}
