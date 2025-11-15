package phase3;

import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Pattern;



public class Phase3JDBC {
    private static Scanner scan = new Scanner(System.in); // 어차피 계속 쓸 건데 전역으로 하자
    public enum GamePanel{
        Login,
        InLogin,
        InGame
    }

	public static final String URL = "jdbc:oracle:thin:@localhost:1523:orcl";
	public static final String USER_GAME = "MYGAME";
	public static final String USER_PASSWD = "GAME1234";
	
	
	static String signin = "로그인";
	static String signup = "회원가입";
	static String game_start = "세션(게임) 생성 또는 새 게임 시작";
	static String bring_recent_game = "가장 최근 게임 세션 불러오기";
	static String loading_initial_catalog_data = "초기 카탈로그 데이터 불러오기";
	static String view_all_exhibition_items = "전시장 아이템 전체 조회";
	static String view_news = "뉴스(당일 이벤트) 조회";
	static String customer_hint = "고객 힌트(고객 정보가 드러남)";
	static String get_item_hint = "아이템 힌트 조회";
	static String generate_daily_deals = "하루 거래 3개 미리 생성";
	static String action_with_dealing = "거래 중 액션";
	static String deal_succeed = "거래(구매) 성사";
	static String deal_reject = "거래 거부";
	static String request_for_item_restoration = "아이템 복원 요청";
	static String item_restoration_complete = "아이템 복원 완료";
	static String item_sales_begin = "아이템 판매 개시";
	static String item_sale_confirmed = "아이템 판매 확정";
	static String loan_or_repayment = "대출 또는 상환";
	static String move_on_to_the_next_day_Settle = "다음 날 넘어가기(정산하기)";
	static String world_record_view_Ranking = "세계 기록 조회(랭킹)";
	static String check_game_over = "게임 오버 확인";
	static String signout = "로그아웃";
	static String session_finish = "세선 완료";
	static String session_token = "";

	public static void main(String[] args) {
        /* DB용 변수 선언 */
        Connection conn = null;
		PreparedStatement stmt = null;
	
        /* DB 연결 파트 */
		// Load a JDBC driver for Oracle DBMS
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch(ClassNotFoundException e) {
			System.err.println("error = " + e.getMessage());
			System.exit(1);
		}
		try {
			conn = DriverManager.getConnection(URL, USER_GAME, USER_PASSWD);
		} catch (SQLException ex) {
			ex.printStackTrace();
			System.err.println("Cannot get a connection: " + ex.getLocalizedMessage());
			System.err.println("Cannot get a connection: " + ex.getMessage());
			System.exit(1);
		}

        /*
         * 프로그램 시작
         * GamePanel == 패널 구분용 enum.
         * GamePanel.Login   : 로그인/회원가입 창
         * GamePanel.InLogin : 로그인 이후 창
         * GamePanel.InGame  : 게임 내 창
         */
        GamePanel currentPanel = GamePanel.Login; // 초기 패널은 로그인/회원가입 창
        while(true){
            if(currentPanel == GamePanel.Login){
                currentPanel = sign(conn, stmt); // 다음으로 이동할 패널을 반환 받음
            }
            else if(currentPanel == GamePanel.InLogin){
                currentPanel = GotoInLoginPanel(conn,stmt); // 다음으로 이동할 패널을 반환 받음
            }
            else if(currentPanel == GamePanel.InGame){
                currentPanel = GotoInGamePanel(conn, stmt); // 다음으로 이동할 패널을 반환 받음
            }
        }
	}


