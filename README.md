DBQ（Database Queue）是一个轻量级的基于JDBC的Java消息队列库。

最新版本：
![Maven Central](https://img.shields.io/maven-central/v/io.github.jiangood/dbq)

1. 添加依赖
Maven
```xml
<dependency>
    <groupId>io.github.jiangood</groupId>
    <artifactId>dbq</artifactId>
    <version>1.0.0</version>
</dependency>
```

2. 创建数据库表
DBQ需要特定的表结构来存储消息，默认情况会自动建表

```sql
CREATE TABLE queue (
    id INT PRIMARY KEY AUTO_INCREMENT,
    queue_name VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    available_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reserved_at TIMESTAMP NULL,
    attempts INT DEFAULT 0,
);
```
3. 基本使用示例
初始化DBQ
```java

public class DBQExample {
    private DataSource dataSource;
    private DBQ dbq;
    
    public void init() {
        // 配置数据源（以Mysql为例）
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3306/dbq");
        dataSource.setUser("root");
        dataSource.setPassword("123456");
        
        // 初始化DBQ
        dbq = new DBQ(dataSource);
    }
}
```
4. 消息生产者
```java
public class MessageProducer {
    private DBQ dbq;
    
    public MessageProducer(DBQ dbq) {
        this.dbq = dbq;
    }
    
    public void sendMessage(String queueName, String message) {
        try {
            // 发送消息到指定队列
            dbq.send(queueName, message);
            System.out.println("消息已发送: " + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // 发送带延迟的消息
    public void sendDelayedMessage(String queueName, String message, int delaySeconds) {
        try {
            dbq.send(queueName, message, delaySeconds);
            System.out.println("延迟消息已发送，延迟 " + delaySeconds + " 秒");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```
5. 消息消费者
```java
public class SyncConsumer {
    private DBQ dbq;
    
    public SyncConsumer(DBQ dbq) {
        this.dbq = dbq;
    }
    
    public void consumeMessage(String queueName) {
        try {
            // 接收消息（阻塞，直到有消息可用）
            String message = dbq.receive(queueName);
            if (message != null) {
                System.out.println("接收到消息: " + message);
                
                // 处理完成后删除消息
                dbq.acknowledge(queueName, message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```