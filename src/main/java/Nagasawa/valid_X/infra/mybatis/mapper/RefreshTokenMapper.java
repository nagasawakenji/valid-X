package Nagasawa.valid_X.infra.mybatis.mapper;

import Nagasawa.valid_X.domain.model.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.UUID;

@Mapper
public interface RefreshTokenMapper {

    /** INSERTしてid(UUID)をセッタに入れる。戻り値は挿入行数(1想定) */
    int insert(RefreshToken token);

    RefreshToken findById(@Param("id") UUID id);

    RefreshToken findActiveById(@Param("id") UUID id);

    /** 単体リフレッシュをrevoke（回転時・ログアウト時など） */
    int revoke(@Param("id") UUID id, @Param("ts") Instant ts);

    /** 任意：セッション単位でまとめてrevoke（端末ログアウト用） */
    int revokeBySession(@Param("sessionId") UUID sessionId, @Param("ts") Instant ts);

    /** 任意：期限切れクリーニング */
    int deleteExpired(@Param("before") Instant before);
}