README.txt
전당포 운영 게임 - Phase 3 - Team 11 Project

※주의 사항)
oracle db의 한글 데이터(고객 이름 등에 쓰임)를 못 받아들이기 때문에
ojdbc.jar 와 같은 경로로 
oracle_jdbc_drivers/orai18n.jar를 추가로 포함해야 합니다.



      [ 목차 ]
  1. 개요 README
    1. 게임 개요
    2. 프로그램 실행 가이드
    3. 데이터베이스 쿼리 설명
    4. 시스템 구조
    5. Phase 2 대비 변경사항 및 사용된 쿼리 10개
  2. 상세 README
  3. 실행 예시


----------------------------------------------------------------------------

1. 게임 개요

게임 스토리
    개인 빚을 갚기 위해 고향으로 내려와 전당포를 이어받아 운영하게 된 당신.
    물건의 흠을 찾고, 진위 여부를 밝혀내며, 숨겨진 가치를 발견하여
    싸게 사서 비싸게 파는 것이 목표입니다.

게임 목표
    클리어 조건: 전당포 빚과 개인 빚을 모두 상환
    게임 오버: 이자를 내지 못하거나 자금이 부족할 경우

게임 메커니즘
    - 물건 구매 및 판매를 통한 수익 창출
    - 흠 찾기, 진위 판정, 감정을 통한 가격 협상
    - 대출, 경매, 복원 시스템 활용
    - 뉴스 이벤트에 따른 시장 가격 변동
    - 일일/주간 정산 및 이자 관리

----------------------------------------------------------------------------


2. 주요 프로그램 흐름도

2.1 시작 화면

################################################################################
#                               전당포 운영 게임                               #
################################################################################
1. 로그인
2. 회원가입
0. 게임 종료

2.2 회원가입 진행

입력 요구사항
    - 아이디: "영문" 30자 이하
    - 비밀번호: 최소 1자 이상

회원가입 절차
    1. [2] 선택하여 회원가입 시작
    2. 아이디 입력 (중복 체크 자동 실행)
    3. 비밀번호 입력
    4. 회원가입 완료 시 PLAYER 테이블에 신규 계정 생성

오류 처리
    - 아이디 길이 초과
        "영문 최대 30글자만 가능합니다."
    - 아이디 중복
        "이미 존재하는 사용자 입니다." 

관련 쿼리
    아이디 중복 체크
       SELECT HASHED_PW FROM PLAYER WHERE PLAYER_ID = '%s'
    
    플레이어 생성
        INSERT INTO PLAYER P ( P.PLAYER_ID,  P.HASHED_PW,  P.SESSION_TOKEN, P.LAST_ACTIVITY ) VALUES (?, ?, ?, ?)


2.3 로그인

로그인 절차
    1. [1] 선택하여 로그인 시작
    2. 아이디 및 비밀번호 입력
    3. 비밀번호 해시 검증
    4. 세션 토큰 생성

오류 처리
    - 로그인 실패
        "계정이 존재하지 않습니다"

관련 쿼리
    비밀번호 조회
       SELECT P.HASHED_PW FROM PLAYER P WHERE P.PLAYER_ID = '%s'
    
    로그인 검증
       SELECT P.PLAYER_KEY FROM PLAYER P 
       WHERE P.PLAYER_ID = '%s' AND P.HASHED_PW = '%s'
    
    세션 업데이트
       UPDATE PLAYER SET SESSION_TOKEN = '%s', 
                        LAST_ACTIVITY = TO_DATE('%s', 'YYYY-MM-DD HH24:MI:SS') 
       WHERE PLAYER_ID = '%s'


2.4 로그인 후 메인 메뉴
################################################################################
#                               전당포 운영 게임                               #
################################################################################
1. 게임 시작
2. 월드 레코드
3. 로그아웃
0. 게임 종료
선택 (0~3):

월드 레코드
    - 게임 클리어 기록 상위 10명 표시
    - 플레이어 ID, 닉네임, 상점명, 생존 일수, 클리어 날짜 표시
    
    관련 쿼리:
    SELECT * FROM (
        SELECT p.player_id, gs.nickname, gs.shop_name, 
               gs.game_end_day_count, gs.game_end_date 
        FROM PLAYER P, GAME_SESSION GS 
        WHERE p.player_key = gs.player_key 
          AND gs.game_end_day_count > 0 
        ORDER BY gs.game_end_day_count ASC
    ) WHERE ROWNUM <= 10

로그아웃
    - 세션 토큰 무효화
    
    관련 쿼리:
    UPDATE PLAYER SET SESSION_TOKEN = NULL 
    WHERE SESSION_TOKEN = '%s'


2.5 게임 세션 시작
################################################################################
#                            게임 세션 가져오기 요청                           #
################################################################################
진행 중인 게임 세션이 있는지 확인하고, 있다면 불러와야 합니다.
1. 게임 세션 가져오기
선택 (1~1): 1

진행 중인 게임 세션이 없습니다.

계속하려면 Enter를 누르세요...
################################################################################
#                            새 게임 세션 생성 요청                            #
################################################################################
진행 중인 게임이 없습니다. 새 게임을 시작해야 합니다.
1. 새 게임 세션 생성
선택 (1~1): 1
닉네임을 입력하세요 (최대 10글자): 
닉네임이 입력되지 않았습니다.
닉네임을 입력하세요 (최대 10글자): d
상점 이름을 입력하세요 (최대 10글자): shop

=== 새 게임 세션 생성 완료 ===
닉네임: d
상점명: shop
시작 일수: 1일
시작 잔액: 50,000G
개인 빚: 500,000G
전당포 빚: 0G
===========================

게임 세션 로드/생성
    1. 기존 게임 세션 확인
    2. 없을 경우: 닉네임(최대 10자), 상점명(최대 10자) 입력 후 신규 생성
    3. 있을 경우: 기존 세션 로드