	public static GamePanel sign(Connection conn, PreparedStatement stmt) {
		String sql = "";
		Encrypt encrypt = new Encrypt();

        int signin_or_up = -1; // 로그인/회원가입 입력 저장 변수
		try{ // 나갈 때 자동 close. try-with-resources 키워드 검색
			Timestamp timestamp;
            while(true){
                /* 로그인/회원가입 창 */
                System.out.println("반갑습니다! 게임을 시작하려면 " + signin + " 혹은 " + signup + "을 하십시오.");
                System.out.println("0: " + "게임 종료"); // 게임 종료
                System.out.println("1: " + signin);
                System.out.println("2: " + signup);

                System.out.print("입력: ");
                signin_or_up = scan.nextInt(); // 사용자 입력 받기
                if(signin_or_up == 0){
                    System.out.println("프로그램을 종료합니다.");
                    System.exit(signin_or_up);
                }
                else if(signin_or_up ==1){ // 로그인 시도
                    System.out.println("[" + signin + "]"); // [로그인]
                    while(true){
                        String playerId = "";
                        String password = ""; 

                        System.out.print("아이디(30자 이하 영문. q를 눌러 취소): ");
                        playerId = scan.next();
                        if(playerId.equals("q")) {
                            break; // q를 눌러 취소
                        }
                        else if(!(playerId.length() <= 30) || !(Pattern.matches("[a-zA-Z]+", playerId))){
                            System.out.println("아이디는 30자 이하 영문이어야 합니다.\n");
                            break; // 아이디 입력 실패
                        }
                        /* 아이디 정상 입력 -> 패스워드 입력 받기 */
                        else{
                            System.out.print("비밀번호: ");
                            password = scan.next();

                            /* 아이디/패스워드 유효성 검사 */
                            sql = "Select p.HASHED_PW, p.SESSION_TOKEN from PLAYER P where p.PLAYER_ID = ?";
                            stmt = conn.prepareStatement(sql);
                            stmt.setString(1, playerId);							
                            
                            try (ResultSet rs = stmt.executeQuery()) {
                                if (rs.next()) {
                                    String result = rs.getString(1);
                                    String[] hashedpw_salt = result.split(";");
                                    String cmp_pw = encrypt.getEncrypt(password, hashedpw_salt[1]);
                                    
                                    if (hashedpw_salt[0].equals(cmp_pw)) {
                                        System.out.println("로그인에 성공하였습니다.");
                                        session_token = rs.getString(2);
                                        return GamePanel.InLogin; // 로그인 성공 -> 로그인 이후 창으로 이동합니다.
                                    } else {
                                        System.out.println("존재하지 않는 계정입니다.\n");
                                        break;
                                    }
                                } else {
                                    System.out.println("존재하지 않는 계정입니다.\n");
                                    break;
                                }
                            }
                            catch(SQLException e) {
                                System.err.println("error = " + e.getMessage());
                                System.exit(1);
                            }
                        }
                    }
                }
                else if(signin_or_up == 2){ // 회원가입 시도
                    System.out.println("[" + signup + "]"); // [회원가입]
                    while(true){
                        String playerId = "";
                        String password = "";

                        System.out.print("아이디(30자 이하 영문. q를 눌러 취소): ");
                        playerId = scan.next();
                        if(playerId.equals("q")) {
                            break; // q를 눌러 취소
                        }
                        // 아이디 제약조건 확인
                        else if(!(playerId.length() <= 30) || !(Pattern.matches("[a-zA-Z]+", playerId))){
                            System.out.println("아이디는 30자 이하 영문이어야 합니다.\n");
                            break; // 아이디 입력 실패
                        }
                        // 아이디 중복 체크
                        else if (!"null".equals(findDuplicateID(conn, playerId))) { // 뭔가 가져왔다
                            System.out.println("이미 존재하는 아이디가 있습니다.");
                            break;
                        }
                        /* 아이디 정상 입력 -> 패스워드 입력 받기 */
                        else{
                            System.out.print("비밀번호: ");
                            password = scan.next();

                            /* 아이디/패스워드 유효성 검사 */
                            sql = "insert into PLAYER (PLAYER_ID , HASHED_PW , SESSION_TOKEN , LAST_ACTIVITY) values(?, ?, ?, ?)";
                            String hashed_pw = encrypt.hashedpw_with_salt(password);
                            while(!"null".equals(findDuplicatePW(conn, playerId, hashed_pw))) {
                                hashed_pw = encrypt.hashedpw_with_salt(password);
                            }
                            stmt = conn.prepareStatement(sql);
                            
                            stmt.setString(1, playerId);
                            stmt.setString(2, hashed_pw);
                            
                            SecureRandom random = new SecureRandom();
                            byte[] bytes = new byte[48]; // Base64 인코딩 시 64글자
                            random.nextBytes(bytes);
                            /* 세션 토큰 설정 */
                            session_token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
                            stmt.setString(3, session_token);
                            
                            timestamp = new Timestamp(System.currentTimeMillis());
                            stmt.setTimestamp(4, timestamp);
                            
                            try {
                                stmt.executeUpdate();
                                System.out.println("회원가입에 성공하였습니다.");
                                break; // 다시 로그인 창으로 돌아감
                            } catch(SQLException e) {
                                System.err.println("error = " + e.getMessage());
                                System.exit(1);
                            }
                        }
                    }
                }
                else{
                    System.out.println("1(" + signin + ") 또는 2(" + signup + ") 중에 입력해주세요.");
                }
            }
		} catch(SQLException ex2) {
			System.err.println("sql error = " + ex2.getMessage());
			System.exit(1);
		}
		return GamePanel.Login;
	}

