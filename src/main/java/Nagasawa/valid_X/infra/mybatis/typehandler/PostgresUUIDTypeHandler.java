// src/main/java/Nagasawa/valid_X/infra/mybatis/typehandler/PostgresUUIDTypeHandler.java
package Nagasawa.valid_X.infra.mybatis.typehandler;

import org.apache.ibatis.type.*;
import java.sql.*;
import java.util.UUID;

@MappedJdbcTypes(JdbcType.OTHER)      // PostgreSQL の uuid は OTHER 扱い
@MappedTypes(UUID.class)
public class PostgresUUIDTypeHandler extends BaseTypeHandler<UUID> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, UUID parameter, JdbcType jdbcType)
            throws SQLException {
        // PG JDBC は UUID を setObject でそのまま渡せる
        ps.setObject(i, parameter);
    }

    @Override
    public UUID getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Object obj = rs.getObject(columnName);
        return toUUID(obj);
    }

    @Override
    public UUID getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Object obj = rs.getObject(columnIndex);
        return toUUID(obj);
    }

    @Override
    public UUID getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Object obj = cs.getObject(columnIndex);
        return toUUID(obj);
    }

    private UUID toUUID(Object obj) {
        if (obj == null) return null;
        if (obj instanceof UUID u) return u;
        return UUID.fromString(obj.toString());
    }
}