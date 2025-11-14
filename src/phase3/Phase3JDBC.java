package phase3;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.StringTokenizer;
import java.text.SimpleDateFormat;


public class Phase3JDBC {
	public static final String URL = "jdbc:oracle:thin:@localhost:1521:orcl";
	public static final String USER_UNIVERSITY = "university";
	public static final String USER_PASSWD = "comp322";
	public static final String TABLE_NAME = "TEST";
	
	public static String findDuplicateID (Connection conn, String playerId) throws SQLException {
		String sql = "select P.PLAYER_ID from PLAYER P where P.PLAYER = ?";
		
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, playerId);
			
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getString(1);
				} else {
					return "null";
				}
			}
		}
	}
	
	public static String findDuplicatePW (Connection conn, StringBuilder sessionToken) throws SQLException {
		String sql = "select P.PLAYER_ID from PLAYER P where P.SESSION_TOKEN = ?";
		
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			String str_sessionToken = sessionToken.toString();
			stmt.setString(1, str_sessionToken);
			
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getString(1);
				} else {
					return "null";
				}
			}
		}
	}
	
	public static void main(String[] args) {
		Connection conn = null;
		PreparedStatement stmt = null;
		String sql = "";
		
		// Load a JDBC driver for Oracle DBMS
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch(ClassNotFoundException e) {
			System.err.println("error = " + e.getMessage());
			System.exit(1);
		}
		
		// 
		try {
			conn = DriverManager.getConnection(URL, USER_UNIVERSITY, USER_PASSWD);
		} catch (SQLException ex) {
			ex.printStackTrace();
			System.err.println("Cannot get a connection: " + ex.getLocalizedMessage());
			System.err.println("Cannot get a connection: " + ex.getMessage());
			System.exit(1);
		}
		
		try {			
			String signin = "로그인";
			String signup = "회원가입";
			String game_start = "세션(게임) 생성 또는 새 게임 시작";
			String bring_recent_game = "가장 최근 게임 세션 불러오기";
			String loading_initial_catalog_data = "초기 카탈로그 데이터 불러오기";
			String view_all_exhibition_items = "전시장 아이템 전체 조회";
			String view_news = "뉴스(당일 이벤트) 조회";
			String customer_hint = "고객 힌트(고객 정보가 드러남)";
			String get_item_hint = "아이템 힌트 조회";
			String generate_daily_deals = "하루 거래 3개 미리 생성";
			String action_with_dealing = "거래 중 액션";
			String deal_succeed = "거래(구매) 성사";
			String deal_reject = "거래 거부";
			String request_for_item_restoration = "아이템 복원 요청";
			String item_restoration_complete = "아이템 복원 완료";
			String item_sales_begin = "아이템 판매 개시";
			String item_sale_confirmed = "아이템 판매 확정";
			String loan_or_repayment = "대출 또는 상환";
			String move_on_to_the_next_day_Settle = "다음 날 넘어가기(정산하기)";
			String world_record_view_Ranking = "세계 기록 조회(랭킹)";
			String check_game_over = "게임 오버 확인";
			String signout = "로그아웃";
			String session_finish = "세선 완료";
			
			
			Scanner scan = new Scanner(System.in);
			
			String session_token = "";
			Timestamp timestamp;
			SimpleDateFormat sdf;		
			
			
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
				
				while(true) {
					System.out.println("[" + signin + "]");
					System.out.print("아이디(30자 이하 영문): ");
					playerId = scan.next();
					if (playerId.length() <= 30 && Pattern.matches("[a-zA-Z]", playerId)) {
						if (findDuplicateID(conn, playerId) != "null") {
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
				byte[] pwbyte = password.getBytes(StandardCharsets.UTF_8);
				StringBuilder pw_hex_result = new StringBuilder();
				for (byte b: pwbyte) {
					pw_hex_result.append(String.format("%02X", b));
				}
				
				
				
				switch (signin_or_up) {
					case 1: sql = "Select session_token from PLAYER P where p.PLAYER_ID = ? and p.HASHED_PW = ?";
						stmt = conn.prepareStatement(sql);
						stmt.setString(1, playerId);
						password = pw_hex_result.toString();
						stmt.setString(2, password);
						
						try (ResultSet rs = stmt.executeQuery()) {
							if (rs.next()) {
								 session_token = rs.getString(1);
							}
						}
						break;
						
					case 2:
						while(findDuplicatePW(conn, pw_hex_result) != "null") {
							pw_hex_result = new StringBuilder();
							for (byte b: pwbyte) {
								pw_hex_result.append(String.format("%02X", b));
							}
						}
						
						sql = "insert into PLAYER values(?, ?, ?, ?)";
						stmt = conn.prepareStatement(sql);
						stmt.setString(1, playerId);
						password = pw_hex_result.toString();
						stmt.setString(2, password);
						StringTokenizer tokenizer = new StringTokenizer(playerId);
						session_token = tokenizer.toString();
						stmt.setString(3, session_token);
						timestamp = new Timestamp(System.currentTimeMillis());
						sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
						stmt.setString(4, sdf.format(timestamp));
						break;
				}
			}
				
				
				
				sql = "";
				// 쿼리문 날리고
				
				
			
		
			 
		} catch(SQLException ex2) {
			System.err.println("sql error = " + ex2.getMessage());
			System.exit(1);
		}
		
		
	}
}
