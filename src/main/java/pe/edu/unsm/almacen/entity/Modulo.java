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
@Table(name = "modulo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Modulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_modulo", nullable = false)
    private Integer idModulo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "url", nullable = true, length = 150)
    private String url;

    @Column(name = "icono", nullable = true, length = 50)
    private String icono;

    @Column(name = "orden", nullable = true)
    @ColumnDefault("0")
    private Integer orden;

    @Column(name = "estado", nullable = false)
    @ColumnDefault("1")
    private Byte estado;
}
