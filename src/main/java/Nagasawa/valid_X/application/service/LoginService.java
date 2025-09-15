package Nagasawa.valid_X.application.service;

import Nagasawa.valid_X.domain.dto.AuthToken;
import Nagasawa.valid_X.domain.model.User;
import Nagasawa.valid_X.infra.mybatis.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final UserMapper userMapper;
    private final JwtService jwtService;

    @Value("${app.jwt.access-ttl}")
    private Duration accessTtl;

    // 登録時にJWTを発行する
    public AuthToken generateAuthToken(Long userId, Instant now) {
        User user = userMapper.findById(userId);
        String jwt = jwtService.issueAccessToken(userId, user.getUsername(), null);

        return AuthToken.builder()
                .token(jwt)
                .expiresAt(now.plus(accessTtl))
                .build();
    }
}