    /* 로그인 이후 패널: 가능한 기능
     * - 월드 레코드
     * - 게임 시작
     * - 로그아웃
    */
    private static GamePanel GotoInLoginPanel(Connection conn, PreparedStatement stmt){
        int userInput =-1;
        try{
            while(true){
                /* 로그인 이후 패널 창 */
                System.out.println("로그인 이후 패널에 오신 걸 환영합니다. 아래 기능 중 하나를 선택하세요");
                System.out.println("0: " + "게임 종료"); // 게임 종료
                System.out.println("1: " + world_record_view_Ranking); // 세계 기록
                System.out.println("2: " + game_start); // 게임 시작
                System.out.println("3: " + signout); // 로그아웃
                
                System.out.print("입력: ");
                userInput = scan.nextInt(); // 유저 인풋 받기
                if(userInput == 0){ // 게임 종료
                    System.out.println("프로그램을 종료합니다.");
                    System.exit(userInput); // 프로그램 종료
                }
                if(userInput == 1){ // 세계 기록 조회
                    System.out.println("[세계 기록]"); // 상위 10명만 가져옴
                    String sql = "SELECT * FROM (SELECT p.player_id, gs.nickname, gs.shop_name, gs.game_end_day_count, gs.game_end_date " +
                                 "FROM PLAYER P, GAME_SESSION GS " +
                                 "WHERE p.player_key = gs.player_key " +
                                 "AND gs.game_end_day_count > 0 " +
                                 "ORDER BY gs.game_end_day_count ASC) " +
                                 "WHERE ROWNUM <= 10";
                    stmt = conn.prepareStatement(sql);

                    try(ResultSet rs = stmt.executeQuery();){    
                        int rank = 1;
                        boolean hasRecords = false;
                        while (rs.next()) {
                            hasRecords = true;
                            String playerId = rs.getString("player_id");
                            String nickname = rs.getString("nickname");
                            String shopName = rs.getString("shop_name");
                            int endDayCount = rs.getInt("game_end_day_count");
                            Date endDate = rs.getDate("game_end_date");
                            
                            System.out.println(rank + "등: " + nickname + " (" + playerId + ")");
                            System.out.println("   가게명: " + shopName);
                            System.out.println("   클리어 일수: " + endDayCount + "일");
                            System.out.println("   클리어 날짜: " + endDate);
                            System.out.println();
                            rank += 1;
                        }
                        
                        if (!hasRecords) {
                            System.out.println("아직 게임을 클리어한 플레이어가 없습니다.\n");
                        }
                        
                    } catch (SQLException e) {
                        System.err.println("랭킹 조회 실패: " + e.getMessage());
                    }
                }
                else if(userInput ==2){ // 게임 시작
                    return GamePanel.InGame; // 인게임 창으로 이동 
                }
                else if(userInput == 3){ // 로그아웃
                    session_token = ""; // 세션토큰 초기화
                    return GamePanel.Login; // 다시 로그인/회원가입 창으로 이동
                }
                else{
                    System.out.println("가능한 선택범위 내 숫자를 입력해주세요.");
                }
            }
        }
        catch(Exception ex2) {
			System.err.println("sql error = " + ex2.getMessage());
			System.exit(1);
		}
        return null;
    }


    public static GamePanel GotoInGamePanel(Connection conn, PreparedStatement stmt){
        return GamePanel.InLogin;
    }

