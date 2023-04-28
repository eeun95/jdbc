package hello.jdbc.connection;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ConnectionTest {

    @Test
    void driverManager() throws SQLException {
        Connection con1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        Connection con2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        log.info("connection={}, class={}", con1, con1.getClass());
        log.info("connection={}, class={}", con2, con2.getClass());
    }

    @Test
    void dataSourceDriverManager() throws SQLException {
        // DriverManagerDataSource - 항상 새로운 커넥션 획득
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        useDataSource(dataSource);

    }

    @Test
    void dataSourceConnectionPool() throws SQLException, InterruptedException {
        // 스프링에서 JDBC쓰면 자동으로 제공이 됨
        // 커넥션 풀링
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setMaximumPoolSize(10);
        dataSource.setPoolName("MyPool");

        // 테스트는 별도의 풀에서 돌기 때문에 1초정도 쓰레드 슬립줘야함
        useDataSource(dataSource);
        Thread.sleep(1000);

        // 커넥션 풀에 커넥션을 채우는 것은 상대적으로 오래 걸린다
        // 애플리케이션 실행시간이 늦어질 수 있어서 별도 쓰레드를 사용해서 채워줘야 영향을 안줌
    }
    private void useDataSource(DataSource dataSource) throws SQLException {
        // 처음 객체 생성 시점에만 필요한 파라미터를 넘김
        // 설정과 사용의 분리 !!!
        // 향후 변경에 더 유연하게 대처할 수 있음
        Connection con1 = dataSource.getConnection();
        Connection con2 = dataSource.getConnection();
        log.info("connection={}, class={}", con1, con1.getClass());
        log.info("connection={}, class={}", con2, con2.getClass());
    }
}
