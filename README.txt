================================================================================
                       전당포 운영 게임 - README
                       Phase 3 - Team 11 Project
================================================================================

[ 전체 목차 ]

 1. 프로그램 실행 순서
 2. Phase 2에서 사용된 10개 이상의 쿼리
 3. Phase 2 대비 변경사항
 4. 게임 기획 개요
 5. 상세 게임 진행 가이드


================================================================================
                         1. 프로그램 실행 순서
================================================================================

[ 1-1. 시작 화면 ]

################################################################################
#                               전당포 운영 게임                               #
################################################################################
[1] 로그인
[2] 회원가입
[0] 게임 종료

※ 모든 화면에서 [0]을 선택하면 게임을 종료할 수 있습니다.


[ 1-2. 회원가입 절차 ]

1) [2]를 입력하여 회원가입 시작
2) 아이디 입력 (영문 30자 이하)
3) 비밀번호 입력 (최소 1자 이상)

◆ 사용되는 쿼리
   - 아이디 중복 체크
     SELECT P.PLAYER_ID FROM PLAYER P WHERE P.PLAYER_ID = '%s'
   
   - 플레이어 생성
     INSERT INTO PLAYER P (P.PLAYER_ID, P.HASHED_PW, P.SESSION_TOKEN, 
                          P.LAST_ACTIVITY) VALUES (?, ?, ?, ?)

◆ 오류 처리
   - 영문 30자 초과 → "영문 최대 30글자만 가능합니다."
   - 아이디 중복 → "이미 존재하는 사용자 입니다."
   - 회원가입 실패 시 시작 화면으로 복귀


[ 1-3. 로그인 절차 ]

1) [1]을 입력하여 로그인 시작
2) 아이디 및 비밀번호 입력

◆ 사용되는 쿼리
   - 비밀번호 조회
     SELECT P.HASHED_PW FROM PLAYER P WHERE P.PLAYER_ID = '%s'
   
   - 로그인 검증
     SELECT P.PLAYER_KEY FROM PLAYER P 
     WHERE P.PLAYER_ID = '%s' AND P.HASHED_PW = '%s'
   
   - 세션 토큰 생성 및 업데이트
     UPDATE PLAYER SET SESSION_TOKEN = '%s', 
                      LAST_ACTIVITY = TO_DATE('%s', 'YYYY-MM-DD HH24:MI:SS') 
     WHERE PLAYER_ID = '%s'

◆ 오류 처리
   - 로그인 실패 → "계정이 존재하지 않습니다"


[ 1-4. 로그인 후 메인 메뉴 ]

################################################################################
[1] 게임 시작
[2] 월드 레코드
[3] 로그아웃
[0] 게임 종료

◆ [2] 월드 레코드 조회
   - 게임 클리어 기록 상위 10명 표시
   
   쿼리:
   SELECT * FROM (
       SELECT p.player_id, gs.nickname, gs.shop_name, 
              gs.game_end_day_count, gs.game_end_date 
       FROM PLAYER P, GAME_SESSION GS 
       WHERE p.player_key = gs.player_key 
         AND gs.game_end_day_count > 0 
       ORDER BY gs.game_end_day_count ASC
   ) WHERE ROWNUM <= 10


[ 1-5. 게임 세션 시작 ]

