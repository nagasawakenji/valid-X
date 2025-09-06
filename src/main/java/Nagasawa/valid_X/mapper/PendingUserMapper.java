package Nagasawa.valid_X.mapper;

import Nagasawa.valid_X.model.PendingUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PendingUserMapper {

    // idで1件取得
    PendingUser findPendingUserById(Long id);

    // 仮登録ユーザーの挿入
    int insertPendingUser(PendingUser pendingUser);

    // 仮登録ユーザーの更新
    int updatePendingUser(PendingUser pendingUser);

    // 仮登録ユーザーの削除(ユーザー指定)
    int deletePendingUser(PendingUser pendingUser);

    // 仮登録ユーザーの削除(verified指定)
    int deletePendingUsers(boolean verified);
}