관련 쿼리
    플레이어 키 조회
       SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'
    
    게임 세션 확인
       SELECT * FROM GAME_SESSION WHERE PLAYER_KEY = %d 
       ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
    
    신규 게임 세션 생성
       INSERT INTO GAME_SESSION (PLAYER_KEY, NICKNAME, SHOP_NAME, UNLOCKED_SHOWCASE_COUNT) VALUES ((SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'), '%s', '%s', 8)

2.5.5 경매/복원 결과 처리하기

경매 정산하기
    - 경매 중(ITEM_STATE = 3)인 아이템 확인
    - 경매 시작 후 2일이 경과한 아이템 자동 정산
    - 감정가의 1.2~1.5배 랜덤 금액으로 판매
    - 판매 완료 시 아이템 상태를 판매됨(ITEM_STATE = 4)으로 변경

복원 완료 처리
    - 복원 중(ITEM_STATE = 2)인 아이템 확인
    - 복원 시작 후 1일이 경과한 아이템 자동 완료 처리
    - 찾은 흠 개수만큼 감정가 상승 (흠당 5%)
    - 복원 완료 시 아이템 상태를 복원 완료(ITEM_STATE = 5)로 변경

2.6 게임 플레이 - 전시장 관리
################################################################################
#                        전시 중인 아이템 가져오기 요청                        #
################################################################################
게임 진행을 위해서는 전시 중인 아이템을 가져와야 합니다.
1. 전시 중인 아이템 가져오기
선택 (1~1): 1
전시 중인 아이템이 없습니다.
계속하려면 Enter를 누르세요...

전시중인 아이템 조회
    - 전시장에 배치된 아이템 목록 표시
    - 위치, 아이템 정보, 카탈로그 정보 포함

    관련 쿼리:
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

전시 아이템 상세 정보
    - 구매가, 요구가, 감정가
    - 구매 날짜, 판매자 정보
    - 현재 보유 자금
    
    관련 쿼리:
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


2.7 게임 플레이 - 거래 시스템
################################################################################
#                         대기 중인 거래 기록 확인 요청                        #
################################################################################
남은 거래가 있는지 확인해야 합니다. 남은 거래가 있다면, 그 거래를 해야 합니다.
1. 남은 거래 있는지 확인하기
선택 (1~1): 1

=== 대기 중인 거래 ===
대기 중인 거래 수: 0개
====================

계속하려면 Enter를 누르세요...

거래 진행 확인
    - 진행 중인 거래가 있는지 확인
    - 없을 경우 신규 거래 생성 (랜덤 고객 3명)
    
    관련 쿼리:
    SELECT DR.* FROM DEAL_RECORD DR, EXISTING_ITEM I 
    WHERE DR.GAME_SESSION_KEY = (
        SELECT GAME_SESSION_KEY FROM GAME_SESSION 
        WHERE PLAYER_KEY = %d 
        ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
    ) AND DR.ITEM_KEY = I.ITEM_KEY 
      AND I.ITEM_STATE = %d 
    ORDER BY DR.DRC_KEY

신규 거래 생성 프로세스

=== 오늘의 거래 생성 ===
현재 적용 중인 이벤트 없음
고객 3명 선택 완료
1번 거래 생성: 도자기 컵 (등급: 레전더리, 흠: 4개, 가품, 제시가: 7,478G)
2번 거래 생성: 입체주의 그림 (등급: 레전더리, 흠: 3개, 가품, 제시가: 7,894G)
3번 거래 생성: 청동상 (등급: 유니크, 흠: 13개, 가품, 제시가: 4,242G)

총 3개의 거래가 생성되었습니다!
======================

    1. 랜덤 고객 3명 선택
       SELECT CUSTOMER_KEY, FRAUD, WELL_COLLECT, CLUMSY, CATEGORY_KEY 
       FROM CUSTOMER_CATALOG 
       ORDER BY DBMS_RANDOM.VALUE FETCH FIRST %d ROWS ONLY
    
    2. 각 고객의 선호 카테고리 아이템 선택
       SELECT * FROM ITEM_CATALOG 
       WHERE CATEGORY_KEY = %d 
       ORDER BY DBMS_RANDOM.VALUE FETCH FIRST ROW ONLY
    
    3. 아이템 생성 (등급, 흠, 진위 등 랜덤 결정)
       INSERT INTO EXISTING_ITEM (
           GAME_SESSION_KEY, ITEM_CATALOG_KEY, GRADE, FOUND_GRADE, 
           FLAW_EA, FOUND_FLAW_EA, SUSPICIOUS_FLAW_AURA, 
           AUTHENTICITY, IS_AUTHENTICITY_FOUND, ITEM_STATE
       ) VALUES (?, ?, ?, 0, ?, 0, ?, ?, 'N', 0)
    
    4. 생성된 아이템 키 조회
       SELECT ITEM_KEY FROM EXISTING_ITEM WHERE GAME_SESSION_KEY = %d 
       ORDER BY ITEM_KEY DESC FETCH FIRST ROW ONLY



거래 중 가능한 행동
    - 물건 조사(아이템 힌트 열람)
    - 고객 뒷조사 (고객 힌트 열람)
    - 흠 찾기
    - 진위 판정
    - 감정
    - 거래하기 / 거래 거절
################################################################################
#                                  고객과 거래                                 #
################################################################################
=====================================
고객: 위쟁반 (CIM00010)
아이템: 도자기 컵
발견 등급: 일반
발견 흠: 0개
진위 판정: 미확인

최초 제시가: 7,478G
현재 구매가: 7,478G
현재 감정가: 7,478G

---공개된 고객 힌트---
(공개된 힌트 없음)
(공개된 힌트 없음)

현재 잔액: 50,000G
=====================================
1. 물건 조사
2. 등급 감정
3. 흠 찾기
4. 진위 판정 [200G 소모]
5. 새 고객 힌트 열기
6. 거래 수락
7. 거래 거절
8. 거래 일시정지, 메인으로 돌아가기
0. 게임 종료
선택 (0~8): 


거래 정보 조회
    현재 거래 기록
       SELECT I.*, DR.* FROM EXISTING_ITEM I, DEAL_RECORD DR 
       WHERE DR.DRC_KEY = %d AND DR.ITEM_KEY = I.ITEM_KEY
    
    고객 정보
       SELECT CUSTOMER_NAME, IMG_ID, FRAUD, WELL_COLLECT, CLUMSY 
       FROM CUSTOMER_CATALOG WHERE CUSTOMER_KEY = %d
    
    아이템 카탈로그
       SELECT * FROM ITEM_CATALOG WHERE ITEM_CATALOG_KEY = %d
    
    현재 자금
       SELECT MONEY FROM GAME_SESSION WHERE GAME_SESSION_KEY = (
           SELECT GAME_SESSION_KEY FROM GAME_SESSION 
           WHERE PLAYER_KEY = (
               SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'
           ) ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
       )
    
    고객 힌트 정보
       SELECT HINT_REVEALED_FLAG 
       FROM CUSTOMER_HIDDEN_DISCOVERED_IN_GAME_SESSION 
       WHERE GAME_SESSION_KEY = %d AND CUSTOMER_KEY = %d


2.8 게임 플레이 - 정산 시스템

일일 정산 (1~6일차)
    - 당일 시작 자금, 종료 자금 계산
    - 전당포 빚 이자 5% 차감
    - 자금이 음수가 되면 게임 오버
    
    관련 쿼리:
    SELECT G.MONEY + SUM(BOUGHT.PURCHASE_PRICE) - SUM(SOLD.SELLING_PRICE) 
               AS TODAY_START, 
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

주간 정산 (7일차)
    - 전당포 빚 이자 5% 차감
    - 개인 빚 이자 0.05% 차감
    - 총 이자 차감 후 자금 확인
    
    관련 쿼리:
    SELECT G.MONEY + SUM(BOUGHT.PURCHASE_PRICE) - SUM(SOLD.SELLING_PRICE) 
               AS TODAY_START, 
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

이자 차감
    UPDATE GAME_SESSION SET MONEY = MONEY - %d 
    WHERE GAME_SESSION_KEY = (
        SELECT GAME_SESSION_KEY FROM GAME_SESSION 
        WHERE PLAYER_KEY = (
            SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'
        ) ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
    )

다음 날로 진행
    UPDATE GAME_SESSION SET DAY_COUNT = DAY_COUNT + 1 
    WHERE GAME_SESSION_KEY = (
        SELECT GAME_SESSION_KEY FROM GAME_SESSION 
        WHERE PLAYER_KEY = (
            SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'
        ) ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
    )


2.9 게임 플레이 - 이벤트 시스템

뉴스 이벤트
    - 7일차마다 랜덤 이벤트 발생
    - 특정 카테고리 아이템 가격 변동
    
    관련 쿼리:
    SELECT * FROM EXISTING_NEWS N, NEWS_CATALOG NC 
    WHERE N.GAME_SESSION_KEY = (
        SELECT GAME_SESSION_KEY FROM GAME_SESSION 
        WHERE PLAYER_KEY = %d 
        ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
    ) AND N.NCAT_KEY = NC.NCT_KEY 
    ORDER BY NC.NCT_KEY


----------------------------------------------------------------------------

4. 시스템 구조

4.1 프로젝트 구조

src/phase3/
    ├── Main.java                   - 프로그램 진입점
    ├── PlayerSession.java          - 플레이어 세션 관리
    │
    ├── constants/                  - 상수 정의
    │   └── ItemState.java          - 아이템 상태 열거형
    │
    ├── exceptions/                 - 예외 처리
    │   ├── CloseGameException.java - 게임 종료 예외
    │   └── NotASuchRowException.java - 데이터 없음 예외
    │
    ├── queries/                    - 데이터베이스 쿼리 클래스
    │   ├── AuthenticationCreator.java    - 인증 생성
    │   ├── IdIsExist.java               - 아이디 존재 확인
    │   ├── HashedPwGetter.java          - 비밀번호 조회
    │   ├── SessionToken.java            - 세션 토큰 관리
    │   ├── GameSessionByToken.java      - 세션 조회
    │   ├── GameSessionUpdater.java      - 세션 업데이트
    │   ├── InsertGameSession.java       - 세션 생성
    │   ├── WorldRecord.java             - 월드 레코드
    │   ├── DealRecordByItemState.java   - 거래 조회
    │   ├── InsertDealRecord.java        - 거래 생성
    │   ├── DealRecordUpdater.java       - 거래 업데이트
    │   ├── DeleteDeal.java              - 거래 삭제
    │   ├── RandomCustomers.java         - 랜덤 고객
    │   ├── StaticCustomer.java          - 고객 정보
    │   ├── CustomerInfo.java            - 고객 상세
    │   ├── ItemCatalog.java             - 아이템 카탈로그
    │   ├── InsertExistingItem.java      - 아이템 생성
    │   ├── ExistingItemUpdater.java     - 아이템 업데이트
    │   ├── DisplayManagement.java       - 전시장 관리
    │   ├── DailyCalculate.java          - 일일 정산
    │   ├── WeeklyCaluclate.java         - 주간 정산
    │   ├── MoneyUpdater.java            - 자금 업데이트
    │   ├── TodaysEvent.java             - 오늘의 이벤트
    │   └── ...
    │
    ├── screens/                    - 화면 UI 클래스
    │   ├── BaseScreen.java         - 기본 화면
    │   ├── IntroScreen.java        - 시작 화면
    │   ├── LoginScreen.java        - 로그인 화면
    │   ├── MainScreen.java         - 메인 화면
    │   ├── DealScreen.java         - 거래 화면
    │   ├── SellScreen.java         - 판매 화면
    │   └── DebtAndItemScreen.java  - 빚/아이템 처리 화면
    │
    └── utils/                      - 유틸리티 클래스
        └── PasswordHasher.java     - 비밀번호 해싱


4.2 화면 흐름도

IntroScreen (시작 화면)
     │
     ├─→ [회원가입] → PlayerSession 생성 → IntroScreen
     │
     └─→ [로그인] → LoginScreen
                       │
                       └─→ MainScreen (메인 메뉴)
                              │
                              ├─→ [게임 시작] → 게임 세션 로드/생성
                              │                      │
                              │                      └─→ DealScreen (거래 화면)
                              │                             │
                              │                             ├─→ [거래 행동]
                              │                             ├─→ [전시장 관리]
                              │                             ├─→ [정산]
                              │                             └─→ [다음 날]
                              │
                              ├─→ [월드 레코드] → 기록 표시 → MainScreen
                              │
                              ├─→ [로그아웃] → IntroScreen
                              │
                              └─→ [게임 종료] → 프로그램 종료

----------------------------------------------------------------------------

5. Phase 2 대비 변경사항 및 사용된 쿼리 10개

5.1 데이터베이스 스키마 변경사항

DEAL_RECORD 테이블
    - LAST_ACTION_DATE 컬럼 추가
        : 마지막 액션 시작 일수를 기록하기 위한 필드 추가
        : 거래 진행 상황을 추적하는데 활용

EXISTING_ITEM 테이블
    ITEM_STATE IN (             ITEM_STATE IN (
        0, -- 생성 됨               0, -- 생성 됨
        1, -- 전시 중               1, -- 전시 중
        2, -- 복원 중               2, -- 복원 중
        3, -- 경매 중               3, -- 경매 중
        4  -- 판매 됨               4,  -- 판매 됨
                                    5  -- 판매 됨
    )                           )

5.2 Phase 2 쿼리 활용

Phase 2에서 작성한 20개의 쿼리 중 실제 사용된 쿼리
1. 플레이어 정보

SELECT * FROM GAME_SESSION WHERE PLAYER_KEY = 1 ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY

쿼리 클래스.함수() : PlayerInfo.getPlayerInfo()
사용된 곳: DealScreen.java, DebtAndItemScreen.java, MainScreen.java


2. 진열대에 진열되어 있는 Item들 (Type 8)

SELECT D.DISPLAY_POS, I.*, IC.* FROM GAME_SESSION_ITEM_DISPLAY D, EXISTING_ITEM I, ITEM_CATALOG IC WHERE D.GAME_SESSION_KEY = ( SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = %d ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY ) AND D.ITEM_KEY = I.ITEM_KEY AND I.ITEM_CATALOG_KEY = IC.ITEM_CATALOG_KEY ORDER BY D.DISPLAY_POS

쿼리 클래스.함수() : ItemInDisplay.getItemInDisplay()
사용된 곳: ItemInDisplay.java, DebtAndItemScreen.java, MainScreen.java


3. 당일자 Event

SELECT * FROM EXISTING_NEWS N, NEWS_CATALOG NC WHERE N.GAME_SESSION_KEY = ( SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = %d ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY ) AND N.NCAT_KEY = NC.NCT_KEY ORDER BY NC.NCT_KEY

쿼리 클래스.함수() : TodaysEvent.getTodaysEvent()
사용된 곳: DealScreen.java, SellScreen.java, MainScreen.java


4. 손님 pk -> 손님 선호하는 Category 진열장에 있는 선호 Item들

SELECT * FROM CUSTOMER_CATALOG C, GAME_SESSION_ITEM_DISPLAY D, EXISTING_ITEM I, ITEM_CATALOG IC WHERE C.CUSTOMER_KEY = %d AND D.GAME_SESSION_KEY = ( SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = %d ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY ) AND D.ITEM_KEY = I.ITEM_KEY AND I.ITEM_CATALOG_KEY = IC.ITEM_CATALOG_KEY AND IC.CATEGORY_KEY = C.CATEGORY_KEY ORDER BY D.DISPLAY_POS

쿼리 클래스.함수() : PreferableItemsInDisplay()
사용된 곳: MainScreen.java


5. 손님의 정보 + 힌트: 사기정도, 잘수집정도, 부주의함

SELECT CUSTOMER_NAME, IMG_ID, FRAUD, WELL_COLLECT, CLUMSY FROM CUSTOMER_CATALOG WHERE CUSTOMER_KEY = %d

쿼리 클래스.함수() : CustomerInfo.getCustomerInfo()
사용된 곳: DealScreen.java


6. 거래에 필요한 정보 (레전더리 확률값, 유니크 확률값, 레어 확률값, 진품 확률값) (Type 7)

SELECT HINT_REVEALED_FLAG FROM CUSTOMER_HIDDEN_DISCOVERED_IN_GAME_SESSION WHERE GAME_SESSION_KEY = %d AND CUSTOMER_KEY = %d

쿼리 클래스.함수() : CustomerHiddenDiscovered.getHintRevealedFlag()
사용된 곳: DealScreen.java


7. 다음 날 넘어갈 때, 정산 -> 결과

SELECT G.MONEY + SUM(BOUGHT.PURCHASE_PRICE) - SUM(SOLD.SELLING_PRICE) AS TODAY_START, G.MONEY AS TODAY_END, FLOOR(G.PAWNSHOP_DEBT * 0.05) AS TODAY_INTEREST, G.MONEY - FLOOR(G.PAWNSHOP_DEBT * 0.05) AS TODAY_FINAL FROM (( GAME_SESSION G LEFT OUTER JOIN DEAL_RECORD BOUGHT ON G.GAME_SESSION_KEY = BOUGHT.GAME_SESSION_KEY AND G.DAY_COUNT = BOUGHT.BOUGHT_DATE ) LEFT OUTER JOIN DEAL_RECORD SOLD ON G.GAME_SESSION_KEY = SOLD.GAME_SESSION_KEY AND G.DAY_COUNT = SOLD.SOLD_DATE ) WHERE G.GAME_SESSION_KEY = ( SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = %d ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY ) GROUP BY G.MONEY, G.PAWNSHOP_DEBT

쿼리 클래스.함수() : DailyCalculate.getDailyCalculate()
사용된 곳: MainScreen.java


8. 다음 주로 넘어갈 때, 정산 (일별 정산 포함) -> 결과

SELECT G.MONEY + SUM(BOUGHT.PURCHASE_PRICE) - SUM(SOLD.SELLING_PRICE) AS TODAY_START, G.MONEY AS TODAY_END, FLOOR(G.PAWNSHOP_DEBT * 0.05) AS TODAY_INTEREST, FLOOR(G.PERSONAL_DEBT * 0.0005) AS TODAY_INTEREST_PERSONAL, G.MONEY - FLOOR(G.PAWNSHOP_DEBT * 0.05) - FLOOR(G.PERSONAL_DEBT * 0.0005) AS TODAY_FINAL FROM (( GAME_SESSION G LEFT OUTER JOIN DEAL_RECORD BOUGHT ON G.GAME_SESSION_KEY = BOUGHT.GAME_SESSION_KEY AND G.DAY_COUNT = BOUGHT.BOUGHT_DATE ) LEFT OUTER JOIN DEAL_RECORD SOLD ON G.GAME_SESSION_KEY = SOLD.GAME_SESSION_KEY AND G.DAY_COUNT = SOLD.SOLD_DATE ) WHERE G.GAME_SESSION_KEY = ( SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = %d ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY ) GROUP BY G.MONEY, G.PAWNSHOP_DEBT, G.PERSONAL_DEBT

쿼리 클래스.함수() : WeeklyCaluclate.getWeeklyCaluclate()
사용된 곳: MainScreen.java


9. 해당 아이디 있는지 확인 (Type 1)

SELECT HASHED_PW FROM PLAYER WHERE PLAYER_ID = '%s'

쿼리 클래스.함수() : IdIsExist.isIdExist()
사용된 곳: LoginScreen.java


10. 특정 플레이어가 모든 게임 세션에 걸쳐 발견 못 한 아이템 종류명 (Type 10)

(SELECT IC.ITEM_CATALOG_NAME FROM ITEM_CATALOG IC) MINUS ( SELECT IC.ITEM_CATALOG_NAME FROM GAME_SESSION G, EXISTING_ITEM I, ITEM_CATALOG IC WHERE G.PLAYER_KEY = %d AND G.GAME_SESSION_KEY = I.GAME_SESSION_KEY AND I.ITEM_CATALOG_KEY = IC.ITEM_CATALOG_KEY )

쿼리 클래스.함수() : NotFoundItem.getNotFoundItemName()
사용된 곳: MainScreen.java
-----------------------------------------------------------------------------


=============================================================================

상세 README

=============================================================================

[ 1-1. 시작 화면 선택지 ]
[1] 로그인
[2] 회원가입
[0] 게임 종료

[ 1-2. 회원가입 절차 ]

1) [2]를 입력하여 회원가입 시작
2) 아이디 입력 (영문 30자 이하)
3) 비밀번호 입력 (최소 1자 이상)

   사용되는 쿼리
   - 아이디 중복 체크
     SELECT P.PLAYER_ID FROM PLAYER P WHERE P.PLAYER_ID = '%s'
   
   - 플레이어 생성
     INSERT INTO PLAYER P ( P.PLAYER_ID,  P.HASHED_PW,  P.SESSION_TOKEN, P.LAST_ACTIVITY )
            VALUES (?, ?, ?, ?)

   오류 처리
   - 영문 30자 초과 → "영문 최대 30글자만 가능합니다."
   - 아이디 중복 → "이미 존재하는 사용자 입니다."
   - 회원가입 실패 시 시작 화면으로 복귀


