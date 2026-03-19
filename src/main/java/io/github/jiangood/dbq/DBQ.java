package io.github.jiangood.dbq;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DBQ {
    private final DbUtils dbUtils;

    public DBQ(DataSource dataSource) {
        this.dbUtils = new DbUtils(dataSource);
        initTable();
    }

    private void initTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS dbq_queue (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "queue_name VARCHAR(100) NOT NULL, " +
                "message TEXT NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "available_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "reserved_at TIMESTAMP NULL, " +
                "attempts INT DEFAULT 0 " +
                ")";

        try {
            dbUtils.executeDDL(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void send(String queueName, String message) throws SQLException {
        send(queueName, message, 0);
    }

    public void send(String queueName, String message, int delaySeconds) throws SQLException {
        String sql = "INSERT INTO dbq_queue (queue_name, message, available_at) VALUES (?, ?, ?)";
        LocalDateTime availableAt = LocalDateTime.now().plusSeconds(delaySeconds);
        Timestamp availableTimestamp = Timestamp.valueOf(availableAt);

        dbUtils.executeUpdate(sql, queueName, message, availableTimestamp);
    }

    public Message receive(String queueName) throws SQLException, InterruptedException {
        while (true) {
            Message message = tryReceive(queueName);
            if (message != null) {
                return message;
            }
            TimeUnit.SECONDS.sleep(1);
        }
    }

    private Message tryReceive(String queueName) throws SQLException {
        // 查询消息
        String selectSql = "SELECT id, message FROM dbq_queue WHERE queue_name = ? AND available_at <= NOW() AND reserved_at IS NULL ORDER BY available_at ASC LIMIT 1";
        Map<String, Object> result = dbUtils.executeQuery(selectSql, queueName);

        if (result != null) {
                // 更新消息状态
                int id = (Integer) result.get("id");
                String body = (String) result.get("message");
                String updateSql = "UPDATE dbq_queue SET reserved_at = NOW(), attempts = attempts + 1 WHERE id = ? AND reserved_at IS NULL";
                int rowsAffected = dbUtils.executeUpdate(updateSql, id);
                if (rowsAffected > 0) {
                    return new Message(id, body);
                }
            }

        return null;
    }

    public void acknowledge(Message message) throws SQLException {
        String sql = "DELETE FROM dbq_queue WHERE id = ? AND reserved_at IS NOT NULL";
        dbUtils.executeUpdate(sql, message.getId());
    }
}
