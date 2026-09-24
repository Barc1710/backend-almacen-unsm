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
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;

    @Column(name = "usuario", nullable = false, length = 50, unique = true)
    private String usuario;

    @Column(name = "clave", nullable = false, length = 255)
    private String clave;

    @Column(name = "estado", nullable = false, length = 1, columnDefinition = "char(1)")
    @ColumnDefault("'1'")
    private String estado;

    @Column(name = "dni", nullable = true, length = 8, columnDefinition = "char(8)")
    private String dni;

    @Column(name = "telefono", nullable = true, length = 20)
    private String telefono;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idperfil_usuario", referencedColumnName = "id_perfil", nullable = false,
            foreignKey = @ForeignKey(name = "fk_usuario_perfil"))
    private Perfil perfil;

    @Column(name = "correo", nullable = true, length = 100)
    private String correo;

    @Column(name = "direccion", nullable = true, length = 255)
    private String direccion;

    @Column(name = "debe_cambiar_clave", nullable = false, columnDefinition = "boolean")
    @ColumnDefault("TRUE")
    private Boolean debeCambiarClave;

    public String getNombreCompleto() {
        return (nombre != null ? nombre : "") + (apellido != null ? " " + apellido : "");
    }

    public Integer getId() {
        return this.idUsuario;
    }
}
