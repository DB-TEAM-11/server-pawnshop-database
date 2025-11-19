package phase3;

// 원래는 세션 토큰을 클라로부터 JSON 헤더에서 받아야 하지만, 
// 여기서는 쿼리 JDBC 구현이 주이므로 싱글톤(전역변수)로 대체
public class PlayerSession {
    private static PlayerSession instance; // 싱글톤 객체
    
    public String sessionToken; // 세션 토큰
    private int currentDrcKey;  // 현재 처리 중인 거래

    // 싱글톤이므로 생성자 막음
    private PlayerSession() {
        this.sessionToken = null;
        this.currentDrcKey = 0;
    }
    
    // 인스턴스 전달
    public static PlayerSession getInstance() {
        if (instance == null) {
            instance = new PlayerSession();
        }
        return instance;
    }
    
    // 게임 세션 초기화 (로그아웃 할 때 사용하기)
    public void reset() {
        this.sessionToken = null;
        this.currentDrcKey = 0;
    }
    
    // get, set
    public String getSessionToken() {
        return sessionToken;
    }
    
    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
    
    public int getCurrentDrcKey() {
        return currentDrcKey;
    }
    
    public void setCurrentDrcKey(int currentDrcKey) {
        this.currentDrcKey = currentDrcKey;
    }
}
