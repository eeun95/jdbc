package hello.jdbc.repository;

import hello.jdbc.connection.ConnectionConst;
import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.NoSuchElementException;

/**
 * JDBC - DriverManager 사용
 */
@Slf4j
public class MemberRepositoryV0 {
    public Member save(Member member) throws SQLException{
        String sql = "insert into member values (?,?)";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate();
            return member;
        } catch (SQLException e) {
            log.info("db error", e);
            throw e;
        } finally {
            //pstmt.close();  // 만약 여기서 Exception이 터지면 밖으로 나가면서 con.close()가 안되는 경우 발생
            //con.close();
            close(con, pstmt, null);
        }
    }

    public Member findById(String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            con.prepareStatement(sql);
            pstmt.setString(1, memberId);

            rs = pstmt.executeQuery();
            if (rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else {
                throw new NoSuchElementException("member not found memberId=" + memberId);
            }
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        }
    }

    // 리소스 정리는 꼭 해주어야 한다.
    // 예외가 발생하든 하지 않든 항상 수행되어야 하므로 finally 구문에 주의해서 작성해야 한다.
    // 이 부분을 놓치면 커넥션이 끊어지지 않고 계속 유지되는 문제가 발생할 수 있다. = 리소스 누수, 커넥션 부족으로 장애발생할 수 있음
    private void close(Connection con, Statement stmt, ResultSet rs) {
        // pstmt는 stmt에 파라미터를 바인딩하는 기능이 추가된 것 (SQL Injection 공격 예방)

        if(stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }
        if(con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }
        if(rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }
    }

    private Connection getConnection() {
        return DBConnectionUtil.getConnection();
    }

}
