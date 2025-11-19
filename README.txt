README.txt

	[ 전체 목차 ]

 1. 프로그램 실행 순서
 2. 사용된 10개 이상의 쿼리문들(저희는 phase2의 10개의 사용하였고, 추가 쿼리는 작성하였습니다)
 3. 이전 Phase 대비 수정된 사항들
 4. 간단 기획 설명
 5. 세부 기획 목차
 6. 세부 기획
 7. SQL 쿼리문 설명 (Team11-Phase2-3.sql 설명)



						[ 프로그램 실행 순서 ]

  [ PHASE 2 쿼리 활용 ]



<시작 화면>
################################################################################
#                               전당포 운영 게임                               #
################################################################################
[1] 로그인
[2] 회원가입
[0] 게임 종료: 가능한 모든 공간에 게임 종료 선택지가 있어, 게임을 종료시킬 수 있음


1. 터미널에 [2]를 입력하여 회원가입을 시작한다.
2. 아이디(영문 30자 이하) 및 비밀번호 입력
	사용되는 쿼리
	- 아이디 중복 체크 "SELECT P.PLAYER_ID FROM PLAYER P WHERE P.PLAYER_ID = '%s'"
	- 플레이어 생성 "INSERT INTO PLAYER P (P.PLAYER_ID, P.HASHED_PW, P.SESSION_TOKEN, P.LAST_ACTIVITY) VALUES (?, ?, ?, ?)"

(
	영문 30자 초과 -> "영문 최대 30글자만 가능합니다." 출력
	중복된 아이디를 입력할 경우 -> "이미 존재하는 사용자 입니다." 출력
	비밀번호는 최대 한자 이상 입력해야 함.
)
회원가입 실패 시 다시 [시작] 창을 보여줌.

회원가입에 성공하였다면,
- PLAYER 테이블에 새로운 row를 생성
"INSERT INTO PLAYER P ( P.PLAYER_ID,  P.HASHED_PW,  P.SESSION_TOKEN,  P.LAST_ACTIVITY ) VALUES (?, ?, ?, ?)"

1번을 눌러서, 로그인 기능으로 이동

회원가입 시 생성한 아이디 및 비밀번호 입력
	사용되는 쿼리
	- 사용자가 입력한 id와 player table의 id가 동일한 행의 HASHED_PW 컬럼의 값을 불러오는 쿼리
	"SELECT P.HASHED_PW FROM PLAYER P WHERE P.PLAYER_ID = '%s'";

	- 입력한 비밀번호와 불러온 비밀번호가 동일하다면 
	사용자가 입력한 id와 player table의 id가 동일한 행이 존재하는지를 확인하는 쿼리 실행
	"SELECT P.PLAYER_KEY FROM PLAYER P WHERE P.PLAYER_ID = '%s' AND P.HASHED_PW = '%s'"
	로그인에 성공했다면, session_token을 새롭게 생성하고, LAST_ACTIVITY를 현재의 날짜 및 시간으로 업데이트하는 쿼리 실행
	"UPDATE PLAYER SET SESSION_TOKEN = '%s', LAST_ACTIVITY = TO_DATE('%s', 'YYYY-MM-DD HH24:MI:SS') WHERE PLAYER_ID = '%s'"


<로그인 실패 시>
"계정이 존재하지 않습니다" 출력

<로그인 성공 시>
[1] 게임 시작
[2] 월드 레코드
[3] 로그아웃
[0] 게임 종료

[2]
	월드 레코드 불러오는 쿼리
	- "SELECT * FROM (SELECT p.player_id, gs.nickname, gs.shop_name, gs.game_end_day_count, gs.game_end_date FROM PLAYER P, GAME_SESSION GS WHERE p.player_key = gs.player_key AND gs.game_end_day_count > 0 ORDER BY gs.game_end_day_count ASC) WHERE ROWNUM <= 10";

