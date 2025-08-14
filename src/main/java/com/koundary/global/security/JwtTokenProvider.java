package com.koundary.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final String CLAIM_UID = "uid";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TOKEN_TYPE = "token_type";
    private static final String TYPE_ACCESS = "AT";
    private static final String TYPE_REFRESH = "RT";

    private final Key key;
    private final long accessTokenExpiration;   // ms
    private final long refreshTokenExpiration;  // ms

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        // HS256은 32바이트 이상 권장
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    /* ======================== 생성 ======================== */

    /** Access Token 생성 */
    public String generateToken(Long userId, String role) {
        return buildToken(userId, role, TYPE_ACCESS, accessTokenExpiration);
    }

    /** Refresh Token 생성 */
    public String generateRefreshToken(Long userId) {
        // Refresh에는 role이 굳이 필요 없으므로 null
        return buildToken(userId, null, TYPE_REFRESH, refreshTokenExpiration);
    }

    private String buildToken(Long userId, String role, String tokenType, long validityMs) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + validityMs);

        var builder = Jwts.builder()
                .setSubject(String.valueOf(userId)) // 호환 위해 유지
                .claim(CLAIM_UID, userId)           // 명시적 uid
                .claim(CLAIM_TOKEN_TYPE, tokenType) // ✅ 타입 구분
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256);

        if (role != null) {
            builder.claim(CLAIM_ROLE, role);
        }

        return builder.compact();
    }

    /* ======================== 파싱/공통 ======================== */

    private Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isNotExpired(Claims c) {
        return c.getExpiration() != null && c.getExpiration().after(new Date());
    }

    private String getTokenType(Claims c) {
        Object type = c.get(CLAIM_TOKEN_TYPE);
        return type == null ? null : type.toString();
    }

    /* ======================== 검증 분리 ======================== */

    /** Access Token 전용 검증 */
    public boolean validateAccessToken(String token) {
        try {
            Claims c = parse(token);
            return TYPE_ACCESS.equals(getTokenType(c)) && isNotExpired(c);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /** Refresh Token 전용 검증 */
    public boolean validateRefreshToken(String token) {
        try {
            Claims c = parse(token);
            return TYPE_REFRESH.equals(getTokenType(c)) && isNotExpired(c);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /* ======================== 조회 분리 ======================== */

    public Long getUserIdFromAccessToken(String token) {
        Claims c = parse(token);
        if (!TYPE_ACCESS.equals(getTokenType(c))) {
            throw new IllegalArgumentException("Access Token이 아닙니다.");
        }
        return extractUserId(c);
    }

    public Long getUserIdFromRefreshToken(String token) {
        Claims c = parse(token);
        if (!TYPE_REFRESH.equals(getTokenType(c))) {
            throw new IllegalArgumentException("Refresh Token이 아닙니다.");
        }
        return extractUserId(c);
    }

    private Long extractUserId(Claims c) {
        Object uid = c.get(CLAIM_UID);
        if (uid instanceof Number) return ((Number) uid).longValue();
        if (uid != null) return Long.parseLong(uid.toString());
        // 구버전 호환: subject 사용 (이전 토큰들이 uid 없이 subject만 가질 수도 있음)
        return Long.parseLong(c.getSubject());
    }

    /* ======================== 편의 메서드 ======================== */

    /** Authorization 헤더에서 Bearer 토큰 추출 */
    public String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    public long getAccessTokenExpiration() { return accessTokenExpiration; }
    public long getRefreshTokenExpiration() { return refreshTokenExpiration; }

    /* ======================== (선택) 기존 API 비활성화 ======================== */
    /** @deprecated Access/Refresh 전용 메서드를 사용하세요. */
    @Deprecated
    public boolean validateToken(String token) {
        // 혼동 방지를 위해 항상 false 반환하거나, 필요시 AT/RT 아무거나 true로 하고 주석으로 명시
        return false;
    }

    /** @deprecated Access/Refresh 전용 메서드를 사용하세요. */
    @Deprecated
    public Long getUserId(String token) {
        throw new UnsupportedOperationException("Use getUserIdFromAccessToken / getUserIdFromRefreshToken");
    }
}
