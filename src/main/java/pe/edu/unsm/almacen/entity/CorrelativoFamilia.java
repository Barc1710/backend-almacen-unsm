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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "correlativo_familia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorrelativoFamilia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_familia", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_correlativo_familia"))
    private Familia familia;

    @Column(name = "correlativo", nullable = false)
    @ColumnDefault("1")
    private Integer correlativo;
}
