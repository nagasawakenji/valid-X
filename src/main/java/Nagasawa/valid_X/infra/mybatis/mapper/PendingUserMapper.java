package Nagasawa.valid_X.infra.mybatis.mapper;

import Nagasawa.valid_X.domain.model.PendingUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PendingUserMapper {

    // idで1件取得
    PendingUser findPendingUserById(Long id);

    // emailで存在判定
    boolean existsActiveByEmail(String email);

    // tokenHashで1件取得
    PendingUser findPendingUserByTokenHash(byte[] tokenHash);

    // 仮登録ユーザーの挿入
    int insertPendingUser(PendingUser pendingUser);

    // 仮登録ユーザーの更新
    int updatePendingUser(PendingUser pendingUser);

    // 仮登録ユーザーの削除(ユーザー指定)
    int deletePendingUserById(Long id);

    // 仮登録ユーザーの削除(verified指定)
    int deletePendingUsers(boolean verified);
}