[ 1-3. 로그인 절차 ]

1) [1]을 입력하여 로그인 시작
2) 아이디 및 비밀번호 입력

   사용되는 쿼리
   - 비밀번호 조회
     SELECT P.HASHED_PW FROM PLAYER P WHERE P.PLAYER_ID = '%s'
   
   - 로그인 검증
     SELECT P.PLAYER_KEY FROM PLAYER P 
     WHERE P.PLAYER_ID = '%s' AND P.HASHED_PW = '%s'
   
   - 세션 토큰 생성 및 업데이트
     UPDATE PLAYER SET SESSION_TOKEN = '%s', 
                      LAST_ACTIVITY = TO_DATE('%s', 'YYYY-MM-DD HH24:MI:SS') 
     WHERE PLAYER_ID = '%s'

   오류 처리
   - 로그인 실패 → "계정이 존재하지 않습니다"


[ 1-4. 로그인 후 메인 메뉴 ]

   [2] 월드 레코드 조회
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

   게임 세션 확인 및 생성

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

   전시 중인 아이템 목록 조회
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

   전시 아이템 상세 정보 조회
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

   진행 중인 거래 조회
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

   거래가 없을 때 → 정산 진행

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

    TODAY_FINAL < 0 이면 게임 오버

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

    TODAY_FINAL < 0 이면 게임 오버


