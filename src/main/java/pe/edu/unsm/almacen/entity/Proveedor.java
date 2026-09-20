package pe.edu.unsm.almacen.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "proveedor")
@Getter
@Setter
@NoArgsConstructor
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "ruc", nullable = true, length = 20)
    private String ruc;

    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Column(name = "telefono", nullable = true, length = 50)
    private String telefono;

    @Column(name = "celular", nullable = true, length = 50)
    private String celular;

    @Column(name = "correo", nullable = true, length = 100)
    private String correo;

    @Column(name = "direccion", nullable = true, length = 255)
    private String direccion;

    @Column(name = "contacto", nullable = true, length = 100)
    private String contacto;

    @Column(name = "banco", nullable = true, length = 50)
    private String banco;

    @Column(name = "cuenta_corriente", nullable = true, length = 50)
    private String cuentaCorriente;

    @Column(name = "estado", nullable = false, length = 1, columnDefinition = "char(1)")
    @ColumnDefault("'1'")
    private String estado;
}