◆ 게임 세션 확인 및 생성

   1) 플레이어 키 조회
      SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'
   
   2) 기존 게임 세션 확인
      SELECT * FROM GAME_SESSION WHERE PLAYER_KEY = %d 
      ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
   
   3) 게임 세션이 없는 경우 → 새 게임 생성
      - 닉네임 입력 (최대 10자)
      - 상점명 입력 (최대 10자)
      
      INSERT INTO GAME_SESSION (PLAYER_KEY, NICKNAME, SHOP_NAME) 
      VALUES ((SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'), 
              '%s', '%s')


[ 1-6. 전시장 아이템 조회 ]

◆ 전시 중인 아이템 목록 조회
   SELECT D.DISPLAY_POS, I.*, IC.* 
   FROM GAME_SESSION_ITEM_DISPLAY D, 
        EXISTING_ITEM I, 
        ITEM_CATALOG IC 
   WHERE D.GAME_SESSION_KEY = (
       SELECT GAME_SESSION_KEY FROM GAME_SESSION 
       WHERE PLAYER_KEY = %d 
       ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
   ) AND D.ITEM_KEY = I.ITEM_KEY 
     AND I.ITEM_CATALOG_KEY = IC.ITEM_CATALOG_KEY 
   ORDER BY D.DISPLAY_POS

◆ 전시 아이템 상세 정보 조회
   SELECT D.DISPLAY_POS, I.*, IC.*, DR.DRC_KEY, 
          DR.PURCHASE_PRICE, DR.ASKING_PRICE, DR.APPRAISED_PRICE, 
          DR.BOUGHT_DATE, CC.CUSTOMER_NAME, GS.MONEY 
   FROM GAME_SESSION_ITEM_DISPLAY D, EXISTING_ITEM I, ITEM_CATALOG IC, 
        DEAL_RECORD DR, CUSTOMER_CATALOG CC, GAME_SESSION GS 
   WHERE D.ITEM_KEY = %d 
     AND D.ITEM_KEY = I.ITEM_KEY 
     AND I.ITEM_CATALOG_KEY = IC.ITEM_CATALOG_KEY 
     AND I.ITEM_KEY = DR.ITEM_KEY 
     AND DR.SELLER_KEY = CC.CUSTOMER_KEY 
     AND GS.GAME_SESSION_KEY = DR.GAME_SESSION_KEY


[ 1-7. 거래 진행 확인 ]

◆ 진행 중인 거래 조회
   SELECT DR.* 
   FROM DEAL_RECORD DR, EXISTING_ITEM I 
   WHERE DR.GAME_SESSION_KEY = (
       SELECT GAME_SESSION_KEY FROM GAME_SESSION 
       WHERE PLAYER_KEY = %d 
       ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
   ) AND DR.ITEM_KEY = I.ITEM_KEY 
     AND I.ITEM_STATE = %d 
   ORDER BY DR.DRC_KEY


[ 1-8. 정산 시스템 ]

◆ 거래가 없을 때 → 정산 진행

   1) 7일차 확인
      SELECT * FROM GAME_SESSION WHERE PLAYER_KEY = %d 
      ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY

   2-1) 주간 정산 (7일차)
        SELECT G.MONEY + SUM(BOUGHT.PURCHASE_PRICE) 
                       - SUM(SOLD.SELLING_PRICE) AS TODAY_START, 
               G.MONEY AS TODAY_END, 
               FLOOR(G.PAWNSHOP_DEBT * 0.05) AS TODAY_INTEREST, 
               FLOOR(G.PERSONAL_DEBT * 0.0005) AS TODAY_INTEREST_PERSONAL, 
               G.MONEY - FLOOR(G.PAWNSHOP_DEBT * 0.05) 
                       - FLOOR(G.PERSONAL_DEBT * 0.0005) AS TODAY_FINAL 
        FROM ((GAME_SESSION G 
               LEFT OUTER JOIN DEAL_RECORD BOUGHT 
               ON G.GAME_SESSION_KEY = BOUGHT.GAME_SESSION_KEY 
                  AND G.DAY_COUNT = BOUGHT.BOUGHT_DATE) 
              LEFT OUTER JOIN DEAL_RECORD SOLD 
              ON G.GAME_SESSION_KEY = SOLD.GAME_SESSION_KEY 
                 AND G.DAY_COUNT = SOLD.SOLD_DATE) 
        WHERE G.GAME_SESSION_KEY = (
            SELECT GAME_SESSION_KEY FROM GAME_SESSION 
            WHERE PLAYER_KEY = %d 
            ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
        ) GROUP BY G.MONEY, G.PAWNSHOP_DEBT, G.PERSONAL_DEBT

        이자 차감:
        UPDATE GAME_SESSION SET MONEY = MONEY - %d 
        WHERE GAME_SESSION_KEY = (
            SELECT GAME_SESSION_KEY FROM GAME_SESSION 
            WHERE PLAYER_KEY = (
                SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'
            ) ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
        )

        ※ TODAY_FINAL < 0 이면 게임 오버

   2-2) 일일 정산 (1~6일차)
        SELECT G.MONEY + SUM(BOUGHT.PURCHASE_PRICE) 
                       - SUM(SOLD.SELLING_PRICE) AS TODAY_START, 
               G.MONEY AS TODAY_END, 
               FLOOR(G.PAWNSHOP_DEBT * 0.05) AS TODAY_INTEREST, 
               G.MONEY - FLOOR(G.PAWNSHOP_DEBT * 0.05) AS TODAY_FINAL 
        FROM ((GAME_SESSION G 
               LEFT OUTER JOIN DEAL_RECORD BOUGHT 
               ON G.GAME_SESSION_KEY = BOUGHT.GAME_SESSION_KEY 
                  AND G.DAY_COUNT = BOUGHT.BOUGHT_DATE) 
              LEFT OUTER JOIN DEAL_RECORD SOLD 
              ON G.GAME_SESSION_KEY = SOLD.GAME_SESSION_KEY 
                 AND G.DAY_COUNT = SOLD.SOLD_DATE) 
        WHERE G.GAME_SESSION_KEY = (
            SELECT GAME_SESSION_KEY FROM GAME_SESSION 
            WHERE PLAYER_KEY = %d 
            ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
        ) GROUP BY G.MONEY, G.PAWNSHOP_DEBT

        이자 차감:
        UPDATE GAME_SESSION SET MONEY = MONEY - %d 
        WHERE GAME_SESSION_KEY = (
            SELECT GAME_SESSION_KEY FROM GAME_SESSION 
            WHERE PLAYER_KEY = (
                SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'
            ) ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
        )

        ※ TODAY_FINAL < 0 이면 게임 오버