[ 1-9. 다음 날 진행 ]

   DAY_COUNT 증가
   UPDATE GAME_SESSION SET DAY_COUNT = DAY_COUNT + 1 
   WHERE GAME_SESSION_KEY = (
       SELECT GAME_SESSION_KEY FROM GAME_SESSION 
       WHERE PLAYER_KEY = (
           SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'
       ) ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
   )

   업데이트된 게임 세션 조회
   SELECT * FROM GAME_SESSION WHERE PLAYER_KEY = %d 
   ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY


[ 1-10. 일일 거래 3개 생성 ]

   게임 세션 키 조회
   SELECT GAME_SESSION_KEY FROM GAME_SESSION 
   WHERE PLAYER_KEY = (SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s') 
   ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY

   현재 이벤트 조회
   SELECT * FROM EXISTING_NEWS N, NEWS_CATALOG NC 
   WHERE N.GAME_SESSION_KEY = (
       SELECT GAME_SESSION_KEY FROM GAME_SESSION 
       WHERE PLAYER_KEY = %d 
       ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
   ) AND N.NCAT_KEY = NC.NCT_KEY 
   ORDER BY NC.NCT_KEY

   랜덤 고객 3명 선택
   SELECT CUSTOMER_KEY, FRAUD, WELL_COLLECT, CLUMSY, CATEGORY_KEY 
   FROM CUSTOMER_CATALOG 
   ORDER BY DBMS_RANDOM.VALUE 
   FETCH FIRST %d ROWS ONLY

   각 고객별 거래 생성
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

   첫 번째 거래 정보 가져오기
   SELECT DR.* FROM DEAL_RECORD DR, EXISTING_ITEM I 
   WHERE DR.GAME_SESSION_KEY = (
       SELECT GAME_SESSION_KEY FROM GAME_SESSION 
       WHERE PLAYER_KEY = %d 
       ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
   ) AND DR.ITEM_KEY = I.ITEM_KEY 
     AND I.ITEM_STATE = %d 
   ORDER BY DR.DRC_KEY

   거래 상세 정보 조회
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

   [1] 아이템 힌트 얻기 (10 골드)
   
   1) 잔액 확인
      SELECT MONEY FROM GAME_SESSION WHERE GAME_SESSION_KEY = (
          SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = (
              SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'
          ) ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
      )
    잔액 < 10 이면 실패

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
    손님의 여러 정보를 통해 아이템 정보를 계산하여 사용자가 원하는 힌트 출력
    6개의 힌트 중 랜덤하게 하나를 골라서 제공

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


   [2] 등급 검사 (20/30/50 골드)
   
   검사 레벨: 1 = 20골드, 2 = 30골드, 3 = 50골드
   
   1) 잔액 조회 후 비용 검증
      ※ 잔액 < 비용이면 실패

   2) 아이템 등급 정보 조회
      SELECT I.*, DR.* FROM EXISTING_ITEM I, DEAL_RECORD DR 
      WHERE DR.DRC_KEY = %d AND DR.ITEM_KEY = I.ITEM_KEY
      ※ 현재까지 밝혀진 아이템의 등급 출력


   [3] 흠 찾기 (20/60/100 골드)
   
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


   [4] 정가품 판정 (200 골드)
   
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

   4) 거래 기록 업데이트 (정가품 판정 후 가격 변경)
      UPDATE DEAL_RECORD SET PURCHASE_PRICE = %d, APPRAISED_PRICE = %d 
      WHERE DRC_KEY = %d

   5) 잔액 차감 및 조회


   [5] 손님 힌트 조회 (50 골드)
   
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
    사용자가 선택한 힌트 정보 제공

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

   [6] 거래 수락 (구매)
   
   1) 현재 거래 정보 및 잔액 조회
      SELECT I.*, DR.* FROM EXISTING_ITEM I, DEAL_RECORD DR 
      WHERE DR.DRC_KEY = %d AND DR.ITEM_KEY = I.ITEM_KEY
      
      SELECT MONEY FROM GAME_SESSION WHERE GAME_SESSION_KEY = (
          SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = (
              SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'
          ) ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY
      )
    현재 잔액으로 구매 가능 여부 체크 <<

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


   [7] 거래 거절 (삭제하기)
   
   1) 거래 기록 삭제
      DELETE FROM DEAL_RECORD WHERE DRC_KEY = %d

   2) 아이템 삭제
      DELETE FROM EXISTING_ITEM WHERE ITEM_KEY = %d