게임 시작
	사용되는 쿼리
	- 기존에 진행하던 게임 세션이 있는지 확인하기 위해 로그인이 할당 받은 세션토큰으로 PLAYER 테이블의 PLAYER_KEY를 불러온 후,
	"SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'";
	- 해당 PLAYER_KEY가 가지고 있는 게임 세션이 있는지 확인
	"SELECT * FROM GAME_SESSION WHERE PLAYER_KEY = %d ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY";
	
	- 기존에 진행하던 게임이 없는 경우 새로운 게임을 생성
	"INSERT INTO GAME_SESSION (PLAYER_KEY, NICKNAME, SHOP_NAME) VALUES ((SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'), '%s', '%s')";
	
	- 생성된 게임을 불러오기 위해, 로그인 시 할당 받은 세션 토큰을 통해 PLAYER 테이블에서 PLAYER_KEY를 가져옴
	"SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'";
	- 가져온 PLAYER_KEY를 통해 이에 맞는 게임 세션을 불러옴
	"SELECT * FROM GAME_SESSION WHERE PLAYER_KEY = %d ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY";

	전시중인 아이템 목록을 불러옴
	"SELECT D.DISPLAY_POS, I.*, IC.* 
	FROM GAME_SESSION_ITEM_DISPLAY D, 
		EXISTING_ITEM I, 
		ITEM_CATALOG IC 
	WHERE D.GAME_SESSION_KEY = ( 
		SELECT GAME_SESSION_KEY 
		FROM GAME_SESSION 
		WHERE PLAYER_KEY = %d 
		ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY 
	) AND D.ITEM_KEY = I.ITEM_KEY 
		AND I.ITEM_CATALOG_KEY = IC.ITEM_CATALOG_KEY 
		ORDER BY D.DISPLAY_POS";

	전시중인 아이템이 있다면, 해당 아이템의 자세한 정보를 불러옴
	- "SELECT 
		D.DISPLAY_POS, I.*, IC.*, DR.DRC_KEY, 
		DR.PURCHASE_PRICE, DR.ASKING_PRICE, 
		DR.APPRAISED_PRICE, DR.BOUGHT_DATE, 
		CC.CUSTOMER_NAME, GS.MONEY 
	FROM GAME_SESSION_ITEM_DISPLAY D, 
		EXISTING_ITEM I, ITEM_CATALOG IC, 
		DEAL_RECORD DR, CUSTOMER_CATALOG CC, 
		GAME_SESSION GS 
	WHERE D.ITEM_KEY = %d 
	AND D.ITEM_KEY = I.ITEM_KEY 
	AND I.ITEM_CATALOG_KEY = IC.ITEM_CATALOG_KEY 
	AND I.ITEM_KEY = DR.ITEM_KEY 
	AND DR.SELLER_KEY = CC.CUSTOMER_KEY 
	AND GS.GAME_SESSION_KEY = DR.GAME_SESSION_KEY"

	이전에 하던 거래가 있는지 확인
	"SELECT DR.* 
	FROM DEAL_RECORD DR, EXISTING_ITEM I 
	WHERE DR.GAME_SESSION_KEY = (
		SELECT GAME_SESSION_KEY 
		FROM GAME_SESSION 
		WHERE PLAYER_KEY = %d 
		ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
	) AND DR.ITEM_KEY = I.ITEM_KEY 
	AND I.ITEM_STATE = %d 
	ORDER BY DR.DRC_KEY"

	거래가 없다면
	- 7일차인지 확인
	"SELECT * FROM GAME_SESSION WHERE PLAYER_KEY = %d ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY";
	- 7일차라면, 주간 정산 정보 가져오기 -> 정산이 완료된 상태를 가져와 출력
	"SELECT 
		G.MONEY + SUM(BOUGHT.PURCHASE_PRICE) 
		- SUM(SOLD.SELLING_PRICE) AS TODAY_START, 
		G.MONEY AS TODAY_END, 
		FLOOR(G.PAWNSHOP_DEBT * 0.05) AS TODAY_INTEREST, 
		FLOOR(G.PERSONAL_DEBT * 0.0005) AS TODAY_INTEREST_PERSONAL, 
		G.MONEY 
		- FLOOR(G.PAWNSHOP_DEBT * 0.05) 
		- FLOOR(G.PERSONAL_DEBT * 0.0005) AS TODAY_FINAL 
		FROM (( 
				GAME_SESSION G 
				LEFT OUTER JOIN DEAL_RECORD BOUGHT 
				ON G.GAME_SESSION_KEY = BOUGHT.GAME_SESSION_KEY 
				AND G.DAY_COUNT = BOUGHT.BOUGHT_DATE 
			) LEFT OUTER JOIN DEAL_RECORD SOLD 
				ON G.GAME_SESSION_KEY = SOLD.GAME_SESSION_KEY 
				AND G.DAY_COUNT = SOLD.SOLD_DATE 
			) WHERE G.GAME_SESSION_KEY = ( 
				SELECT GAME_SESSION_KEY 
				FROM GAME_SESSION 
				WHERE PLAYER_KEY = %d 
				ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY 
			) GROUP BY G.MONEY, G.PAWNSHOP_DEBT, G.PERSONAL_DEBT";
    
		주간 정산 정보에서 가져온 이자 값을 활용하여 이자를 지불함
		"UPDATE GAME_SESSION SET MONEY = MONEY - %d 
		WHERE GAME_SESSION_KEY = (
			SELECT GAME_SESSION_KEY 
			FROM GAME_SESSION 
			WHERE PLAYER_KEY = (
				SELECT PLAYER_KEY 
				FROM PLAYER 
				WHERE SESSION_TOKEN = '%s'
			) ORDER BY GAME_SESSION_KEY DESC 
			FETCH FIRST ROW ONLY)";

		게임 오버
			TODAY_FINAL의 값이 < 0이라면 게임 오버

	7일차가 아니라면, 일일 정산 실행
	"SELECT 
		G.MONEY 
		+ SUM(BOUGHT.PURCHASE_PRICE) 
		- SUM(SOLD.SELLING_PRICE) AS TODAY_START, 
		G.MONEY AS TODAY_END, 
		FLOOR(G.PAWNSHOP_DEBT * 0.05) AS TODAY_INTEREST, 
		G.MONEY - FLOOR(G.PAWNSHOP_DEBT * 0.05) AS TODAY_FINAL 
		FROM (( 
			GAME_SESSION G LEFT OUTER JOIN DEAL_RECORD BOUGHT 
			ON G.GAME_SESSION_KEY = BOUGHT.GAME_SESSION_KEY 
			AND G.DAY_COUNT = BOUGHT.BOUGHT_DATE 
		) LEFT OUTER JOIN DEAL_RECORD SOLD 
			ON G.GAME_SESSION_KEY = SOLD.GAME_SESSION_KEY 
			AND G.DAY_COUNT = SOLD.SOLD_DATE
		) WHERE G.GAME_SESSION_KEY = ( 
			SELECT GAME_SESSION_KEY 
			FROM GAME_SESSION 
			WHERE PLAYER_KEY = %d 
			ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY 
		) GROUP BY G.MONEY, G.PAWNSHOP_DEBT";

		일일 이자 차감
		"UPDATE GAME_SESSION SET MONEY = MONEY - %d WHERE GAME_SESSION_KEY = (SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = (SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s') ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY)";
		
		게임 오버
			TODAY_FINAL의 값이 < 0이라면 게임 오버


		다음 날로 이동(GAME_SESSION의 DAY_COUNT)
		"UPDATE GAME_SESSION 
		SET DAY_COUNT = DAY_COUNT + 1 
		WHERE GAME_SESSION_KEY = (
			SELECT GAME_SESSION_KEY 
			FROM GAME_SESSION 
			WHERE PLAYER_KEY = (
				SELECT PLAYER_KEY 
				FROM PLAYER WHERE SESSION_TOKEN = '%s'
			) ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY)";


		다음 날로 업데이트된 상태의 게임 세션을 출력
		"SELECT * FROM GAME_SESSION WHERE PLAYER_KEY = %d ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY";
    

		[다음날 거래 3개 생성]
		사용자의 게임 세션을 가져오기 위해
		"SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = (SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s') ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY";
		
		해당 게임 세션의 이벤트를 가져오기 위해(이벤트에 따라 거래 상품들의 가격에 영향을 주기 때문)
		"SELECT * FROM EXISTING_NEWS N, NEWS_CATALOG NC 
		 WHERE N.GAME_SESSION_KEY = ( 
			SELECT GAME_SESSION_KEY 
			FROM GAME_SESSION 
			WHERE PLAYER_KEY = %d 
			ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY 
		) AND N.NCAT_KEY = NC.NCT_KEY ORDER BY NC.NCT_KEY";

		이후 나와 거래할 고객들을 랜덤하게 3명 선택
		"SELECT 
			CUSTOMER_KEY, 
			FRAUD, 
			WELL_COLLECT, 
			CLUMSY, 
			CATEGORY_KEY 
		FROM CUSTOMER_CATALOG 
		ORDER BY DBMS_RANDOM.VALUE FETCH FIRST %d ROWS ONLY"

		각 고객별로 거래를 시작함.
		1. 각 고객이 선호하는 카테고리와 일치하는 카테고리의 아이템을 하나 가져온다
		"SELECT * FROM ITEM_CATALOG 
		WHERE CATEGORY_KEY = %d 
		ORDER BY DBMS_RANDOM.VALUE 
		FETCH FIRST ROW ONLY";

		해당 아이템에 대한 흠 확률, 흠 갯수, 가품 확률, 등급 결정 등을 결정한 후
		아이템을 생성한다.
		"INSERT INTO EXISTING_ITEM (
			GAME_SESSION_KEY, 
			ITEM_CATALOG_KEY, 
			GRADE, FOUND_GRADE, 
			FLAW_EA, FOUND_FLAW_EA, 
			SUSPICIOUS_FLAW_AURA, 
			AUTHENTICITY, IS_AUTHENTICITY_FOUND, 
			ITEM_STATE
		) VALUES (?, ?, ?, 0, ?, 0, ?, ?, 'N', 0)";

		방금 생성한 아이템의 키를 가져옴
		"SELECT ITEM_KEY FROM EXISTING_ITEM WHERE GAME_SESSION_KEY = %d ORDER BY ITEM_KEY DESC FETCH FIRST ROW ONLY";

		거래 기준가를 계산함

		이렇게 수집하고 계산한 정보를 통해 거래를 생성한다.

	거래가 있다면
		남은 거래 중 첫 번째 거래 정보를 가져옴
		"SELECT DR.* FROM DEAL_RECORD DR, EXISTING_ITEM I 
		WHERE DR.GAME_SESSION_KEY = (
			SELECT GAME_SESSION_KEY 
			FROM GAME_SESSION 
			WHERE PLAYER_KEY = %d 
			ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
		) AND DR.ITEM_KEY = I.ITEM_KEY 
		AND I.ITEM_STATE = %d ORDER BY DR.DRC_KEY";


		현재 거래의 기록을 가져오기 위해
		"SELECT I.*, DR.* FROM EXISTING_ITEM I, DEAL_RECORD DR WHERE DR.DRC_KEY = %d AND DR.ITEM_KEY = I.ITEM_KEY";
		를 실행하고
		현재 거래의 손님의 정보를 가져오기 위해
		"SELECT CUSTOMER_NAME, IMG_ID, FRAUD, WELL_COLLECT, CLUMSY FROM CUSTOMER_CATALOG WHERE CUSTOMER_KEY = %d";
		를 실행하고
		현재 거래의 아이템 카탈로그 정보를 가져오기 위해
		"SELECT * FROM ITEM_CATALOG WHERE ITEM_CATALOG_KEY = %d";
		를 실행함.
		현재 플레이어의 잔액을 가져오기 위해
		"SELECT MONEY FROM GAME_SESSION WHERE GAME_SESSION_KEY = (
			SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = (
				SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'
			) ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
		)"
		를 실행함.
		이렇게 얻은 정보를 통해 아래의 정보들을 출력함

		또한, player_key를 가져오기 위해
		"SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'";
		가져온 PLAYER_KEY를 가지고 해당 사용자의 게임 세션 정보를 가져옴
		"SELECT * FROM GAME_SESSION WHERE PLAYER_KEY = %d ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY";
		해당 거래의 손님의 힌트 정보를 불러옴
		"SELECT HINT_REVEALED_FLAG FROM CUSTOMER_HIDDEN_DISCOVERED_IN_GAME_SESSION WHERE GAME_SESSION_KEY = %d AND CUSTOMER_KEY = %d";
		위에 불러온 정보를 통해 아래를 출력함

		이 둘은 거래 중인 상황에서 input을 받을 때 항상 실행됨
		1. 



		1번을 입력하면





