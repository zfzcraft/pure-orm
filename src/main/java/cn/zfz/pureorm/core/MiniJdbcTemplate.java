package cn.zfz.pureorm.core;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 极简生产版 JDBC 模板
 * 功能：增删改查、批量、多语句、事务、自动返回主键/影响行数
 */
public class MiniJdbcTemplate {

    private final Connection conn;

    public MiniJdbcTemplate(Connection conn) {
        this.conn = conn;
    }

    // ======================== 事务控制 ========================
    public void begin() throws SQLException {
        conn.setAutoCommit(false);
    }

    public void commit() throws SQLException {
        conn.commit();
    }

    public void rollback() throws SQLException {
        conn.rollback();
    }

    // ======================== 核心：智能增删改 ========================
    /**
     * 智能更新：
     * 有自增主键 → 返回主键值
     * 无自增主键 → 返回影响行数
     */
    public long save(String sql, Object... args) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // 支持返回主键
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            setParams(pstmt, args);
            int rows = pstmt.executeUpdate();

            // 尝试获取自增主键
            rs = pstmt.getGeneratedKeys();
            if (rs != null && rs.next()) {
                return rs.getLong(1);
            }

            // 没有主键，返回影响行数
            return rows;

        } finally {
            close(rs);
            close(pstmt);
        }
    }

    // ======================== 普通增删改 ========================
    public int update(String sql, Object... args) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setParams(pstmt, args);
            return pstmt.executeUpdate();
        }
    }

    // ======================== 批量更新 ========================
    public int[] batchUpdate(String sql, List<Object[]> batchArgs) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Object[] args : batchArgs) {
                setParams(pstmt, args);
                pstmt.addBatch();
            }
            return pstmt.executeBatch();
        }
    }

    // ======================== 执行多条 SQL（带事务） ========================
    public int[] executeMulti(List<String> sqlList) throws SQLException {
        int[] results = new int[sqlList.size()];
        for (int i = 0; i < sqlList.size(); i++) {
            try (PreparedStatement pstmt = conn.prepareStatement(sqlList.get(i))) {
                results[i] = pstmt.executeUpdate();
            }
        }
        return results;
    }

    // ======================== 多条 SQL + 各自参数 ========================
    public int[] executeMulti(List<String> sqlList, List<Object[]> paramList) throws SQLException {
        if (sqlList.size() != paramList.size()) {
            throw new SQLException("SQL 数量与参数数量不匹配");
        }
        int[] results = new int[sqlList.size()];
        for (int i = 0; i < sqlList.size(); i++) {
            try (PreparedStatement pstmt = conn.prepareStatement(sqlList.get(i))) {
                setParams(pstmt, paramList.get(i));
                results[i] = pstmt.executeUpdate();
            }
        }
        return results;
    }

    // ======================== 查询 ========================
    public <T> T queryOne(String sql, RowMapper<T> mapper, Object... args) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            setParams(pstmt, args);
            return rs.next() ? mapper.map(rs) : null;
        }
    }

    public <T> List<T> queryList(String sql, RowMapper<T> mapper, Object... args) throws SQLException {
        List<T> list = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setParams(pstmt, args);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapper.map(rs));
                }
            }
        }
        return list;
    }

    // ======================== 工具方法 ========================
    private void setParams(PreparedStatement pstmt, Object[] args) throws SQLException {
        if (args == null) return;
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }

    private void close(AutoCloseable ac) {
        if (ac != null) {
            try {
                ac.close();
            } catch (Exception ignored) {}
        }
    }

    
}