[ 1-13. 빚 관리 및 대출 ]

   세션 정보 조회
   SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'
   
   SELECT * FROM GAME_SESSION WHERE PLAYER_KEY = %d 
   ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY


   [1] 개인 빚 상환
   
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
      
    둘 다 0이면 게임 즉시 종료 (클리어) <<


   [2] 가게 빚 상환 및 대출
   
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
      
    둘 다 0이면 게임 즉시 종료 (클리어) <<

[ 1-14. 게임 클리어 ]

   클리어한 게임 요약 조회
   SELECT GS.NICKNAME, GS.SHOP_NAME, GS.GAME_END_DAY_COUNT, GS.GAME_END_DATE 
   FROM PLAYER P, GAME_SESSION GS 
   WHERE P.SESSION_TOKEN = '%s' AND P.PLAYER_KEY = GS.PLAYER_KEY 
   ORDER BY GS.GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY

   미발견 아이템 리스트 출력
   (SELECT IC.ITEM_CATALOG_NAME FROM ITEM_CATALOG IC) 
   MINUS 
   (SELECT IC.ITEM_CATALOG_NAME 
    FROM GAME_SESSION G, EXISTING_ITEM I, ITEM_CATALOG IC 
    WHERE G.PLAYER_KEY = %d AND G.GAME_SESSION_KEY = I.GAME_SESSION_KEY 
      AND I.ITEM_CATALOG_KEY = IC.ITEM_CATALOG_KEY)


[ 1-15. 게임 오버 ]

   게임 오버 요약 조회
   SELECT GS.NICKNAME, GS.SHOP_NAME, GS.GAME_END_DAY_COUNT, GS.GAME_END_DATE 
   FROM PLAYER P, GAME_SESSION GS 
   WHERE P.SESSION_TOKEN = '%s' AND P.PLAYER_KEY = GS.PLAYER_KEY 
   ORDER BY GS.GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY

   미발견 아이템 리스트 출력
   (SELECT IC.ITEM_CATALOG_NAME FROM ITEM_CATALOG IC) 
   MINUS 
   (SELECT IC.ITEM_CATALOG_NAME 
    FROM GAME_SESSION G, EXISTING_ITEM I, ITEM_CATALOG IC 
    WHERE G.PLAYER_KEY = %d AND G.GAME_SESSION_KEY = I.GAME_SESSION_KEY 
      AND I.ITEM_CATALOG_KEY = IC.ITEM_CATALOG_KEY)

   게임 종료 처리
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
    GAME_END_DAY_COUNT는 DAY_COUNT의 음수 값 <<
    
----------------------------------------------------------------------------------


===================================================================================

실행 예시

===================================================================================

################################################################################
#                               전당포 운영 게임                               #
################################################################################
1. 로그인
2. 회원가입
0. 게임 종료
선택 (0~2): 2
ID (영문 최대 30글자): 
사용자 이름이 입력되지 않았습니다. 메인 메뉴로 돌아갑니다...
################################################################################
#                               전당포 운영 게임                               #
################################################################################
1. 로그인
2. 회원가입
0. 게임 종료
선택 (0~2): 1
ID: tt
PW: 
올바르지 않은 비밀번호입니다.
################################################################################
#                               전당포 운영 게임                               #
################################################################################
1. 로그인
2. 회원가입
0. 게임 종료
선택 (0~2): 1
ID: ts
PW: 1234
계정이 존재하지 않습니다.
################################################################################
#                               전당포 운영 게임                               #
################################################################################
1. 로그인
2. 회원가입
0. 게임 종료
선택 (0~2): 2
ID (영문 최대 30글자): tss
이미 존재하는 사용자명입니다.
ID (영문 최대 30글자): 
사용자 이름이 입력되지 않았습니다. 메인 메뉴로 돌아갑니다...
################################################################################
#                               전당포 운영 게임                               #
################################################################################
1. 로그인
2. 회원가입
0. 게임 종료
선택 (0~2): 2
ID (영문 최대 30글자): uid
사용할 수 있는 사용자명입니다.
PW: 1234
회원가입에 성공하였습니다. 로그인 기능으로 이동합니다.
################################################################################
#                               전당포 운영 게임                               #
################################################################################
1. 로그인
2. 회원가입
0. 게임 종료
선택 (0~2): 1
ID: uid
PW: 1234
로그인에 성공하였습니다.
################################################################################
#                               전당포 운영 게임                               #
################################################################################
1. 게임 시작
2. 월드 레코드
3. 로그아웃
0. 게임 종료
선택 (0~3): 2
플레이어명 | 닉네임 | 가게 이름 | 게임 진행한 날짜 | 게임 끝난 날
user031 닉네임179  상점179            10 2025-10-30
user171 닉네임119  상점119            10 2025-10-30
user142  닉네임90   상점90            10 2025-10-30
user062  닉네임10   상점10            12 2025-10-30
user073  닉네임21   상점21            12 2025-10-30
user039 닉네임187  상점187            13 2025-10-30
user190 닉네임138  상점138            13 2025-10-30
user079  닉네임27   상점27            13 2025-10-30
user123  닉네임71   상점71            14 2025-10-30
user128  닉네임76   상점76            14 2025-10-30
################################################################################
#                               전당포 운영 게임                               #
################################################################################
1. 게임 시작
2. 월드 레코드
3. 로그아웃
0. 게임 종료
선택 (0~3): 1
################################################################################
#                            게임 세션 가져오기 요청                           #
################################################################################
진행 중인 게임 세션이 있는지 확인하고, 있다면 불러와야 합니다.
1. 게임 세션 가져오기
선택 (1~1): 1

진행 중인 게임 세션이 없습니다.

계속하려면 Enter를 누르세요...

################################################################################
#                            새 게임 세션 생성 요청                            #
################################################################################
진행 중인 게임이 없습니다. 새 게임을 시작해야 합니다.
1. 새 게임 세션 생성
선택 (1~1): 1
닉네임을 입력하세요 (최대 10글자): 
닉네임이 입력되지 않았습니다.
닉네임을 입력하세요 (최대 10글자): d
상점 이름을 입력하세요 (최대 10글자): shop

=== 새 게임 세션 생성 완료 ===
닉네임: d
상점명: shop
시작 일수: 1일
시작 잔액: 50,000G
개인 빚: 500,000G
전당포 빚: 0G
===========================

계속하려면 Enter를 누르세요...

################################################################################
#                        전시 중인 아이템 가져오기 요청                        #
################################################################################
게임 진행을 위해서는 전시 중인 아이템을 가져와야 합니다.
1. 전시 중인 아이템 가져오기
선택 (1~1): 1
전시 중인 아이템이 없습니다.
계속하려면 Enter를 누르세요...

################################################################################
#                         대기 중인 거래 기록 확인 요청                        #
################################################################################
남은 거래가 있는지 확인해야 합니다. 남은 거래가 있다면, 그 거래를 해야 합니다.
1. 남은 거래 있는지 확인하기
선택 (1~1): 1

=== 대기 중인 거래 ===
대기 중인 거래 수: 0개
====================

계속하려면 Enter를 누르세요...

