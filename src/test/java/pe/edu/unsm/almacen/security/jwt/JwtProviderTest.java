package pe.edu.unsm.almacen.security.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import pe.edu.unsm.almacen.security.service.UserDetailsImpl;

class JwtProviderTest {

    private final SecretKey key = Jwts.SIG.HS256.key().build();
    private final String secret = Base64.getEncoder().encodeToString(key.getEncoded());
    private final JwtProvider provider = new JwtProvider(secret, 60000L);

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "not-base64!", "YWJj", "YWJj$"})
    void rejectsMissingMalformedOrShortSecrets(String value) {
        assertThrows(IllegalArgumentException.class, () -> new JwtProvider(value, 60000L));
    }

    @Test
    void rejectsKeyShorterThan32Bytes() {
        String shortSecret = Base64.getEncoder().encodeToString(new byte[31]);
        assertThrows(IllegalArgumentException.class, () -> new JwtProvider(shortSecret, 60000L));
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L})
    void rejectsNonPositiveExpiration(long expiration) {
        assertThrows(IllegalArgumentException.class, () -> new JwtProvider(secret, expiration));
    }

    @Test
    void generatesSignedTokenWithUserClaimsAndConfiguredLifetime() {
        var user = new UserDetailsImpl(1, "Ana", "Perez", "ana", "unused",
                "ADMIN", true, true, List.of());

        String token = provider.generateToken(user);
        var claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

        assertTrue(provider.validateToken(token));
        assertEquals("ana", provider.getUsernameFromToken(token));
        assertEquals(1, claims.get("id", Integer.class));
        assertEquals("Ana Perez", claims.get("nombre", String.class));
        assertEquals("ADMIN", claims.get("perfil", String.class));
        assertEquals(true, claims.get("debeCambiarClave", Boolean.class));
        assertEquals(60000L, claims.getExpiration().getTime() - claims.getIssuedAt().getTime());
    }

    @Test
    void rejectsExpiredToken() {
        String token = Jwts.builder().subject("ana")
                .expiration(Date.from(Instant.now().minusSeconds(60))).signWith(key).compact();
        assertFalse(provider.validateToken(token));
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() {
        String token = Jwts.builder().subject("ana")
                .expiration(Date.from(Instant.now().plusSeconds(60)))
                .signWith(Jwts.SIG.HS256.key().build()).compact();
        assertFalse(provider.validateToken(token));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"invalid-token", "a.b.c"})
    void rejectsMalformedTokens(String token) {
        assertFalse(provider.validateToken(token));
    }
}
