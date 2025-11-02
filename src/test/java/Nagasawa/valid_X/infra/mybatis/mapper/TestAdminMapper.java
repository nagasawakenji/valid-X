package Nagasawa.valid_X.infra.mybatis.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

// テスト専用
@Mapper
public interface TestAdminMapper {

    @Update("${sql}")
    void executeStatement(@Param("sql") String sql);
}