[ 1-9. 다음 날 진행 ]

◆ DAY_COUNT 증가
   UPDATE GAME_SESSION SET DAY_COUNT = DAY_COUNT + 1 
   WHERE GAME_SESSION_KEY = (
       SELECT GAME_SESSION_KEY FROM GAME_SESSION 
       WHERE PLAYER_KEY = (
           SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'
       ) ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
   )

◆ 업데이트된 게임 세션 조회
   SELECT * FROM GAME_SESSION WHERE PLAYER_KEY = %d 
   ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY


[ 1-10. 일일 거래 3개 생성 ]

◆ 게임 세션 키 조회
   SELECT GAME_SESSION_KEY FROM GAME_SESSION 
   WHERE PLAYER_KEY = (SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s') 
   ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY

◆ 현재 이벤트 조회
   SELECT * FROM EXISTING_NEWS N, NEWS_CATALOG NC 
   WHERE N.GAME_SESSION_KEY = (
       SELECT GAME_SESSION_KEY FROM GAME_SESSION 
       WHERE PLAYER_KEY = %d 
       ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
   ) AND N.NCAT_KEY = NC.NCT_KEY 
   ORDER BY NC.NCT_KEY

◆ 랜덤 고객 3명 선택
   SELECT CUSTOMER_KEY, FRAUD, WELL_COLLECT, CLUMSY, CATEGORY_KEY 
   FROM CUSTOMER_CATALOG 
   ORDER BY DBMS_RANDOM.VALUE 
   FETCH FIRST %d ROWS ONLY

◆ 각 고객별 거래 생성
   1) 고객 선호 카테고리 아이템 선택
      SELECT * FROM ITEM_CATALOG 
      WHERE CATEGORY_KEY = %d 
      ORDER BY DBMS_RANDOM.VALUE 
      FETCH FIRST ROW ONLY

   2) 아이템 생성 (등급, 흠, 진위 등 랜덤 결정)
      INSERT INTO EXISTING_ITEM (
          GAME_SESSION_KEY, ITEM_CATALOG_KEY, 
          GRADE, FOUND_GRADE, 
          FLAW_EA, FOUND_FLAW_EA, 
          SUSPICIOUS_FLAW_AURA, 
          AUTHENTICITY, IS_AUTHENTICITY_FOUND, 
          ITEM_STATE
      ) VALUES (?, ?, ?, 0, ?, 0, ?, ?, 'N', 0)

   3) 생성된 아이템 키 조회
      SELECT ITEM_KEY FROM EXISTING_ITEM 
      WHERE GAME_SESSION_KEY = %d 
      ORDER BY ITEM_KEY DESC FETCH FIRST ROW ONLY


