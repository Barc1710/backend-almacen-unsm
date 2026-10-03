package pe.edu.unsm.almacen.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    @Column(name = "ambiente", nullable = true, length = 100)
    private String ambiente;

    @Column(name = "estado", nullable = false, length = 1, columnDefinition = "char(1)")
    @ColumnDefault("'1'")
    private String estado;

    public String getNombreCompleto() {
        return (nombres != null ? nombres : "") + (apellidos != null ? " " + apellidos : "");
    }
}
