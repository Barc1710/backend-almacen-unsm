package pe.edu.unsm.almacen.security.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pe.edu.unsm.almacen.entity.Usuario;

@Getter
public class UserDetailsImpl implements UserDetails {

    private final Integer id;
    private final String nombre;
    private final String apellido;
    private final String username;

    @JsonIgnore
    private final String password;

    private final String perfil;
    private final Boolean debeCambiarClave;
    private final boolean activo;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Integer id,
                           String nombre,
                           String apellido,
                           String username,
                           String password,
                           String perfil,
                           Boolean debeCambiarClave,
                           boolean activo,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.username = username;
        this.password = password;
        this.perfil = perfil;
        this.debeCambiarClave = debeCambiarClave;
        this.activo = activo;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(Usuario usuario) {
        String nombrePerfil = usuario.getPerfil() != null ? usuario.getPerfil().getNombrePerfil() : "USUARIO";
        boolean administrador = usuario.getPerfil() != null
                && Integer.valueOf(1).equals(usuario.getPerfil().getIdPerfil())
                && "ADMINISTRADOR".equalsIgnoreCase(nombrePerfil.trim());
        String roleName = administrador ? "ROLE_ADMINISTRADOR" : "ROLE_PERFIL";

        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(roleName));
        boolean usuarioActivo = "1".equals(usuario.getEstado());
        boolean perfilActivo = usuario.getPerfil() != null
                && usuario.getPerfil().getEstado() != null
                && usuario.getPerfil().getEstado() == 1;
        boolean activo = usuarioActivo && perfilActivo;

        return new UserDetailsImpl(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getUsuario(),
                usuario.getClave(),
                nombrePerfil,
                usuario.getDebeCambiarClave(),
                activo,
                authorities
        );
    }

    public String getNombreCompleto() {
        return (nombre != null ? nombre : "") + (apellido != null ? " " + apellido : "");
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImpl that = (UserDetailsImpl) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