[ 1-11. 거래 진행 (거래가 있을 때) ]

◆ 첫 번째 거래 정보 가져오기
   SELECT DR.* FROM DEAL_RECORD DR, EXISTING_ITEM I 
   WHERE DR.GAME_SESSION_KEY = (
       SELECT GAME_SESSION_KEY FROM GAME_SESSION 
       WHERE PLAYER_KEY = %d 
       ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
   ) AND DR.ITEM_KEY = I.ITEM_KEY 
     AND I.ITEM_STATE = %d 
   ORDER BY DR.DRC_KEY

◆ 거래 상세 정보 조회
   1) 현재 거래 기록
      SELECT I.*, DR.* FROM EXISTING_ITEM I, DEAL_RECORD DR 
      WHERE DR.DRC_KEY = %d AND DR.ITEM_KEY = I.ITEM_KEY

   2) 고객 정보
      SELECT CUSTOMER_NAME, IMG_ID, FRAUD, WELL_COLLECT, CLUMSY 
      FROM CUSTOMER_CATALOG WHERE CUSTOMER_KEY = %d

   3) 아이템 카탈로그 정보
      SELECT * FROM ITEM_CATALOG WHERE ITEM_CATALOG_KEY = %d

   4) 플레이어 잔액
      SELECT MONEY FROM GAME_SESSION WHERE GAME_SESSION_KEY = (
          SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = (
              SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'
          ) ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
      )

   5) 고객 힌트 정보
      SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'
      SELECT * FROM GAME_SESSION WHERE PLAYER_KEY = %d 
      ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
      SELECT HINT_REVEALED_FLAG 
      FROM CUSTOMER_HIDDEN_DISCOVERED_IN_GAME_SESSION 
      WHERE GAME_SESSION_KEY = %d AND CUSTOMER_KEY = %d


[ 1-12. 거래 중 행동 ]

◆ [1] 아이템 힌트 얻기 (10 골드)
   
   1) 잔액 확인
      SELECT MONEY FROM GAME_SESSION WHERE GAME_SESSION_KEY = (
          SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = (
              SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'
          ) ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
      )
      ※ 잔액 < 10 이면 실패

   2) 거래 정보 조회
      SELECT I.*, DR.* FROM EXISTING_ITEM I, DEAL_RECORD DR 
      WHERE DR.DRC_KEY = %d AND DR.ITEM_KEY = I.ITEM_KEY

   3) 고객 성향 기반 확률 계산
      SELECT (10 * CLUMSY) FLAW_BASE, 
             (15 + (65 * WELL_COLLECT)) LEGENDARY_P, 
             (20 + PROBABILITY_BASE) UNIQUE_P, 
             (30 + PROBABILITY_BASE) RARE_P, 
             (35 + PROBABILITY_BASE) NORMAL_P, 
             FAKE_P, (1 - FAKE_P) GENIUE_P
      FROM (
          SELECT CC.*, 
                 (65 * (1 - WELL_COLLECT) / 3) PROBABILITY_BASE, 
                 (10 + 90 * FRAUD) FAKE_P 
          FROM CUSTOMER_CATALOG CC 
          WHERE CUSTOMER_KEY = %d
      )
      ※ 손님의 여러 정보를 통해 아이템 정보를 계산하여 사용자가 원하는 힌트 출력

   4) 잔액 차감 (10 골드)
      UPDATE GAME_SESSION SET MONEY = MONEY - %d 
      WHERE GAME_SESSION_KEY = (
          SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = (
              SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'
          ) ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
      )

   5) 차감 후 잔액 조회
      SELECT MONEY FROM GAME_SESSION WHERE GAME_SESSION_KEY = (
          SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = (
              SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'
          ) ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
      )