################################################################################
#                                 경매 정산하기                                #
################################################################################
1. 정산하기
0. 게임 종료
선택 (0~1): 1
################################################################################
#                                복원 완료 처리                                #
################################################################################
1. 처리하기
0. 게임 종료
선택 (0~1): 1

=== 오늘의 거래 생성 ===
현재 적용 중인 이벤트 없음
고객 3명 선택 완료
1번 거래 생성: 도자기 컵 (등급: 레전더리, 흠: 4개, 가품, 제시가: 7,478G)
2번 거래 생성: 입체주의 그림 (등급: 레전더리, 흠: 3개, 가품, 제시가: 7,894G)
3번 거래 생성: 청동상 (등급: 유니크, 흠: 13개, 가품, 제시가: 4,242G)

총 3개의 거래가 생성되었습니다!
======================

계속하려면 Enter를 누르세요...

################################################################################
#                         대기 중인 거래 기록 확인 요청                        #
################################################################################
남은 거래가 있는지 확인해야 합니다. 남은 거래가 있다면, 그 거래를 해야 합니다.
1. 남은 거래 있는지 확인하기
선택 (1~1): 1

=== 대기 중인 거래 ===
대기 중인 거래 수: 3개
====================

계속하려면 Enter를 누르세요...

고객이 선호하는 Item이 진열되어 있지 않습니다.
################################################################################
#                                   메인 게임                                  #
################################################################################
1. 거래 (재개)하기
2. 빛 상환 / 아이템 처리
3. 게임 포기하기
0. 게임 종료
선택 (0~3): 1
################################################################################
#                                  고객과 거래                                 #
################################################################################
=====================================
고객: 위쟁반 (CIM00010)
아이템: 도자기 컵
발견 등급: 일반
발견 흠: 0개
진위 판정: 미확인

최초 제시가: 7,478G
현재 구매가: 7,478G
현재 감정가: 7,478G

---공개된 고객 힌트---
(공개된 힌트 없음)
(공개된 힌트 없음)

현재 잔액: 50,000G
=====================================
1. 물건 조사
2. 등급 감정
3. 흠 찾기
4. 진위 판정 [200G 소모]
5. 새 고객 힌트 열기
6. 거래 수락
7. 거래 거절
8. 거래 일시정지, 메인으로 돌아가기
0. 게임 종료
선택 (0~8): 1
################################################################################
#                                   물건 조사                                  #
################################################################################
1. 물건 힌트 1개 열기 [10G 소모]
2. 이전 화면으로 돌아가기
0. 게임 종료
선택 (0~2): 1

[물건 힌트]
힌트: 흠이 있을 거 같은 느낌
값: 0.35

잔액: 50,000G -> 49,990G (-10G)

Enter를 눌러 계속...

################################################################################
#                                  고객과 거래                                 #
################################################################################
=====================================
고객: 위쟁반 (CIM00010)
아이템: 도자기 컵
발견 등급: 일반
발견 흠: 0개
진위 판정: 미확인

최초 제시가: 7,478G
현재 구매가: 7,478G
현재 감정가: 7,478G

---공개된 고객 힌트---
(공개된 힌트 없음)
(공개된 힌트 없음)

현재 잔액: 49,990G
=====================================
1. 물건 조사
2. 등급 감정
3. 흠 찾기
4. 진위 판정 [200G 소모]
5. 새 고객 힌트 열기
6. 거래 수락
7. 거래 거절
8. 거래 일시정지, 메인으로 돌아가기
0. 게임 종료
선택 (0~8): 1
################################################################################
#                                   물건 조사                                  #
################################################################################
1. 물건 힌트 1개 열기 [10G 소모]
2. 이전 화면으로 돌아가기
0. 게임 종료
선택 (0~2): 1

[물건 힌트]
힌트: 레어 확률
값: 33.98%

잔액: 49,990G -> 49,980G (-10G)

Enter를 눌러 계속...

################################################################################
#                                  고객과 거래                                 #
################################################################################
=====================================
고객: 위쟁반 (CIM00010)
아이템: 도자기 컵
발견 등급: 일반
발견 흠: 0개
진위 판정: 미확인

최초 제시가: 7,478G
현재 구매가: 7,478G
현재 감정가: 7,478G

---공개된 고객 힌트---
(공개된 힌트 없음)
(공개된 힌트 없음)

현재 잔액: 49,980G
=====================================
1. 물건 조사
2. 등급 감정
3. 흠 찾기
4. 진위 판정 [200G 소모]
5. 새 고객 힌트 열기
6. 거래 수락
7. 거래 거절
8. 거래 일시정지, 메인으로 돌아가기
0. 게임 종료
선택 (0~8): 2
################################################################################
#                                   등급 감정                                  #
################################################################################
1. 레어 감정 [20G 소모]
2. 유니크 감정 [30G 소모]
3. 레전더리 감정 [50G 소모]
4. 이전 화면으로 돌아가기
0. 게임 종료
선택 (0~4): 3

[등급 감정 결과]
발견 등급: 유니크
감정가 변경: 7,478G -> 11,217G
잔액: 49,980G -> 49,930G (-50G)

Enter를 눌러 계속...

################################################################################
#                                  고객과 거래                                 #
################################################################################
=====================================
고객: 위쟁반 (CIM00010)
아이템: 도자기 컵
발견 등급: 유니크
발견 흠: 0개
진위 판정: 미확인

최초 제시가: 7,478G
현재 구매가: 7,478G
현재 감정가: 11,217G

---공개된 고객 힌트---
(공개된 힌트 없음)
(공개된 힌트 없음)

현재 잔액: 49,930G
=====================================
1. 물건 조사
2. 등급 감정
3. 흠 찾기
4. 진위 판정 [200G 소모]
5. 새 고객 힌트 열기
6. 거래 수락
7. 거래 거절
8. 거래 일시정지, 메인으로 돌아가기
0. 게임 종료
선택 (0~8): 3
################################################################################
#                                    흠 찾기                                   #
################################################################################
1. 하급 흠 찾기 [20G 소모]
2. 중급 흠 찾기 [60G 소모]
3. 고급 흠 찾기 [100G 소모]
4. 이전 화면으로 돌아가기
0. 게임 종료
선택 (0~4): 3

[흠 찾기 결과]
발견한 흠: 4개
총 발견 흠: 4개
구매가 변경: 7,478G -> 5,982G
감정가 변경: 11,217G -> 8,973G
잔액: 49,930G -> 49,830G (-100G)

Enter를 눌러 계속...

################################################################################
#                                  고객과 거래                                 #
################################################################################
=====================================
고객: 위쟁반 (CIM00010)
아이템: 도자기 컵
발견 등급: 유니크
발견 흠: 4개
진위 판정: 미확인

최초 제시가: 7,478G
현재 구매가: 5,982G
현재 감정가: 8,973G

---공개된 고객 힌트---
(공개된 힌트 없음)
(공개된 힌트 없음)

현재 잔액: 49,830G
=====================================
1. 물건 조사
2. 등급 감정
3. 흠 찾기
4. 진위 판정 [200G 소모]
5. 새 고객 힌트 열기
6. 거래 수락
7. 거래 거절
8. 거래 일시정지, 메인으로 돌아가기
0. 게임 종료
선택 (0~8): 4

[진위 판정 결과]
결과: 가품
가품이므로 구매가 50%, 감정가 20% 감소합니다.
구매가 변경: 5,982G -> 2,991G (-2,991G)
감정가 변경: 8,973G -> 7,178G (-1,795G)
잔액: 49,830G -> 49,630G (-200G)

Enter를 눌러 계속...

################################################################################
#                                  고객과 거래                                 #
################################################################################
=====================================
고객: 위쟁반 (CIM00010)
아이템: 도자기 컵
발견 등급: 유니크
발견 흠: 4개
진위 판정: 가품 (확정)

최초 제시가: 7,478G
현재 구매가: 2,991G
현재 감정가: 7,178G

---공개된 고객 힌트---
(공개된 힌트 없음)
(공개된 힌트 없음)

