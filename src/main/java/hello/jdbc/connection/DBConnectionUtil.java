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
            /*
            * DriverManager 가 라이브러리(초기 세팅시 build.gradle에 넣어준 h2 라이브러리)에 등록된 드라이버 목록을 자동으로 인식
            * 이 드라이버들에게 순서대로 URL, 이름, 비밀번호 등 접속에 필요한 추가 정보를 넘겨 커넥션을 획득할 수 있는지 확인
            *   - 연결 가능하면 커넥션을 획득, 안되면 다음 드라이버에게 순서 넘김
            * 찾은 커넥션 구현체를 클라이언트에 반환
            * */
            log.info("get connection={}, class={}", connection, connection.getClass());
            return connection;

        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