◆ [2] 등급 검사 (20/30/50 골드)
   
   검사 레벨: 1 = 20골드, 2 = 30골드, 3 = 50골드
   
   1) 잔액 조회 후 비용 검증
      ※ 잔액 < 비용이면 실패

   2) 아이템 등급 정보 조회
      SELECT I.*, DR.* FROM EXISTING_ITEM I, DEAL_RECORD DR 
      WHERE DR.DRC_KEY = %d AND DR.ITEM_KEY = I.ITEM_KEY
      ※ 현재까지 밝혀진 아이템의 등급 출력


◆ [3] 흠 찾기 (20/60/100 골드)
   
   검사 레벨: 1 = 20골드, 2 = 60골드, 3 = 100골드
   
   1) 잔액 검사 후 아이템 정보 조회
      SELECT I.*, DR.* FROM EXISTING_ITEM I, DEAL_RECORD DR 
      WHERE DR.DRC_KEY = %d AND DR.ITEM_KEY = I.ITEM_KEY

   2) 발견된 흠 개수 업데이트
      UPDATE EXISTING_ITEM SET FOUND_FLAW_EA = FOUND_FLAW_EA + %d 
      WHERE ITEM_KEY = %d

   3) 아이템 카탈로그 정보 조회
      SELECT * FROM ITEM_CATALOG WHERE ITEM_CATALOG_KEY = %d
      ※ 이벤트 정보를 가져와 가격 변동 적용

   4) 거래 기록 업데이트 (가격 재계산)
      UPDATE DEAL_RECORD SET PURCHASE_PRICE = %d, APPRAISED_PRICE = %d 
      WHERE DRC_KEY = %d

   5) 사용자 잔액 차감
      UPDATE GAME_SESSION SET MONEY = MONEY - %d 
      WHERE GAME_SESSION_KEY = (
          SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = (
              SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'
          ) ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
      )

   6) 사용자 잔액 조회
      SELECT MONEY FROM GAME_SESSION WHERE GAME_SESSION_KEY = (
          SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = (
              SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'
          ) ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
      )


◆ [4] 정가품 판정 (200 골드)
   
   1) 잔액 검사 후 아이템 정보 조회
      SELECT I.*, DR.* FROM EXISTING_ITEM I, DEAL_RECORD DR 
      WHERE DR.DRC_KEY = %d AND DR.ITEM_KEY = I.ITEM_KEY
      ※ 이미 진위여부를 받았다면 스킵

   2) 진위여부 확인 플래그 업데이트
      UPDATE EXISTING_ITEM SET IS_AUTHENTICITY_FOUND = 'Y' 
      WHERE ITEM_KEY = %d

   3) 아이템 카탈로그 및 이벤트 정보 조회
      SELECT * FROM ITEM_CATALOG WHERE ITEM_CATALOG_KEY = %d
      
      SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'
      
      SELECT * FROM EXISTING_NEWS N, NEWS_CATALOG NC 
      WHERE N.GAME_SESSION_KEY = ( 
          SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = %d 
          ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY 
      ) AND N.NCAT_KEY = NC.NCT_KEY 
      ORDER BY NC.NCT_KEY
      ※ 이벤트 내용에 따라 정가품 판정 확률 계산

   4) 거래 기록 업데이트 (정가품 판정 후 가격 변경)
      UPDATE DEAL_RECORD SET PURCHASE_PRICE = %d, APPRAISED_PRICE = %d 
      WHERE DRC_KEY = %d

   5) 잔액 차감 및 조회


