package pe.edu.unsm.almacen.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.edu.unsm.almacen.security.service.UserDetailsImpl;

@Component
@Slf4j
public class JwtProvider {

    private final SecretKey signingKey;
    private final long jwtExpirationMs;

    public JwtProvider(@Value("${jwt.secret}") String jwtSecret,
                       @Value("${jwt.expiration-ms:${jwt.expiration:86400000}}") long jwtExpirationMs) {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalArgumentException("jwt.secret es obligatorio y debe estar codificado en Base64");
        }

        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(jwtSecret);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("jwt.secret debe ser una clave válida codificada en Base64");
        }
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("jwt.secret debe contener al menos 32 bytes de clave al decodificarse");
        }
        if (jwtExpirationMs <= 0) {
            throw new IllegalArgumentException("jwt.expiration debe ser mayor que cero");
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String generateToken(UserDetailsImpl userPrincipal) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .claim("id", userPrincipal.getId())
                .claim("nombre", userPrincipal.getNombreCompleto())
                .claim("perfil", userPrincipal.getPerfil())
                .claim("debeCambiarClave", userPrincipal.getDebeCambiarClave())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey)
                .compact();
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Firma JWT inválida o malformada: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("El token JWT ha expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("El token JWT no es soportado: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("La cadena de claims JWT está vacía: {}", e.getMessage());
        } catch (JwtException e) {
            log.error("Error al procesar el token JWT: {}", e.getMessage());
        }
        return false;
    }
}
