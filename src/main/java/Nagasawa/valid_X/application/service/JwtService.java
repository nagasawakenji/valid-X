package Nagasawa.valid_X.application.service;

import brave.internal.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder encoder;
    private final Clock clock;
    @Value("${app.jwt.issuer}") String issuer;
    @Value("${app.jwt.access-ttl}") Duration accessTtl;

    public String issueAccessToken(Long userId, String username,
                                   UUID sessionId, int sessionVersion, @Nullable Collection<String> roles) {
        Instant now = Instant.now(clock);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plus(accessTtl))
                .subject(String.valueOf(userId))
                .claim("sid", sessionId.toString())
                .claim("sv", sessionVersion)
                .claim("username", username)
                .claim("roles", roles == null ? List.of() : roles)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }


}
