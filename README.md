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