	// public static void game_start(Connection conn, PreparedStatement stmt) {
    public static void LoadGameSession(Connection conn, PreparedStatement stmt) {
		String sql = "";
		int GAME_SESSION_KEY = -1;
		int PLAYER_KEY = -1;
		int DAY_COUNT = 1;
		int MONEY = 50000;
		int PERSONAL_DEBT = 500000;
		int PAWNSHOP_DEBT = 0;
		int UNLOCKED_SHOWCASE_COUNT = 2;
		String NICKNAME = ""; 
		String SHOP_NAME = "";
		int GAME_END_DAY_COUNT = -1;
		Date GAME_END_DATE = null;
		
		sql = "select p.PLAYER_KEY from player p where p.SESSION_TOKEN = ?";
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, session_token);

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					PLAYER_KEY = rs.getInt(1);
				} else {
					System.out.println("존재하지 않는 계정입니다.");
					System.out.println();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if (PLAYER_KEY != -1) {

			sql = "select * from GAME_SESSION G where G.PLAYER_KEY = ?";
			try {
				stmt = conn.prepareStatement(sql);
				stmt.setInt(1, PLAYER_KEY);

				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.getRow() != 0) {
						System.out.println("이전에 진행하던 게임이 있습니다.");
						GAME_SESSION_KEY = rs.getInt(1);
						PLAYER_KEY = rs.getInt(2);
						DAY_COUNT = rs.getInt(3);
						MONEY = rs.getInt(4);
						PERSONAL_DEBT = rs.getInt(5);
						PAWNSHOP_DEBT = rs.getInt(6);
						UNLOCKED_SHOWCASE_COUNT = rs.getInt(7);
						NICKNAME = rs.getString(8);
						SHOP_NAME = rs.getString(9);
						GAME_END_DAY_COUNT = rs.getInt(10);
						GAME_END_DATE = rs.getDate(11);
					} else {
						System.out.println(game_start + "합니다. "); 
						System.out.println("닉네임과 가게명을 입력해주세요.");
						System.out.println("[닉네임]: ");
						NICKNAME = scan.next();
						System.out.println("[가게명]: ");
						SHOP_NAME = scan.next();
						
						sql = "insert into GAME_SESSION (PLAYER_KEY, NICKNAME, SHOP_NAME) values(?, ?, ?)";
						try {
							stmt = conn.prepareStatement(sql);
							stmt.setInt(1, PLAYER_KEY);
							stmt.setString(2, NICKNAME);
							stmt.setString(3, SHOP_NAME);
							
							try {
								stmt.executeUpdate();
							} catch(SQLException e) {
								System.err.println("error = " + e.getMessage());
								System.exit(1);
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			System.out.println("Day: " + DAY_COUNT);
			System.out.println("내가 가진 돈: " + MONEY);
			System.out.println("내가 가진 부채: " + PERSONAL_DEBT);
			System.out.println("가게 부채: " + PAWNSHOP_DEBT);
			System.out.println("진열장 진열 가능 갯수(최대 8개): " + UNLOCKED_SHOWCASE_COUNT);
			System.out.println("닉네임: " + NICKNAME);
			System.out.println("가게명: " + SHOP_NAME);
			
			
			
			if (DAY_COUNT % 7 == 0) {
				// event
			} 
			
			sql = "select * from (select * from CUSTOMER_CATALOG order by DBMS_RANDOM.VALUE) where rownum <= 2";
			try {
				stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery();
					
				while(true) {
					
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
			scan.close();
		}
		
	}

	public static String findDuplicateID (Connection conn, String playerId) throws SQLException {
		String sql = "select P.PLAYER_ID from PLAYER P where P.PLAYER_ID = ?";
		
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, playerId);
			
			try (ResultSet rs = stmt.executeQuery()) {
				// if (rs.getRow() != 0) {
                if(rs.next()){ // rs.next 없이 바로 getRow 하면 0번 인덱스(없음) 가리킴. 1번 인덱스(시작) 가리키게 바꿈
					return rs.getString(1);
				} else {
					return "null";
				}
			}
		}
	}
	
	public static String findDuplicatePW (Connection conn, String playerId, String hashedPw) throws SQLException {
		String sql = "select P.HASHED_PW from PLAYER P where P.PLAYER_ID = ?";
		
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, playerId);
			
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next() && rs.getString(1).equals(hashedPw)) { // << 내용 비교하려면 equals 사용해야 함
					return "exist pw";
				} else {
					return "null";
				}
			}
		}
	}

	public static void bring_recent_game(Connection conn, PreparedStatement stmt) {
		
	}







}
