package io.github.jiangood.dbq;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

class DbUtils {
    private final DataSource dataSource;

    public DbUtils(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 执行DDL语句
     */
    public void executeDDL(String sql) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    /**
     * 执行DML语句（插入、更新、删除）
     */
    public int executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = prepareStatement(conn, sql, params)) {
            return pstmt.executeUpdate();
        }
    }

    /**
     * 执行查询语句，返回单个结果，以Map形式返回
     */
    public Map<String, Object> executeQuery(String sql, Object... params) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = prepareStatement(conn, sql, params);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                Map<String, Object> result = new HashMap<>();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i);
                    result.put(columnName, value);
                }
                return result;
            }
            return null;
        }
    }

    /**
     * 准备PreparedStatement并设置参数
     */
    private PreparedStatement prepareStatement(Connection conn, String sql, Object... params) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
        return pstmt;
    }
}
