package Nagasawa.valid_X.infra.mybatis.mapper;

import Nagasawa.valid_X.domain.model.UserSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.UUID;

@Mapper
public interface UserSessionMapper {

    /** INSERTしてid(UUID)をセッタに入れる。戻り値は挿入行数(1想定) */
    int insert(UserSession session);

    UserSession findById(@Param("id") UUID id);

    /** 任意：最後に見た時刻を更新 */
    int updateLastSeen(@Param("id") UUID id, @Param("ts") Instant ts);

    /** 任意：即時失効用にセッション世代を+1 */
    int bumpVersion(@Param("id") UUID id);

    /** セッションをrevoke（端末ログアウト） */
    int revoke(@Param("id") UUID id, @Param("ts") Instant ts);
}