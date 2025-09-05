package Nagasawa.valid_X.mapper;

import Nagasawa.valid_X.model.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {

    // idで1件取得
    User findById(Long id);

    // usernameで1件取得
    User findByUsername(String username);

    // followerを全件取得
    List<User> findFollowers(Long userId);

    // followeeを全件取得
    List<User> findFollowees(Long userId);

    // ユーザーの追加
    int insertUser(User user);

    // ユーザー情報の更新
    int updateUser(User user);

    // ユーザーの削除
    int deleteUser(User user);




}
