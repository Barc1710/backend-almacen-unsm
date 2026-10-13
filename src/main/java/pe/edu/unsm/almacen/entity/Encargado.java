package pe.edu.unsm.almacen.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "encargado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Encargado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "sigla_profesion", nullable = true, length = 20)
    private String siglaProfesion;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "dni", nullable = true, length = 8, columnDefinition = "char(8)")
    private String dni;

    @Column(name = "cargo", nullable = true, length = 100)
    private String cargo;

    @Column(name = "estado", nullable = false, length = 1, columnDefinition = "char(1)")
    @ColumnDefault("'1'")
    private String estado;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario",
            foreignKey = @ForeignKey(name = "fk_encargado_usuario"))
    private Usuario usuario;

    public String getNombreCompleto() {
        String prof = (siglaProfesion != null && !siglaProfesion.isBlank()) ? siglaProfesion.trim() + " " : "";
        return prof + (nombres != null ? nombres : "") + (apellidos != null ? " " + apellidos : "");
    }

    public boolean tieneUsuario() {
        return this.usuario != null;
    }

    public Integer getIdUsuario() {
        return this.usuario != null ? this.usuario.getIdUsuario() : null;
    }

    public String getLoginUsuario() {
        return this.usuario != null ? this.usuario.getUsuario() : null;
    }
}
