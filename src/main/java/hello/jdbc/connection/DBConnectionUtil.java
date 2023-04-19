package hello.jdbc.connection;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Slf4j
public class DBConnectionUtil {

    // JDBC 표준 인터페이스가 제공하는 커넥션
    public static Connection getConnection() {
        try {
            // DB를 연결하기 위해 드라이브 매니저를 사용
            // 커넥션 인터페이스의 구현체인 h2 커넥션을 반환
            Connection connection = DriverManager.getConnection(ConnectionConst.URL, ConnectionConst.USERNAME, ConnectionConst.PASSWORD);
            log.info("get connection={}, class={}", connection, connection.getClass());
            return connection;

        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
