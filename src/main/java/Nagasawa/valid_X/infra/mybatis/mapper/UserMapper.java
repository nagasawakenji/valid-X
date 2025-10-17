package Nagasawa.valid_X.infra.mybatis.mapper;

import Nagasawa.valid_X.domain.dto.SearchUserSummary;
import Nagasawa.valid_X.domain.dto.UserSummary;
import Nagasawa.valid_X.domain.model.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    /*
       usersテーブルに関するDMLはここに追加していく
    */
    // idで1件取得
    User findById(Long id);

    // DisplayNemaで複数件取得
    List<SearchUserSummary> searchByDisplayNameOrUsernamePrefix(
            @Param("prefix") String prefix,
            @Param("cursor") Long cursor,
            @Param("limit") int limit
    );

    // idで複数件取得
    List<UserSummary> findSummariesByIds(@Param("ids") List<Long> ids);

    // usernameで1件取得
    User findByUsername(String username);

    // emailでuserIdを1件取得
    Long findByEmail(String email);

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

    // usernameが存在するかどうか
    boolean existsByUsername(String username);

     /*
       user_emailsテーブルに関するDMLはここに追加していく
    */
    // emailの追加
    int insertUserEmail(UserEmail userEmail);

    // emailが存在するかどうか
    boolean existsByEmail(String email);

    /*
       profilesテーブルに関するDMLはここに追加していく
    */
    int insertProfile(Profile profile);

    /*
       countsテーブルに関するDMLはここに追加していく
    */
    int insertCount(Count count);

    /*
       user_passwordsテーブルに関するDMLはここに追加していく
    */
    int insertUserPassword(UserPassword userPassword);



}