로그아웃
	- 사용자의 session_token을 null로, 변경하며 세션을 무효화
	"UPDATE PLAYER SET SESSION_TOKEN = NULL WHERE SESSION_TOKEN = '%s'";

1. 1번을 누르면 게임이 시작되고,
2. 2번을 누르면 해당 게임을 클리어한 사용자 10명의 리스트, 즉 세계 기록 리스트가 뜬다
(2번을 눌렀을 때는 다음 입력 선택지도 이전과 동일. 즉, '게임 시작', '월드 레코드', '로그아웃', '게임 종료' 메뉴가 뜸)
3. 3번을 누르면 로그아웃.
4. 0번을 누르면 게임이 종료됨.


[게임 시작]
1. [1]번을 입력하여 게임 세션을 가져옴(게임 시작 직후엔 선택지는 1번 하나)
2. 진행 중인 게임 세션이 없다면, 엔터 키를 입력
3. 새 게임을 생성해야 함. [1]번을 입력하여 새 게임 세션 생성(선택지는 1번 하나)
4. 닉네임 입력(최대 10글자), 상점 이름 입력(최대 10글자) 입력 -> 게임 세션 생성 완료
5. 전시 중인 아이템 가져오기 -> [1]번 입력(선택지는 1번 하나)
5.1. 전시 중인 아이템이 없다면 계속하기 위해 Enter입력
5.1.1. 남은 거래 있는지 확인([1]번 입력. 선택지는 1번 하나). 
5.1.2. 남은 거래가 있다면, 남은 거래의 갯수를 띄워주고, 해당 거래를 계속해서 진행해야 함. (계속 진행하기 위해, 'Enter' 입력)
5.1.3. 남은 거래가 없다면 "대기 중인 거래가 없습니다." 출력. (거래 생성 하며 계속 진행하기 위해, 'Enter' 입력)
고객을 랜덤하게 3명을 불러오고 아이템 3개를 랜덤하게 불러와서 초기 거래 기록을 생성하고,
화면에 
'고객의 수', 
'고객이 생성되었다는 문구', 
'아이템 이름', '등급', '흠 갯수', '정가품 여부', '제시가 출력' 

