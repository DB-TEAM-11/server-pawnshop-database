package phase3;

import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Pattern;


public class Phase3JDBC {
	public static final String URL = "jdbc:oracle:thin:@localhost:1521:orcl";
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

	
	public static String findDuplicateID (Connection conn, String playerId) throws SQLException {
		String sql = "select P.PLAYER_ID from PLAYER P where P.PLAYER_ID = ?";
		
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, playerId);
			
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.getRow() != 0) {
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
				if (rs.next() && rs.getString(1) == hashedPw) {
					return "exist pw";
				} else {
					return "null";
				}
			}
		}
	}
	
	public static void sign(Connection conn, PreparedStatement stmt) {
		String sql = "";
		Encrypt encrypt = new Encrypt();
		
		try {			
			Scanner scan = new Scanner(System.in);
			Timestamp timestamp;			
			
			if (session_token == "") {
				System.out.println("반갑습니다! 게임을 시작하려면 " + signin + " 혹은 " + signup + "을 하십시오.");
				System.out.println("1: " + signin);
				System.out.println("2: " + signup);
				int signin_or_up = -1;
				boolean zero_to_one = true;
				
				while(zero_to_one) {
					signin_or_up = scan.nextInt();
					
					System.out.println();
					switch (signin_or_up) {
						case 1: System.out.println("[" + signin + "]");
							zero_to_one = false;
							break;
						case 2: System.out.println("[" + signup + "]");
							zero_to_one = false;
							break;
						default: System.out.println("1(" + signin + ") 또는 2(" + signup + ") 중에 입력해주세요.");
							break;
					}
				}
				
				String playerId = "";
				String password = ""; 
				boolean session = false;
				
				
				do {
					while(true) {
						System.out.print("아이디(30자 이하 영문): ");
						playerId = scan.next();
				
						if (playerId.length() <= 30 && Pattern.matches("[a-zA-Z]+", playerId)) {
							if (!"null".equals(findDuplicateID(conn, playerId))) {
								System.out.println("이미 존재하는 아이디가 있습니다.");
							} else {
								break;
							}
						} else {
							System.out.println("아이디는 30자 이하 영문이어야 합니다.");
							System.out.println();
						}
					}
					
					System.out.print("비밀번호: ");
					password = scan.next();
										
					switch (signin_or_up) {
						case 1: sql = "Select p.HASHED_PW, p.SESSION_TOKEN from PLAYER P where p.PLAYER_ID = ?";
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
										session = true;
									} else {
										System.out.println("존재하지 않는 계정입니다.");
										System.out.println();		
									}
								} else {
									System.out.println("존재하지 않는 계정입니다.");
									System.out.println();
								}
							}
							break;
							
						case 2:	sql = "insert into PLAYER (PLAYER_ID , HASHED_PW , SESSION_TOKEN , LAST_ACTIVITY) values(?, ?, ?, ?)";
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

							session_token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
							stmt.setString(3, session_token);
							
							timestamp = new Timestamp(System.currentTimeMillis());
							stmt.setTimestamp(4, timestamp);
							
							try {
								stmt.executeUpdate();
								System.out.println("회원가입에 성공하였습니다. 로그인 기능으로 이동합니다.");
							} catch(SQLException e) {
								System.err.println("error = " + e.getMessage());
								System.exit(1);
							}
							signin_or_up = 1;
							System.out.println();
							System.out.println("[" + signin + "]");
							break;
					}
				} while(session == false);
			}			 
		} catch(SQLException ex2) {
			System.err.println("sql error = " + ex2.getMessage());
			System.exit(1);
		}
	}
	
	public static void bring_recent_game(Connection conn, PreparedStatement stmt) {
		
	}
	
	public static void game_start(Connection conn, PreparedStatement stmt) {
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
			Scanner scan = new Scanner(System.in);

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
	
		}

	}
	
	
	public static void main(String[] args) {
		Connection conn = null;
		PreparedStatement stmt = null;
	
		
		// Load a JDBC driver for Oracle DBMS
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch(ClassNotFoundException e) {
			System.err.println("error = " + e.getMessage());
			System.exit(1);
		}
		
		// 
		try {
			conn = DriverManager.getConnection(URL, USER_GAME, USER_PASSWD);
		} catch (SQLException ex) {
			ex.printStackTrace();
			System.err.println("Cannot get a connection: " + ex.getLocalizedMessage());
			System.err.println("Cannot get a connection: " + ex.getMessage());
			System.exit(1);
		}
		
		sign(conn, stmt);
		game_start(conn, stmt);
		
	}
}