◆ [5] 손님 힌트 조회 (50 골드)
   
   1) 잔액 검사 (50 골드 미만이면 실패)

   2) 거래 기록 조회
      SELECT I.*, DR.* FROM EXISTING_ITEM I, DEAL_RECORD DR 
      WHERE DR.DRC_KEY = %d AND DR.ITEM_KEY = I.ITEM_KEY

   3) 게임 세션 정보 조회
      SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'
      
      SELECT * FROM GAME_SESSION WHERE PLAYER_KEY = %d 
      ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY

   4) 손님 힌트 공개 여부 확인
      SELECT HINT_REVEALED_FLAG 
      FROM CUSTOMER_HIDDEN_DISCOVERED_IN_GAME_SESSION 
      WHERE GAME_SESSION_KEY = %d AND CUSTOMER_KEY = %d

   5) 고객 정보 조회 및 힌트 제공
      SELECT CUSTOMER_NAME, IMG_ID, FRAUD, WELL_COLLECT, CLUMSY 
      FROM CUSTOMER_CATALOG WHERE CUSTOMER_KEY = %d
      ※ 사용자가 선택한 힌트 정보 제공

   6) 고객 힌트 공개 여부 업데이트
      MERGE INTO CUSTOMER_HIDDEN_DISCOVERED_IN_GAME_SESSION CH 
      USING (
          SELECT %d AS GAME_SESSION_KEY, %d AS CUSTOMER_KEY, 
                 %d AS HINT_REVEALED_FLAG FROM DUAL
      ) SOURCE 
      ON (CH.GAME_SESSION_KEY = SOURCE.GAME_SESSION_KEY 
          AND CH.CUSTOMER_KEY = SOURCE.CUSTOMER_KEY) 
      WHEN MATCHED THEN 
          UPDATE SET CH.HINT_REVEALED_FLAG = SOURCE.HINT_REVEALED_FLAG 
      WHEN NOT MATCHED THEN 
          INSERT (GAME_SESSION_KEY, CUSTOMER_KEY, HINT_REVEALED_FLAG) 
          VALUES (SOURCE.GAME_SESSION_KEY, SOURCE.CUSTOMER_KEY, 
                  SOURCE.HINT_REVEALED_FLAG)
      
   7) 잔액 차감 및 조회


◆ [6] 거래 수락 (구매)
   
   1) 현재 거래 정보 및 잔액 조회
      SELECT I.*, DR.* FROM EXISTING_ITEM I, DEAL_RECORD DR 
      WHERE DR.DRC_KEY = %d AND DR.ITEM_KEY = I.ITEM_KEY
      
      SELECT MONEY FROM GAME_SESSION WHERE GAME_SESSION_KEY = (
          SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = (
              SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'
          ) ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
      )
      ※ 현재 잔액으로 구매 가능 여부 체크

   2) 플레이어 및 게임 세션 정보 조회
      SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'
      
      SELECT * FROM GAME_SESSION WHERE PLAYER_KEY = %d 
      ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY

   3) 전시대 공간 확인
      SELECT DISPLAY_POS 
      FROM GAME_SESSION_ITEM_DISPLAY 
      WHERE GAME_SESSION_KEY = (
          SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = (
              SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'
          ) ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
      ) ORDER BY DISPLAY_POS ASC
      ※ unlockedShowcaseCount 값으로 전시 가능 개수 확인
      ※ 전시대가 가득 차면 구매 취소

   4) 거래 완료 처리
      UPDATE DEAL_RECORD SET BOUGHT_DATE = (
          SELECT DAY_COUNT FROM GAME_SESSION WHERE PLAYER_KEY = (
              SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'
          ) ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
      ) WHERE DRC_KEY = %d

   5) 아이템 상태 변경 (구매 완료)
      UPDATE EXISTING_ITEM SET ITEM_STATE = %d WHERE ITEM_KEY = %d

   6) 전시대에 아이템 추가
      INSERT INTO GAME_SESSION_ITEM_DISPLAY 
      (GAME_SESSION_KEY, DISPLAY_POS, ITEM_KEY) 
      VALUES (%d, %d, %d)

   7) 잔액 차감 및 조회


◆ [7] 거래 거절 (삭제)
   
   1) 거래 기록 삭제
      DELETE FROM DEAL_RECORD WHERE DRC_KEY = %d

   2) 아이템 삭제
      DELETE FROM EXISTING_ITEM WHERE ITEM_KEY = %d


