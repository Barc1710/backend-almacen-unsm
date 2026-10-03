package pe.edu.unsm.almacen.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Types;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@Table(name = "perfil")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perfil", nullable = false)
    private Integer idPerfil;

    @Column(name = "nombreperfil", nullable = false, length = 100)
    private String nombrePerfil;

    @Column(name = "estadoperfil", nullable = false)
    @ColumnDefault("1")
    @JdbcTypeCode(Types.TINYINT)
    private Integer estadoPerfil;

    public Integer getEstado() {
        return estadoPerfil;
    }

    public void setEstado(Integer estado) {
        this.estadoPerfil = estado;
    }
}