5.1.3.1 


5.2. 전시 중인 아이템이 있다면



2.


						[ 10개의 Query문들 ]
1. Team11-Phase3-UsedPhase2Queries.sql

						[ 이전 Phase 대비 수정된 사항 ]
		



						[ 간단 기획 설명 ]
개인 빚을 갚기 위해 고향으로 내려와 전당포를 이어 운영하기로 하였다
물건의 흠을 찾거나, 진위 여부, 숨겨진 가치 등급을 밝혀내 
구매 가격을 낮추거나 감정 가격을 높여서
싸게 사서 비싸게 파는 게 목적이다.
이 과정에서 대출을 받거나, 경매에 아이템을 내놓거나 기습 이벤트(뉴스)가 발생할 수 있다.

이자를 못 내게 되면, 게임 오버
(전당포 빚 포함) 개인 빚을 모두 갚으면 게임 클리어


						[ 세부 기획 목차 ]
[거래 전 동작]
	- 뉴스 이벤트 발생
	- 손님 접수
		- 판매
		- 거래
[거래 중 동작]
	- 아이템 힌트 열람
	- 고객 뒷조사 (고객 힌트 열람)
	- 흠 찾기
	- 진위 판정
	- 감정
	- 거래하기
	- 거래 안 하기
[거래 후 동작]
	- 전시장 해금
	- 복원
	- 경매
	- 대출
	- 대출 상환하기
	- 다음 날 넘어가기
[게임 외부]
	- 게임 오버
	- 게임 클리어
	- 로그인/회원가입
	- 세계 기록 확인








