package Nagasawa.valid_X.infra.mybatis.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FollowMapper {

    /** 1=INSERT、0=既存（ON CONFLICT DO NOTHING） */
    int insert(@Param("followerId") Long followerId, @Param("followeeId") Long followeeId);

    /** 1=DELETE、0=元から無し */
    int delete(@Param("followerId") Long followerId, @Param("followeeId") Long followeeId);

    boolean exists(@Param("followerId") Long followerId, @Param("followeeId") Long followeeId);

    long countFollowers(@Param("userId") Long userId);  // そのユーザをフォローしている人数
    long countFollowing(@Param("userId") Long userId);  // そのユーザがフォローしている人数

    /** フォロワー列挙（自分をフォローしている人） */
    List<Long> listFollowers(@Param("userId") Long userId,
                             @Param("cursorId") Long cursorId,
                             @Param("limit") int limit);

    /** フォロー中ユーザ列挙（自分がフォローしている人） */
    List<Long> listFollowing(@Param("userId") Long userId,
                             @Param("cursorId") Long cursorId,
                             @Param("limit") int limit);
}