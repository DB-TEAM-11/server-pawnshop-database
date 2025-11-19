README.txt
전당포 운영 게임 - Phase 3 - Team 11 Project

  목차
    1. 게임 개요
    2. 프로그램 실행 가이드
    3. 데이터베이스 쿼리 설명
    4. 시스템 아키텍처
    5. Phase 2 대비 변경사항 및 사용된 쿼리 10개

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

4. 시스템

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