현재 잔액: 49,630G
=====================================
1. 물건 조사
2. 등급 감정
3. 흠 찾기
4. 진위 판정 [200G 소모]
5. 새 고객 힌트 열기
6. 거래 수락
7. 거래 거절
8. 거래 일시정지, 메인으로 돌아가기
0. 게임 종료
선택 (0~8): 5
################################################################################
#                               새 고객 힌트 열기                              #
################################################################################
1. '사기칠 거 같은 비율' 힌트 열기 [50G 소모]
2. '수집가 능력' 힌트 열기 [50G 소모]
3. '대충 관리함' 힌트 열기 [50G 소모]
4. 이전 화면으로 돌아가기
0. 게임 종료
선택 (0~4): 1

[고객 힌트]
고객: 위쟁반
힌트: 사기칠 거 같은 비율 (FRAUD)
값: 21.49%
잔액: 49,630G -> 49,580G (-50G)

Enter를 눌러 계속...

################################################################################
#                                  고객과 거래                                 #
################################################################################
=====================================
고객: 위쟁반 (CIM00010)
아이템: 도자기 컵
발견 등급: 유니크
발견 흠: 4개
진위 판정: 가품 (확정)

최초 제시가: 7,478G
현재 구매가: 2,991G
현재 감정가: 7,178G

---공개된 고객 힌트---
사기칠 거 같은 비율: 89.58%

현재 잔액: 49,580G
=====================================
1. 물건 조사
2. 등급 감정
3. 흠 찾기
4. 진위 판정 [200G 소모]
5. 새 고객 힌트 열기
6. 거래 수락
7. 거래 거절
8. 거래 일시정지, 메인으로 돌아가기
0. 게임 종료
선택 (0~8): 6
################################################################################
#                               구매 전 잔고 확인                              #
################################################################################
구매가: 2,991G
현재 잔액: 49,580G

구매 가능 여부: 가능
1. 잔고 확인
선택 (1~1): 1
################################################################################
#                                구매 기록 저장                                #
################################################################################
구매를 확정하고 전시대 0번 위치에 배치합니다.
1. 저장
선택 (1~1): 1

구매가 완료되었습니다!
전시 위치: 0번
잔액: 49,580G -> 46,589G

Enter를 눌러 계속...

################################################################################
#                         대기 중인 거래 기록 확인 요청                        #
################################################################################
남은 거래가 있는지 확인해야 합니다. 남은 거래가 있다면, 그 거래를 해야 합니다.
1. 남은 거래 있는지 확인하기
선택 (1~1): 1

=== 대기 중인 거래 ===
대기 중인 거래 수: 2개
====================

계속하려면 Enter를 누르세요...

고객이 선호하는 Item이 진열되어 있지 않습니다.
################################################################################
#                                   메인 게임                                  #
################################################################################
1. 거래 (재개)하기
2. 빛 상환 / 아이템 처리
3. 게임 포기하기
0. 게임 종료
선택 (0~3): 1
################################################################################
#                                  고객과 거래                                 #
################################################################################
=====================================
고객: 양베개 (CIM00012)
아이템: 입체주의 그림
발견 등급: 일반
발견 흠: 0개
진위 판정: 미확인

최초 제시가: 7,894G
현재 구매가: 7,894G
현재 감정가: 7,894G

---공개된 고객 힌트---
(공개된 힌트 없음)
(공개된 힌트 없음)

현재 잔액: 46,589G
=====================================
1. 물건 조사
2. 등급 감정
3. 흠 찾기
4. 진위 판정 [200G 소모]
5. 새 고객 힌트 열기
6. 거래 수락
7. 거래 거절
8. 거래 일시정지, 메인으로 돌아가기
0. 게임 종료
선택 (0~8): 6
################################################################################
#                               구매 전 잔고 확인                              #
################################################################################
구매가: 7,894G
현재 잔액: 46,589G

구매 가능 여부: 가능
1. 잔고 확인
선택 (1~1): 1
################################################################################
#                                구매 기록 저장                                #
################################################################################
구매를 확정하고 전시대 1번 위치에 배치합니다.
1. 저장
선택 (1~1): 1

구매가 완료되었습니다!
전시 위치: 1번
잔액: 46,589G -> 38,695G

Enter를 눌러 계속...

################################################################################
#                         대기 중인 거래 기록 확인 요청                        #
################################################################################
남은 거래가 있는지 확인해야 합니다. 남은 거래가 있다면, 그 거래를 해야 합니다.
1. 남은 거래 있는지 확인하기
선택 (1~1): 1

=== 대기 중인 거래 ===
대기 중인 거래 수: 1개
====================

계속하려면 Enter를 누르세요...

고객이 선호하는 Item이 진열되어 있지 않습니다.
################################################################################
#                                   메인 게임                                  #
################################################################################
1. 거래 (재개)하기
2. 빛 상환 / 아이템 처리
3. 게임 포기하기
0. 게임 종료
선택 (0~3): 1
################################################################################
#                                  고객과 거래                                 #
################################################################################
=====================================
고객: 변라벨 (CIM00016)
아이템: 청동상
발견 등급: 일반
발견 흠: 0개
진위 판정: 미확인

최초 제시가: 4,242G
현재 구매가: 4,242G
현재 감정가: 4,242G

---공개된 고객 힌트---
(공개된 힌트 없음)
(공개된 힌트 없음)

현재 잔액: 38,695G
=====================================
1. 물건 조사
2. 등급 감정
3. 흠 찾기
4. 진위 판정 [200G 소모]
5. 새 고객 힌트 열기
6. 거래 수락
7. 거래 거절
8. 거래 일시정지, 메인으로 돌아가기
0. 게임 종료
선택 (0~8): 8
################################################################################
#                         대기 중인 거래 기록 확인 요청                        #
################################################################################
남은 거래가 있는지 확인해야 합니다. 남은 거래가 있다면, 그 거래를 해야 합니다.
1. 남은 거래 있는지 확인하기
선택 (1~1): 1

=== 대기 중인 거래 ===
대기 중인 거래 수: 1개
====================

계속하려면 Enter를 누르세요...

