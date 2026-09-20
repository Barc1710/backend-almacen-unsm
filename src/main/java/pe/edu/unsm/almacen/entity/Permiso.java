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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "permiso",
        uniqueConstraints = @UniqueConstraint(name = "uq_perfil_modulo", columnNames = {"idperfil", "idmodulo"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permiso", nullable = false)
    private Integer idPermiso;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idperfil", referencedColumnName = "id_perfil", nullable = false,
            foreignKey = @ForeignKey(name = "fk_permiso_perfil"))
    private Perfil perfil;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idmodulo", referencedColumnName = "id_modulo", nullable = false,
            foreignKey = @ForeignKey(name = "fk_permiso_modulo"))
    private Modulo modulo;

    @Column(name = "estadopermiso", nullable = false)
    @ColumnDefault("1")
    private Byte estadoPermiso;
}
