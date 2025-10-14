package Nagasawa.valid_X.service;

import Nagasawa.valid_X.application.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URL;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class JwtServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;
    @Mock
    private Clock fixedClock;
    @Captor
    private ArgumentCaptor<JwtEncoderParameters> paramsCaptor;

    private JwtService jwtService;


    @BeforeEach
    void setup() {
        jwtService = new JwtService(
                jwtEncoder,
                fixedClock
        );
        ReflectionTestUtils.setField(jwtService, "issuer", "https://testIssuer");
        ReflectionTestUtils.setField(jwtService, "accessTtl", Duration.ofMinutes(15));
    }

    @Test
    @DisplayName("正常系: 正しいクレームでJwtが発行される")
    void issueAccessToken_success() throws Exception {
        // issueAccessTokenの引数
        Long userId = 5L;
        String username = "TestUser";
        UUID sessionId = UUID.randomUUID();
        int sessionVersion = 1;

        Instant now = Instant.parse("2025-10-13T12:00:00Z");
        Jwt jwtMock = mock(Jwt.class);

        when(fixedClock.instant()).thenReturn(now);
        when(jwtEncoder.encode(any())).thenReturn(jwtMock);
        when(jwtMock.getTokenValue()).thenReturn("mock-token-value");

        // 実行
        String token = jwtService.issueAccessToken(
                userId,
                username,
                sessionId,
                sessionVersion,
                null
        );

        // tokenの検証
        assertThat(token).isEqualTo("mock-token-value");

        // encode呼び出しの内容を取得
        verify(jwtEncoder).encode(paramsCaptor.capture());
        JwtEncoderParameters params = paramsCaptor.getValue();
        JwsHeader header = params.getJwsHeader();
        JwtClaimsSet claims = params.getClaims();

        // headerの検証
        assertThat(header.getAlgorithm()).isEqualTo(MacAlgorithm.HS256);

        // claimsの検証
        assertThat(claims.getIssuer()).isEqualTo(new URL("https://testIssuer"));
        assertThat(claims.getSubject()).isEqualTo("5");
        assertThat(claims.getIssuedAt()).isEqualTo(now);
        assertThat(claims.getExpiresAt()).isEqualTo(now.plus(Duration.ofMinutes(15)));
        assertThat((Object) claims.getClaim("username")).isEqualTo("TestUser");
        assertThat((Object) claims.getClaim("sid")).isEqualTo(sessionId.toString());
        assertThat((Object) claims.getClaim("sv")).isEqualTo(1);
        assertThat((Object) claims.getClaim("roles")).isEqualTo(List.of());

    }


}