고객이 선호하는 Item이 진열되어 있지 않습니다.
################################################################################
#                                   메인 게임                                  #
################################################################################
1. 거래 (재개)하기
2. 빛 상환 / 아이템 처리
3. 게임 포기하기
0. 게임 종료
선택 (0~3): 2
################################################################################
#                        빚 대출/상환 & 아이템 경매/복원                       #
################################################################################
재산: 38695 | 개인 빚: 500000 | 가게 빛: 0
1. 개인 빚 상환
2. 가게 빚 대출/상환
3. 아이템 경매
4. 아이템 복원
5. 이전 화면으로 돌아가기
0. 게임 종료
선택 (0~5): 1
################################################################################
#                                 개인 빚 상환                                 #
################################################################################
재산: 38695 | 빚: 500000
1. 상환: 2000
2. 상환: 1000
3. 상환: 500
4. 상환: 100
5. 취소
0. 게임 종료
선택 (0~5): 1
################################################################################
#                               개인 빚 상환 저장                              #
################################################################################
1. 개인 빚 상환 실행
선택 (1~1): 1
################################################################################
#                                   재산 저장                                  #
################################################################################
1. 재산 저장
선택 (1~1): 1
################################################################################
#                               가게 빚 잔액 확인                              #
################################################################################
개인 빚과 가게 빚이 모두 남아있지 않다면, 게임을 즉시 클리어한 것으로 처리됩니다.
1. 가게 빚 확인
선택 (1~1): 1
################################################################################
#                               개인 빚 잔액 확인                              #
################################################################################
가게 빚은 모두 갚으신 것 같습니다. 개인 빚도 확인해야 합니다.
1. 개인 빚 확인
선택 (1~1): 1
아직 개인 빚이 있습니다.
################################################################################
#                        빚 대출/상환 & 아이템 경매/복원                       #
################################################################################
재산: 36695 | 개인 빚: 498000 | 가게 빛: 0
1. 개인 빚 상환
2. 가게 빚 대출/상환
3. 아이템 경매
4. 아이템 복원
5. 이전 화면으로 돌아가기
0. 게임 종료
선택 (0~5): 2
################################################################################
#                                 가게 빚 상환                                 #
################################################################################
재산: 36695 | 빚: 0
1. 상환: 2000
2. 상환: 1000
3. 상환: 500
4. 상환: 100
5. 대출: 2000
6. 대출: 1000
7. 대출: 500
8. 대출: 100
9. 취소
0. 게임 종료
선택 (0~9): 5
################################################################################
#                               가게 빚 상환 저장                              #
################################################################################
1. 가게 빚 대출/상환 실행
선택 (1~1): 1
################################################################################
#                                   재산 저장                                  #
################################################################################
1. 재산 저장
선택 (1~1): 1
################################################################################
#                        빚 대출/상환 & 아이템 경매/복원                       #
################################################################################
재산: 38695 | 개인 빚: 498000 | 가게 빛: 2000
1. 개인 빚 상환
2. 가게 빚 대출/상환
3. 아이템 경매
4. 아이템 복원
5. 이전 화면으로 돌아가기
0. 게임 종료
선택 (0~5): 2
################################################################################
#                                 가게 빚 상환                                 #
################################################################################
재산: 38695 | 빚: 2000
1. 상환: 2000
2. 상환: 1000
3. 상환: 500
4. 상환: 100
5. 대출: 2000
6. 대출: 1000
7. 대출: 500
8. 대출: 100
9. 취소
0. 게임 종료
선택 (0~9): 3
################################################################################
#                               가게 빚 상환 저장                              #
################################################################################
1. 가게 빚 대출/상환 실행
선택 (1~1): 1
################################################################################
#                                   재산 저장                                  #
################################################################################
1. 재산 저장
선택 (1~1): 1
################################################################################
#                               가게 빚 잔액 확인                              #
################################################################################
개인 빚과 가게 빚이 모두 남아있지 않다면, 게임을 즉시 클리어한 것으로 처리됩니다.
1. 가게 빚 확인
선택 (1~1): 1
아직 가게 빚이 있습니다.
################################################################################
#                        빚 대출/상환 & 아이템 경매/복원                       #
################################################################################
재산: 38195 | 개인 빚: 498000 | 가게 빛: 1500
1. 개인 빚 상환
2. 가게 빚 대출/상환
3. 아이템 경매
4. 아이템 복원
5. 이전 화면으로 돌아가기
0. 게임 종료
선택 (0~5): 3
################################################################################
#                                  아이템 경매                                 #
################################################################################
가게 빚은 모두 갚으신 것 같습니다. 개인 빚도 확인해야 합니다.
1. 0번 인덱스: 도자기 컵
2. 1번 인덱스: 입체주의 그림
3. -
4. -
5. -
6. -
7. -
8. -
9. 취소
0. 게임 종료
선택 (0~9): 1
################################################################################
#                                  아이템 경매                                 #
################################################################################
1. 아이템 상태를 경매 중으로 변경
0. 게임 종료
선택 (0~1): 1
################################################################################
#                        빚 대출/상환 & 아이템 경매/복원                       #
################################################################################
재산: 38195 | 개인 빚: 498000 | 가게 빛: 1500
1. 개인 빚 상환
2. 가게 빚 대출/상환
3. 아이템 경매
4. 아이템 복원
5. 이전 화면으로 돌아가기
0. 게임 종료
선택 (0~5): 4
################################################################################
#                                  아이템 복원                                 #
################################################################################
가게 빚은 모두 갚으신 것 같습니다. 개인 빚도 확인해야 합니다.
1. -
2. 1번 인덱스: 입체주의 그림
3. -
4. -
5. -
6. -
7. -
8. -
9. 취소
0. 게임 종료
선택 (0~9): 2
################################################################################
#                                  아이템 복원                                 #
################################################################################
1. 아이템 상태를 복원 중으로 변경
0. 게임 종료
선택 (0~1): 1
################################################################################
#                        빚 대출/상환 & 아이템 경매/복원                       #
################################################################################
재산: 38195 | 개인 빚: 498000 | 가게 빛: 1500
1. 개인 빚 상환
2. 가게 빚 대출/상환
3. 아이템 경매
4. 아이템 복원
5. 이전 화면으로 돌아가기
0. 게임 종료
선택 (0~5): 5
################################################################################
#                         대기 중인 거래 기록 확인 요청                        #
################################################################################
남은 거래가 있는지 확인해야 합니다. 남은 거래가 있다면, 그 거래를 해야 합니다.
1. 남은 거래 있는지 확인하기
선택 (1~1): 1

=== 대기 중인 거래 ===
대기 중인 거래 수: 1개
====================

계속하려면 Enter를 누르세요...

고객이 선호하는 Item이 진열되어 있지 않습니다.
################################################################################
#                                   메인 게임                                  #
################################################################################
1. 거래 (재개)하기
2. 빛 상환 / 아이템 처리
3. 게임 포기하기
0. 게임 종료
선택 (0~3): 3
***게임 오버***
게임을 포기하셨습니다...
################################################################################
#                            현재 게임 결과 가져오기                           #
################################################################################
게임 결과 출력을 위해서, 현재 게임 결과를 가져와야 합니다.
1. 가져오기
선택 (1~1): 1
################################################################################
#                         발견하지 못한 아이템 가져오기                        #
################################################################################
게임 결과 출력을 위해서, 발견하지 못한 아이템들을 가져와야 합니다.
1. 가져오기
선택 (1~1): 1
          닉네임: d
       가게 이름: shop
게임 진행한 날짜: 0
    게임 끝난 날: null
현재까지 발견하지 못한 아이템
  SLR 카메라
  가죽 갑옷
  고급 유리 병
  고급시계
  고대 금화
  고대 서책
  고대 투구
  고전 카메라
  고풍 시계
  관광 메달
  구리 램프
  궁수의 활
  금실 자수옷
  기념 엽서
  기념 주화
  기병상
  기사 장화
  기어
  기타
  나무 항아리
  나무시계
  다이아 반지
  단검
  대리석 조각상
  도자기 병
  동전 묶음
  레이피어
  렌즈 세트
  롱소드
  루비 반지
  면 의복
  목재 책상
  바이올린
  백자 찻잔
  벽시계
  부처상
  분청 접시
  비단 옷
  사무라이 검
  사찰 경전
  사파이어 목걸이
  서랍장
  서예 족자
  석고상
  시계
  쌍검
  앤틱 의자
  에메랄드 귀걸이
  역사서
  왕관
  왕실 예복
  왕실 일지
  왕실 초상화
  왕조 화폐
  원목 식탁
  유물 단지
  유물 돌조각
  유약 그릇
  유적 토기
  유화 풍경화
  은 갑옷
  은제 숟가락
  은제 접시
  은화 세트
  의식용 단검
  인물화
  장검
  장식장
  전통 두루마기
  전투 도끼
  정물화 액자
  증기 엔진
  진주 브로치
  철제 갑옷
  철제 컵
  철제 트로피
  철퇴
  청동 거울
  청동 촛대
  청동 헬멧
  청자 항아리
  추상화 그림
  축제 열쇠고리
  축하 장식품
  태엽 장치
  파스텔 풍경화
  폴라로이드
  풍경화 그림
  플루트
  피아노
  필름 카메라
  하프
  학자 노트
  현대화 작품
  황동 항아리
  회전 기계
  회중시계
################################################################################
#                              게임 패배 기록하기                              #
################################################################################
1. 기록하기
0. 게임 종료
선택 (0~1): 1
인트로 화면으로 돌아가시려면 Enter를 누르세요...

################################################################################
#                               전당포 운영 게임                               #
################################################################################
1. 게임 시작
2. 월드 레코드
3. 로그아웃
0. 게임 종료
선택 (0~3): 3
################################################################################
#                               전당포 운영 게임                               #
################################################################################
1. 로그인
2. 회원가입
0. 게임 종료
선택 (0~2): 0
Exiting...

