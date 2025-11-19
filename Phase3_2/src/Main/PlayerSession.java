package Main;

// ?›?˜?Š” ?„¸?…˜ ?† ?°?„ ?´?¼ë¡œë??„° JSON ?—¤?”?—?„œ ë°›ì•„?•¼ ?•˜ì§?ë§?, 
// ?—¬ê¸°ì„œ?Š” ì¿¼ë¦¬ JDBC êµ¬í˜„?´ ì£¼ì´ë¯?ë¡? ?‹±ê¸??†¤(? „?—­ë³??ˆ˜)ë¡? ??ì²?
public class PlayerSession {
    private static PlayerSession instance; // ?‹±ê¸??†¤ ê°ì²´
    
    // ?„¸?…˜ ?† ê·?
    private String sessionToken;

    // ?‹±ê¸??†¤?´ë¯?ë¡? ?ƒ?„±? ë§‰ìŒ
    private PlayerSession() {}
    
    // ?¸?Š¤?„´?Š¤ ? „?‹¬
    public static PlayerSession getInstance() {
        if (instance == null) {
            instance = new PlayerSession();
        }
        return instance;
    }
    
    // get, set
    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
    public String getSessionToken() {
        return sessionToken;
    }
}
