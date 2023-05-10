### 커넥션 풀 이해
1. 애플리케이션 로직은 DB 드라이버를 통해 커넥션을 조회한다.
2. DB 드라이버는 DB와 TCP/IP 커넥션을 연결한다. 물론 이 과정에서 3way handshake 같은 TCP/IP 연결을 위한 네트워크 동작이 발생한다.
3. DB 드라이버는 TCP/IP 커넥션이 연결되면 ID, PW 기타 부가정보를 DB에 전달한다.
4. DB는 ID,PW를 통해 내부 인증을 완료하고 내부에 DB 세션을 셍성한다.
5. DB는 커넥션 생성이 완료되었다는 응답을 보낸다.
6. DB 드라이버는 커넥션 객체를 생성해서 클라이언트에 반환한다. 

-> 과정도 복잡하고 시간도 많이 소모 됨
DB는 물론이고 애플리케이션 서버에서도 TCP/IP 커넥션을 생성하기 위한 리소스를 매번 사용해야 한다.
결과적으로 응답속도에 영향을 줌

> 이런 문제를 한번에 해결하는 아이디어가 바로 커넥션을 미리 생성해두고 사용하는 커넥션 풀이라는 방법이다.

애플리케이션을 시작하는 시점에 커넥션 풀은 필요한 만큼 커넥션을 미리 확보해서 풀에 보관한다.
보통 얼마나 보관할 지는 서비스의 특징과 서버 스펙에 따라 다르지만 기본값은 보통 10개이다.

커넥션 풀에 들어있는 커넥션은 TCP/IP로 DB와 커넥션이 연결되어 있는 상태이기 때문에 언제든지 즉시 SQL을 DB에 전달할 수 있다.

커넥션 풀 사용1
- 애플리케이션 로직에서 이제는 DB 드라이버를 통해서 새로운 커넥션을 획득하는 것이 아니다
- 커넥션 풀을 통해 이미 생성되어있는 커넥션을 객체 참조로 그냥 가져다 쓰기만 하면 된다
- 커넥션 풀에 커넥션을 요청하면 커넥션 풀은 자신이 가지고 있는 커넥션 중 하나를 반환한다

커넥션 풀 사용2
- 애플리케이션 로직은 커넥션 풀에서 받은 커넥션을 사용해서 SQL을 데이터베이스에 전달하고 그 결과를 받아서 처리한다
- 커넥션을 모두 사용하고 나면 커넥션을 종료하는 것이 아니라 다음에 다시 사용할 수 있도록 해당 커넥션을 그대로 커넥션 풀에 반환하면 된다
  (주의! 커넥션이 살아 있는 상태로 풀에 반환해야 함)

### DataSource 이해
DriveManager는 항상 신규 커넥션 획득 -> 커넥션 풀에서 만들어진 커넥션을 조회
변경하려면 코드의 변경 필요

커넥션을 획득하는 방법을 추상화 !!!
Datasource는 커넥션을 획득하는 방법을 추상화하는 인터페이스이다
이 인터페이스의 핵심 기능은 커넥션 조회 하나이다

### 트랜잭션의 이해
1. 사용자는 웹 애플리케이션 서버(was)나 db 접근 툴 같은 클라이언트를 사용해 데이터베이스 서버에 접근할 수 있다
2. 클라이언트는 데이터베이스 서버에 연결을 요청하고 커넥션을 맺게 된다
   - 이때 데이터베이스 서버는 내부에 세션이라는 것을 만든다 (해당 커넥션을 통한 모든 요청은 이 세션을 통해 실행)
   - 즉 개발자가 클라이언트를 통해 SQL을 전달하면 현재 커넥션에 연결된 세션이 SQL 실행
3. 세션은 트랜잭션을 시작하고, 커밋 또는 롤백을 통해 트랜잭션을 종료한다 그리고 이후에 새로운 트랜잭션을 다시 시작할 수 있다
4. 사용자가 커넥션을 닫거나 또는 DBA(DB 관리자)가 세션을 강제로 종료하면 세션은 종료된다

A 원자성 - 트랜잭션에서 실행한 작업들은 모두 성공하거나 모두 실패해야 한다
C 일관성 - 모든 트랜잭션은 일관성 있는 데이터베이스 상태를 유지해야 한다
I 고립성 - 동시에 실행되는 트랜잭션들이 서로에게 영향을 미치지 않도록 격리한다(ex. 락)
D 지속성 - 트랜잭션이 성공적으로 완료되면 그 결과가 항상 기록되어야 한다

DB 락
조회 - 조회시점에 락이 필요한 경우 (너무 중요한 로직이라 조회시점에 얻은 데이터로 어떤 계산을 수행할 때)
select for update 사용

### 트랜잭션 적용
- 트랜잭션은 비즈니스 로직이 있는 서비스 계층에서 시작해야 한다
- 서비스 계층에서 커넥션을 만들고 트랜잭션 커밋 이후에 커넥션을 종료해야 한다
- 애플리케이션에서 DB 트랜잭션을 사용하면 트랜잭션을 사용하는 동안 같은 커넥션을 유지해야 한다

### 문제점들
애플리케이션 구조
@Controller(UI 관련처리) -> @Service(비즈니스 로직) -> @Repository(DB 접근 처리) -> DB
- 프레젠테이션 계층
  - UI와 관련된 처리 담당
  - 웹 요청과 응답
  - 사용자 요청을 검증
  - 주 사용 기술 : 서블릿과 HTTP 같은 웹 기술, 스프링 MVC
- 서비스 계층
  - 비즈니스 로직을 담당
  - 주 사용 기술 : 가급적 특정 기술에 의존하지 않고, 순수 자바 코드로 작성
- 데이터 접근 계층
  - 실제 데이터베이스에 접근하는 코드
  - 주 사용 기술 : JDBC, JPA, File, Redis, Mongo ...

### 가장 큰 문제
1. 트랜잭션 문제
   - 트랜잭션을 적용하기 위해 JDBC 구현 기술이 서비스 계층에 누수됨
     - 서비스 계층은 순수해야 함 -> 구현기술을 변경해도 서비스 계층 코드는 최대한 유지할 수 있어야 한다(변화에 대응)
     - 서비스 계층은 특정 기술에 종속되지 않아야 함
   - 트랜잭션 동기화 문제
     - 같은 트랜잭션을 유지하기 위해 커넥션을 파라미터로 넘겨야 함
   - 트랜잭션 적용 반복 문제
2. 예외 누수 문제
  - 데이터 접근 계층의 JDBC 구현 기술 예외가 서비스 계층으로 전파된다
    - SQLException은 체크 예외이기 때문에 데이터 접근 게층을 호출한 서비스 계층에서 해당 예외를 잡아서 처리하거나 명시적으로 thorws를 통해서 다시 밖으로 던져야함 
    - SQLException은 JDBC 전용 기술이기 때문에 향후 JPA같은 기술을 사용할 경우 다 변경해야함
  - JDBC 반복 문제
    - 유사한 코드의 반복이 너무 많음
      - try, catch, finally...
      - PreparedStatement 사용하고 결과 매핑하고 실행하고 .. 커넥션과 리소스 정리 ...
      