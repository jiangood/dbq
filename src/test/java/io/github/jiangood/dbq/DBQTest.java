package io.github.jiangood.dbq;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class DBQTest {
    private DBQ dbq;

    @BeforeEach
    public void setUp() {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3306/dbq");
        dataSource.setUser("root");
        dataSource.setPassword("123456");
        dbq = new DBQ(dataSource);
    }

    @Test
    public void testSendAndReceiveMessage() throws SQLException, InterruptedException {
        String queueName = "test-queue";
        String message = "Hello, DBQ!";

        // 发送消息
        dbq.send(queueName, message);
        System.out.println("消息已发送: " + message);

        // 接收消息
        Message receivedMessage = dbq.receive(queueName);
        System.out.println("接收到消息: " + receivedMessage);

        // 确认消息
        dbq.acknowledge(receivedMessage);
        System.out.println("消息已确认");
    }

    @Test
    public void testSendDelayedMessage() throws SQLException, InterruptedException {
        String queueName = "delayed-queue";
        String message = "Delayed message";
        int delaySeconds = 2;

        // 发送延迟消息
        dbq.send(queueName, message, delaySeconds);
        System.out.println("延迟消息已发送，延迟 " + delaySeconds + " 秒");

        // 接收消息（会等待直到消息可用）
        Message receivedMessage = dbq.receive(queueName);
        System.out.println("接收到延迟消息: " + receivedMessage);

        // 确认消息
        dbq.acknowledge(receivedMessage);
        System.out.println("延迟消息已确认");
    }
}
