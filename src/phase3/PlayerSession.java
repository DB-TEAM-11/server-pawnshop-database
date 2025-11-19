package phase3;

// 원래는 세션 토큰을 클라로부터 JSON 헤더에서 받아야 하지만, 
// 여기서는 쿼리 JDBC 구현이 주이므로 싱글톤(전역변수)로 대체
public class PlayerSession {
    private static PlayerSession instance; // 싱글톤 객체
    
    // 세션 토근
    public String sessionToken;

    // 싱글톤이므로 생성자 막음
    private PlayerSession() {}
    
    // 인스턴스 전달
    public static PlayerSession getInstance() {
        if (instance == null) {
            instance = new PlayerSession();
        }
        return instance;
    }
    
    // Getters and Setters
    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getSessionToken() {
        return sessionToken;
    }
}
