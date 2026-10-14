package pe.edu.unsm.almacen.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "encargado_almacen")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EncargadoAlmacen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "dni", nullable = true, length = 8, columnDefinition = "char(8)")
    private String dni;

    @Column(name = "estado", nullable = false, length = 1, columnDefinition = "char(1)")
    @ColumnDefault("'1'")
    private String estado;

    @Column(name = "es_titular", nullable = false)
    @ColumnDefault("0")
    private Boolean esTitular;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario",
            foreignKey = @ForeignKey(name = "fk_encargado_almacen_usuario"))
    private Usuario usuario;

    public String getNombreCompleto() {
        String nom = (nombres != null ? nombres.trim() : "");
        String ape = (apellidos != null && !apellidos.isBlank() ? " " + apellidos.trim() : "");
        return (nom + ape).trim();
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

    /**
     * Alias de compatibilidad hacia reportes y módulos de movimientos.
     */
    public String getNombre() {
        return getNombreCompleto();
    }

    public static class EncargadoAlmacenBuilder {
        public EncargadoAlmacenBuilder nombre(String nombre) {
            if (nombre != null) {
                int firstSpace = nombre.indexOf(' ');
                if (firstSpace > 0) {
                    this.nombres = nombre.substring(0, firstSpace).trim();
                    this.apellidos = nombre.substring(firstSpace + 1).trim();
                } else {
                    this.nombres = nombre.trim();
                    this.apellidos = "";
                }
            }
            return this;
        }
    }
}