[ 1-13. 빚 관리 및 대출 ]

◆ 세션 정보 조회
   SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'
   
   SELECT * FROM GAME_SESSION WHERE PLAYER_KEY = %d 
   ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY


◆ [1] 개인 빚 상환
   
   상환 금액 선택: 1=2000, 2=1000, 3=500, 4=100, 5=취소
   
   1) 잔액 확인
      ※ 잔액보다 많은 금액 상환 시도 시 실패

   2) 개인 빚 차감
      UPDATE GAME_SESSION G SET G.PERSONAL_DEBT = G.PERSONAL_DEBT + %d 
      WHERE G.PLAYER_KEY = (
          SELECT P.PLAYER_KEY FROM PLAYER P WHERE P.SESSION_TOKEN = '%s'
      )

   3) 잔액 차감
      UPDATE GAME_SESSION SET MONEY = MONEY + %d 
      WHERE GAME_SESSION_KEY = (
          SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = (
              SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'
          ) ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
      )

   4) 게임 클리어 여부 확인
      - 가게 빚 조회:
        SELECT PAWNSHOP_DEBT FROM PLAYER P, GAME_SESSION G 
        WHERE P.PLAYER_KEY = G.PLAYER_KEY AND P.SESSION_TOKEN = '%s' 
          AND G.GAME_END_DAY_COUNT IS NULL 
        ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
      
      - 개인 빚 조회:
        SELECT PERSONAL_DEBT FROM PLAYER P, GAME_SESSION G 
        WHERE P.PLAYER_KEY = G.PLAYER_KEY AND P.SESSION_TOKEN = '%s' 
          AND G.GAME_END_DAY_COUNT IS NULL 
        ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
      
      ※ 둘 다 0이면 게임 즉시 종료 (클리어)


◆ [2] 가게 빚 상환 및 대출
   
   선택: 1=2000, 2=1000, 3=500, 4=100, 5=-2000, 6=-1000, 7=-500, 8=-100, 9=취소
   
   1) 가게 빚 업데이트
      UPDATE GAME_SESSION G SET G.PAWNSHOP_DEBT = G.PAWNSHOP_DEBT + %d 
      WHERE G.PLAYER_KEY = (
          SELECT P.PLAYER_KEY FROM PLAYER P WHERE P.SESSION_TOKEN = '%s'
      )

   2) 잔액 업데이트
      UPDATE GAME_SESSION SET MONEY = MONEY + %d 
      WHERE GAME_SESSION_KEY = (
          SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = (
              SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'
          ) ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
      )

   3) 상환 시 게임 클리어 여부 확인
      - 가게 빚 조회:
        SELECT PAWNSHOP_DEBT FROM PLAYER P, GAME_SESSION G 
        WHERE P.PLAYER_KEY = G.PLAYER_KEY AND P.SESSION_TOKEN = '%s' 
          AND G.GAME_END_DAY_COUNT IS NULL 
        ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
      
      - 개인 빚 조회:
        SELECT PERSONAL_DEBT FROM PLAYER P, GAME_SESSION G 
        WHERE P.PLAYER_KEY = G.PLAYER_KEY AND P.SESSION_TOKEN = '%s' 
          AND G.GAME_END_DAY_COUNT IS NULL 
        ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
      
      ※ 둘 다 0이면 게임 즉시 종료 (클리어)


◆ [3] 경매
   (추가 구현 필요)


◆ [4] 복원
   (추가 구현 필요)


[ 1-14. 게임 클리어 ]

◆ 클리어한 게임 요약 조회
   SELECT GS.NICKNAME, GS.SHOP_NAME, GS.GAME_END_DAY_COUNT, GS.GAME_END_DATE 
   FROM PLAYER P, GAME_SESSION GS 
   WHERE P.SESSION_TOKEN = '%s' AND P.PLAYER_KEY = GS.PLAYER_KEY 
   ORDER BY GS.GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY

