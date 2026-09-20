package pe.edu.unsm.almacen.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "egreso",
        indexes = @Index(name = "idx_egreso_numeracion", columnList = "prefijo, correlativo"))
@Getter
@Setter
@NoArgsConstructor
public class Egreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_egreso_cliente"))
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_encargado", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_egreso_encargado"))
    private Encargado encargado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_area", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_egreso_area"))
    private Area area;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_encargado_almacen", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_egreso_enc_almacen"))
    private EncargadoAlmacen encargadoAlmacen;

    @Column(name = "ambiente", nullable = true, length = 100)
    private String ambiente;

    @Column(name = "prefijo", nullable = false, length = 10)
    @ColumnDefault("''")
    private String prefijo;

    @Column(name = "correlativo", nullable = false)
    @ColumnDefault("0")
    private Integer correlativo;

    @Column(name = "fecha", nullable = false, columnDefinition = "datetime")
    @ColumnDefault("CURRENT_TIMESTAMP")
    private LocalDateTime fecha;

    @Column(name = "estado", nullable = false, length = 1, columnDefinition = "char(1)")
    @ColumnDefault("'1'")
    private String estado;
}