◆ 미발견 아이템 리스트 출력
   (SELECT IC.ITEM_CATALOG_NAME FROM ITEM_CATALOG IC) 
   MINUS 
   (SELECT IC.ITEM_CATALOG_NAME 
    FROM GAME_SESSION G, EXISTING_ITEM I, ITEM_CATALOG IC 
    WHERE G.PLAYER_KEY = %d AND G.GAME_SESSION_KEY = I.GAME_SESSION_KEY 
      AND I.ITEM_CATALOG_KEY = IC.ITEM_CATALOG_KEY)


[ 1-15. 게임 오버 ]

◆ 게임 오버 요약 조회
   SELECT GS.NICKNAME, GS.SHOP_NAME, GS.GAME_END_DAY_COUNT, GS.GAME_END_DATE 
   FROM PLAYER P, GAME_SESSION GS 
   WHERE P.SESSION_TOKEN = '%s' AND P.PLAYER_KEY = GS.PLAYER_KEY 
   ORDER BY GS.GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY

◆ 미발견 아이템 리스트 출력
   (SELECT IC.ITEM_CATALOG_NAME FROM ITEM_CATALOG IC) 
   MINUS 
   (SELECT IC.ITEM_CATALOG_NAME 
    FROM GAME_SESSION G, EXISTING_ITEM I, ITEM_CATALOG IC 
    WHERE G.PLAYER_KEY = %d AND G.GAME_SESSION_KEY = I.GAME_SESSION_KEY 
      AND I.ITEM_CATALOG_KEY = IC.ITEM_CATALOG_KEY)

◆ 게임 종료 처리
   1) 게임 세션 정보 조회
      SELECT * FROM GAME_SESSION WHERE PLAYER_KEY = %d 
      ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY

   2) 게임 종료 일자 기록
      UPDATE GAME_SESSION 
      SET GAME_END_DAY_COUNT = %d, GAME_END_DATE = SYSDATE 
      WHERE GAME_SESSION_KEY = (
          SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = (
              SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'
          ) ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
      )
      ※ GAME_END_DAY_COUNT는 DAY_COUNT의 음수 값


========================================
[ 2. 추가 기능 ]
========================================

[ 2-1. 로그아웃 ]

◆ 세션 토큰 무효화
   UPDATE PLAYER SET SESSION_TOKEN = NULL WHERE SESSION_TOKEN = '%s'


[ 2-2. 메뉴 선택지 ]

   1. 게임 시작
   2. 월드 레코드 조회 (게임 클리어한 상위 10명)
   3. 로그아웃
   0. 게임 종료


[ 2-3. 게임 시작 흐름 요약 ]

   1. [1]번 입력하여 게임 세션 가져오기
   
   2. 진행 중인 게임 세션이 없다면 Enter 입력
   
   3. [1]번 입력하여 새 게임 세션 생성
      - 닉네임 입력 (최대 10글자)
      - 상점 이름 입력 (최대 10글자)
      → 게임 세션 생성 완료
   
   4. 전시 중인 아이템 가져오기 → [1]번 입력
   
   5-1. 전시 중인 아이템이 없는 경우:
        - Enter 입력으로 계속 진행
        - [1]번 입력으로 남은 거래 확인
        
        5-1-1. 남은 거래가 있는 경우:
               - 남은 거래 개수 출력
               - Enter 입력으로 거래 진행
        
        5-1-2. 남은 거래가 없는 경우:
               - "대기 중인 거래가 없습니다." 출력
               - Enter 입력으로 거래 생성
               - 랜덤 고객 3명, 랜덤 아이템 3개로 초기 거래 기록 생성
               - 출력 정보: 고객 수, 생성 문구, 아이템 이름, 등급, 흠 개수, 정가품 여부, 제시가
   
   5-2. 전시 중인 아이템이 있는 경우:
        (계속 진행)


========================================
[ 3. 참조 ]
========================================

[ 3-1. 10개의 Query문들 ]
   Team11-Phase3-UsedPhase2Queries.sql 파일 참조


[ 3-2. 이전 Phase 대비 수정 사항 ]
   (내용 추가 필요)
		
