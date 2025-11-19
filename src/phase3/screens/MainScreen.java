package phase3.screens;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

import phase3.PlayerSession;
import phase3.constants.ItemState;
import phase3.exceptions.CloseGameException;
import phase3.exceptions.NotASuchRowException;
import phase3.queries.*;

public class MainScreen extends BaseScreen {
    private static final String TITLE_LOAD_PLAYING_GAME_SESSION_REQUEST = "게임 세션 가져오기 요청";
    private static final String TITLE_CREATE_GAME_SESSION_REQUEST = "새 게임 세션 생성 요청";
    private static final String TITLE_DIPLAY_ITEM_REQUEST = "전시 중인 아이템 가져오기 요청";
    private static final String TITLE_CHECK_REMAING_DEAL_REQUEST = "대기 중인 거래 기록 확인 요청";
    private static final String TITLE_CHECK_IS_END_OF_WEEK_REQUEST = "7의 배수 일자인지 확인 요청";
    private static final String TITLE_FINALIZE_DAY = "일일 정산 요청";
    private static final String TITLE_FINALIZE_WEEK = "주간 정산 요청";
    private static final String TITLE_NEXT_DAY_REQUEST = "다음 날로 진행 요청";
    private static final String TITLE_MAIN_MENU = "메인 게임";
    private static final String TITLE_GET_CURRENT_GAME_SUMMARY = "현재 게임 결과 가져오기";
    private static final String TITLE_GET_NOT_FOUND_ITEM = "발견하지 못한 아이템 가져오기";
    private static final String TITLE_RECORD_GAME_CLEAR = "게임 클리어 기록하기";
    private static final String TITLE_RECORD_GAME_DEFEAT = "게임 패배 기록하기";
    private static final String TITLE_FINISH_AUCTION = "경매 정산하기";
    private static final String TITLE_FINISH_RECOVER = "복원 완료 처리";
    
    private static final String MESSAGE_LOAD_PLAYING_GAME_SESSION_REQUEST = "진행 중인 게임 세션이 있는지 확인하고, 있다면 불러와야 합니다.";
    private static final String MESSAGE_CREATE_GAME_SESSION_REQUEST = "진행 중인 게임이 없습니다. 새 게임을 시작해야 합니다.";
    private static final String MESSAGE_DIPLAY_ITEM_REQUEST = "게임 진행을 위해서는 전시 중인 아이템을 가져와야 합니다.";
    private static final String MESSAGE_CHECK_REMAING_DEAL_REQUEST = "남은 거래가 있는지 확인해야 합니다. 남은 거래가 있다면, 그 거래를 해야 합니다.";
    private static final String MESSAGE_FINALIZE_DAY = "하루가 모두 끝났습니다. 다음 날로 넘어가기 전에, 일일 정산을 수행해야 합니다.";
    private static final String MESSAGE_FINALIZE_WEEK = "한 주가 모두 끝났습니다. 다음 주로 넘어가기 전에, 주간 정산을 수행해야 합니다.";
    private static final String MESSAGE_NEXT_DAY_REQUEST = "계속 진행하기 위해서는, 다음 날로 넘어가야 합니다.";
    private static final String MESSAGE_GET_CURRENT_GAME_SUMMARY = "게임 결과 출력을 위해서, 현재 게임 결과를 가져와야 합니다.";
    private static final String MESSAGE_GET_NOT_FOUND_ITEM = "게임 결과 출력을 위해서, 발견하지 못한 아이템들을 가져와야 합니다.";
    
    private static final String[] CHOICES_LOAD_PLAYING_GAME_SESSION_REQUEST = { "게임 세션 가져오기" };
    private static final String[] CHOICES_CREATE_GAME_SESSION_REQUEST = { "새 게임 세션 생성" };
    private static final String[] CHOICES_DIPLAY_ITEM_REQUEST = { "전시 중인 아이템 가져오기" };
    private static final String[] CHOICES_CHECK_REMAING_DEAL_REQUEST = { "남은 거래 있는지 확인하기" };
    private static final String[] CHOICES_CHECK_IS_END_OF_WEEK_REQUEST = { "7의 배수 일자인지 확인하기" };
    private static final String[] CHOICES_FINALIZE_DAY = { "일일 정산하기" };
    private static final String[] CHOICES_FINALIZE_WEEK = { "주간 정산하기" };
    private static final String[] CHOICES_NEXT_DAY_REQUEST = { "다음 날로 넘어가기" };
    private static final String[] CHOICES_MAIN_MENU = { "거래 (재개)하기", "빛 상환 / 아이템 처리", "게임 포기하기" };
    private static final String[] CHOICES_GET = { "가져오기" };
    private static final String[] CHOICES_RECORD = { "기록하기" };
    private static final String[] CHOICES_FINISH_AUCTION = { "정산하기" };
    private static final String[] CHOICES_FINISH_RECOVER = { "처리하기" };
    
    private int gameSessionId;
    private int playerKey;
    private PlayerSession playerSession;
    private DealScreen dealScreen;
    private DebtAndItemScreen debtAndItemScreen;
    private SellScreen sellScreen;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    public MainScreen(Connection connection, Scanner scanner) {
        super(connection, scanner);
        this.playerSession = PlayerSession.getInstance();
        dealScreen = new DealScreen(connection, scanner);
        debtAndItemScreen = new DebtAndItemScreen(connection, scanner);
        sellScreen = new SellScreen(connection, scanner);
    }

    public void showMainScreen() {
        showLoadPlayingGameSessionRequest(); // 게임 세션 불러오기 실행
        if (gameSessionId == 0) { // 세션 없음
            showCreateGameSessionRequest(); // 게임 세션 생성
        }
        showDiplayItemRequest();

        boolean isFirst = true;
        mainLoop:
        while (true) {
            int remaingDealCount = showCheckRemaingDealRequest();
            if (remaingDealCount == 0) { // 대기 중인 거래가 없음
                if (!isFirst) { // 들어와서 첫 루프 아님. 정산 할 시간
                    if (showCheckIsEndOfWeekRequest()) { // 7일차 배수인지 체크 
                        if (showFinalizeWeek()) {  // 주간 정산
                            System.out.println("***게임 오버***\n이자를 내지 못해 파산했습니다...");
                            showDefeat();
                            break mainLoop;
                        }
                    } else {
                        if (showFinalizeDay()) {  // 일간 정산
                            System.out.println("***게임 오버***\n이자를 내지 못해 파산했습니다...");
                            showDefeat();
                            break mainLoop;
                        }
                    }
                    showNextDayRequest(); // 다음 날로 이동
                }
                finishAuction();
                if (finishRecover()) {
                    System.out.println("***게임 오버***\n복원비를 내지 못해 파산했습니다...");
                    showDefeat();
                    break mainLoop;
                }
                showGenerateDailyDeals();  // 거래 3개 생성
            } else {
                // 대기 중인 거래가 있을 때
                // 첫 번째 거래 가져오기
                DealRecordByItemState[] deals;
                try {
                    deals = DealRecordByItemState.getDealRecordByItemState(connection, playerKey, 0);
                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new CloseGameException();
                }
                int firstDrcKey = deals[0].drcKey;

                showTrySell(deals[0].sellerKey);
                
                switch (showChoices(TITLE_MAIN_MENU, CHOICES_MAIN_MENU)) {
                    case 1:
                        dealScreen.showDealScreen(firstDrcKey);
                        break;
                    case 2:
                        if (debtAndItemScreen.showDebtAndItemScreen()) {
                            showWin();
                            break mainLoop;
                        }
                        break;
                    case 3:
                        System.out.println("***게임 오버***\n게임을 포기하셨습니다...");
                        showDefeat();
                        break mainLoop;
                    default:
                        throw new IllegalStateException("Invalid choice");
                }
            }
            isFirst = false;
        }
    }
    
    private int showLoadPlayingGameSessionRequest() {
        showChoices(TITLE_LOAD_PLAYING_GAME_SESSION_REQUEST, MESSAGE_LOAD_PLAYING_GAME_SESSION_REQUEST, CHOICES_LOAD_PLAYING_GAME_SESSION_REQUEST, false);

        try {
            // 세션 토큰으로 플레이어 키 가져오기
            playerKey = PlayerKeyByToken.getPlayerKey(connection, playerSession.getSessionToken());
        } catch (NotASuchRowException e) {
            // 진행 중인 게임이 없거나 오류 발생
            gameSessionId = 0;
            System.out.println("진행 중인 게임 세션이 없습니다.");
            System.out.println("계속하려면 Enter를 누르세요...");
            scanner.nextLine();
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }
        
        // 가장 최근 게임 세션 정보 가져오기
        PlayerInfo playerInfo;
        try {
            playerInfo = PlayerInfo.getPlayerInfo(connection, playerKey);
        } catch (NotASuchRowException e) {
            // 진행 중인 게임이 없거나 오류 발생
            gameSessionId = 0;
            System.out.println("\n진행 중인 게임 세션이 없습니다.");
            System.out.println("\n계속하려면 Enter를 누르세요...");
            scanner.nextLine();
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }
        
        // 게임이 이미 종료되었는지 확인
        if (playerInfo.gameEndDayCount != 0) {
            // 종료된 게임이므로 새 게임 필요
            gameSessionId = 0;
            return 0;
        }
        
        gameSessionId = playerInfo.gameSessionKey;
        
        System.out.println("\n=== 게임 세션 로드 완료 ===");
        System.out.println("닉네임: " + playerInfo.nickname);
        System.out.println("상점명: " + playerInfo.shopName);
        System.out.println("현재 일수: " + playerInfo.dayCount + "일");
        System.out.println("현재 잔액: " + String.format("%,dG", playerInfo.money));
        System.out.println("개인 빚: " + String.format("%,dG", playerInfo.personalDebt));
        System.out.println("전당포 빚: " + String.format("%,dG", playerInfo.pawnshopDebt));
        System.out.println("=======================");
        System.out.println("\n계속하려면 Enter를 누르세요...");
        scanner.nextLine();
        
        return gameSessionId;
    }

    private void showCreateGameSessionRequest() {
        showChoices(TITLE_CREATE_GAME_SESSION_REQUEST, MESSAGE_CREATE_GAME_SESSION_REQUEST, CHOICES_CREATE_GAME_SESSION_REQUEST, false);

        String nickname = "";
        String shopName = "";

        while (true) {
            System.out.print("닉네임을 입력하세요 (최대 10글자): ");
            nickname = scanner.nextLine().trim();
            if (nickname.isEmpty()) {
                System.out.println("닉네임이 입력되지 않았습니다.");
                continue;
            }
            if (nickname.length() > 10) {
                System.out.println("길이가 너무 깁니다.");
                continue;
            }
            break;
        }
        
        while (true) {
            System.out.print("상점 이름을 입력하세요 (최대 10글자): ");
            shopName = scanner.nextLine().trim();
            if (shopName.isEmpty()) {
                System.out.println("상점 이름이 입력되지 않았습니다.");
                continue;
            }
            if (shopName.length() > 10) {
                System.out.println("길이가 너무 깁니다.");
                continue;
            }
            break;
        }

        PlayerInfo playerInfo;
        try {
            // 새 게임 세션 생성
            InsertGameSession.insertGameSession(connection, playerSession.getSessionToken(), nickname, shopName);
            // 생성된 게임 세션 정보 가져오기
            playerKey = PlayerKeyByToken.getPlayerKey(connection, playerSession.getSessionToken());
            playerInfo = PlayerInfo.getPlayerInfo(connection, playerKey);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }
        
        gameSessionId = playerInfo.gameSessionKey;
        
        System.out.println("\n=== 새 게임 세션 생성 완료 ===");
        System.out.println("닉네임: " + playerInfo.nickname);
        System.out.println("상점명: " + playerInfo.shopName);
        System.out.println("시작 일수: " + playerInfo.dayCount + "일");
        System.out.println("시작 잔액: " + String.format("%,dG", playerInfo.money));
        System.out.println("개인 빚: " + String.format("%,dG", playerInfo.personalDebt));
        System.out.println("전당포 빚: " + String.format("%,dG", playerInfo.pawnshopDebt));
        System.out.println("===========================");
        System.out.println("\n계속하려면 Enter를 누르세요...");
        scanner.nextLine();
    }

    private void showDiplayItemRequest() {
        showChoices(TITLE_DIPLAY_ITEM_REQUEST, MESSAGE_DIPLAY_ITEM_REQUEST, CHOICES_DIPLAY_ITEM_REQUEST, false);

        // 전시 중인 아이템 목록 가져오기
        ItemInDisplay[] displayedItems;
        try {
            displayedItems = ItemInDisplay.getItemInDisplay(connection, playerKey);
        } catch (NotASuchRowException e) {
            System.out.println("전시 중인 아이템이 없습니다.");
            System.out.println("계속하려면 Enter를 누르세요...");
            scanner.nextLine();
            return;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }
        
        System.out.println("\n=== 전시장 아이템 목록 ===");
        System.out.println("전시 중인 아이템 수: " + displayedItems.length + "개\n");
        
        // 각 아이템의 상세 정보 가져오기
        for (ItemInDisplay item : displayedItems) {
            DisplayedItemInfo detailInfo = null;
            try {
                // DisplayedItemInfo로 거래 정보 포함 상세 정보 가져오기
                detailInfo = DisplayedItemInfo.getDisplayedItemInfo(connection, item.itemKey);
            } catch (NotASuchRowException e) {
                // 상세 정보를 가져올 수 없는 경우 기본 정보만 표시
                System.out.println("위치 " + item.displayPos + ": " + item.itemCatalogName);
                System.out.println("  - 기본 정보만 표시 (상세 정보 없음)");
                System.out.println();
            } catch (SQLException e) {
                e.printStackTrace();
                throw new CloseGameException();
            }
                
            System.out.println("위치 " + detailInfo.displayPos + ": " + detailInfo.itemCatalogName);
            
            // 거래 정보
            System.out.println("판매자: " + detailInfo.customerName);
            System.out.println("최초 제시가: " + String.format("%,dG", detailInfo.askingPrice));
            System.out.println("구매가: " + String.format("%,dG", detailInfo.purchasePrice));
            System.out.println("감정가: " + String.format("%,dG", detailInfo.appraisedPrice));
            System.out.println("구매일: " + detailInfo.boughtDate + "일차");
            
            // 아이템 상태 정보
            System.out.println("\n[아이템 정보]");
            System.out.println("발견한 등급: " + detailInfo.foundGrade);
            System.out.println("발견한 결함: " + detailInfo.foundFlawEa);
            
            // 진위 여부 표시 (JSON용: 1=진품, 0=가품, -1=미발견)
            if (detailInfo.isAuthenticityFound) {
                // int authenticityStatus = detailInfo.authenticity ? 1 : 0;
                System.out.println("진위 여부: " + (detailInfo.authenticity ? "진품" : "가품") + " (감정 완료 상태)");
            } else {
                // int authenticityStatus = -1;
                System.out.println("진위 여부: 미감정");
            }
            
            // 아이템 상태
            String itemStateStr = "";
            switch (detailInfo.itemState) {
                case 0:
                    itemStateStr = "생성됨";
                    break;
                case 1:
                    itemStateStr = "전시 중";
                    break;
                case 2:
                    itemStateStr = "복원 중";
                    break;
                case 3:
                    itemStateStr = "경매 중";
                    break;
                case 4:
                    itemStateStr = "판매 됨";
                    break;
                case 5:
                    itemStateStr = "복원 완료";
                    break;
                default:
                    itemStateStr = "알 수 없음";
                    break;
            }
            System.out.println("상태: " + itemStateStr);
            System.out.println();
        }
        
        System.out.println("========================");
        System.out.println("\n계속하려면 Enter를 누르세요...");
        scanner.nextLine();
    }

    private int showCheckRemaingDealRequest() {
        showChoices(TITLE_CHECK_REMAING_DEAL_REQUEST, MESSAGE_CHECK_REMAING_DEAL_REQUEST, CHOICES_CHECK_REMAING_DEAL_REQUEST, false);

        // ITEM_STATE = 0 (생성됨) 상태인 거래 조회
        DealRecordByItemState[] deals;
        try {
            deals = DealRecordByItemState.getDealRecordByItemState(connection, playerKey, 0);
        } catch (NotASuchRowException e) {
            deals = new DealRecordByItemState[0];
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }
        
        System.out.println("\n=== 대기 중인 거래 ===");
        System.out.println("대기 중인 거래 수: " + deals.length + "개");
        System.out.println("====================");
        System.out.println("\n계속하려면 Enter를 누르세요...");
        scanner.nextLine();
        
        return deals.length;
    }

    private boolean showCheckIsEndOfWeekRequest() {
        showChoices(TITLE_CHECK_IS_END_OF_WEEK_REQUEST, CHOICES_CHECK_IS_END_OF_WEEK_REQUEST, false);

        // 현재 게임 세션 정보 가져오기
        PlayerInfo playerInfo;
        try {
            playerInfo = PlayerInfo.getPlayerInfo(connection, playerKey);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }
        
        // 7의 배수인지 확인 (1일차는 제외)
        boolean isEndOfWeek = (playerInfo.dayCount > 1) && (playerInfo.dayCount % 7 == 0);
        
        if (isEndOfWeek) {
            System.out.println("\n주간 정산이 필요합니다! (현재: " + playerInfo.dayCount + "일차)");
        } else {
            System.out.println("\n일일 정산만 진행합니다. (현재: " + playerInfo.dayCount + "일차)");
        }
        System.out.println("\n계속하려면 Enter를 누르세요...");
        scanner.nextLine();
        
        return isEndOfWeek;
    }

    private boolean showFinalizeDay() {
        showChoices(TITLE_FINALIZE_DAY, MESSAGE_FINALIZE_DAY, CHOICES_FINALIZE_DAY, false);

        // 일일 정산 정보 가져오기
        DailyCalculate daily;
        try {
            daily = DailyCalculate.getDailyCalculate(connection, playerKey);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }
        
        System.out.println("\n=== 일일 정산 ===");
        System.out.println("오늘 시작 잔액: " + String.format("%,dG", daily.todayStart));
        System.out.println("오늘 종료 잔액: " + String.format("%,dG", daily.todayEnd));
        System.out.println("전당포 이자: " + String.format("-%,dG", daily.todayInterest));
        System.out.println("최종 잔액: " + String.format("%,dG", daily.todayFinal));
        System.out.println("================");
        
        // 이자 차감
        if (daily.todayInterest > 0) {
            try {
                MoneyUpdater.subtractMoney(connection, playerSession.getSessionToken(), daily.todayInterest);
            } catch (SQLException e) {
                e.printStackTrace();
                throw new CloseGameException();
            }
        }
        
        // 게임 오버 확인 (돈이 음수)
        if (daily.todayFinal < 0) {
            return true;
        }
        
        System.out.println("\n계속하려면 Enter를 누르세요...");
        scanner.nextLine();

        return false;
    }

    private boolean showFinalizeWeek() {
        showChoices(TITLE_FINALIZE_WEEK, MESSAGE_FINALIZE_WEEK, CHOICES_FINALIZE_WEEK, false);

        // 주간 정산 정보 가져오기
        WeeklyCaluclate weekly;
        try {
            weekly = WeeklyCaluclate.getWeeklyCaluclate(connection, playerKey);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }
        
        System.out.println("\n=== 주간 정산 ===");
        System.out.println("오늘 시작 잔액: " + String.format("%,dG", weekly.todayStart));
        System.out.println("오늘 종료 잔액: " + String.format("%,dG", weekly.todayEnd));
        System.out.println("전당포 이자: " + String.format("-%,dG", weekly.todayInterest));
        System.out.println("개인 빚 이자: " + String.format("-%,dG", weekly.todayPersonalInterest));
        System.out.println("최종 잔액: " + String.format("%,dG", weekly.todayFinal));
        System.out.println("================");
        
        // 이자 차감
        int totalInterest = weekly.todayInterest + weekly.todayPersonalInterest;
        if (totalInterest > 0) {
            try {
                MoneyUpdater.subtractMoney(connection, playerSession.getSessionToken(), totalInterest);
            } catch (SQLException e) {
                e.printStackTrace();
                throw new CloseGameException();
            }
        }
        
        // 게임 오버 확인 (돈이 음수)
        if (weekly.todayFinal < 0) {
            return true;
        }
        
        System.out.println("\n계속하려면 Enter를 누르세요...");
        scanner.nextLine();

        return false;
    }

    private void showNextDayRequest() {
        showChoices(TITLE_NEXT_DAY_REQUEST, MESSAGE_NEXT_DAY_REQUEST, CHOICES_NEXT_DAY_REQUEST, false);

        // DAY_COUNT 증가
        try {
            GameSessionUpdater.incrementDayCount(connection, playerSession.getSessionToken());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }
        
        // 업데이트된 게임 세션 정보 가져오기 (UI 표시용)
        PlayerInfo playerInfo;
        try {
            playerInfo = PlayerInfo.getPlayerInfo(connection, playerKey);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }
        
        System.out.println("\n=== 다음 날로 진행 ===");
        System.out.println("현재 일수: " + playerInfo.dayCount + "일차");
        System.out.println("현재 잔액: " + String.format("%,dG", playerInfo.money));
        System.out.println("====================");
        System.out.println("\n계속하려면 Enter를 누르세요...");
        scanner.nextLine();
    }

    private void finishAuction() {
        showChoices(TITLE_FINISH_AUCTION, CHOICES_FINISH_AUCTION, false);
        try {
            AuctioningItems[] auctioningItems = AuctioningItems.getAuctioningItems(connection, playerSession.sessionToken);
            for (AuctioningItems auctioningItem : auctioningItems) {
                // 아이템 스테이트 판매 완료
                ExistingItemUpdater.updateItemState(connection, auctioningItem.itemKey, ItemState.SOLD.value());

                double multiplier = 1.0;
                TodaysEvent[] events = TodaysEvent.getTodaysEvent(connection, playerKey);
                for (TodaysEvent event : events) {
                    if (event.affectedPrice == 3 && event.categoryKey == auctioningItem.itemCategory) {
                        multiplier += event.amount * event.plusMinus * 0.01;
                    }
                }
                int soldPrice = (int)(auctioningItem.appraisedPrice * (Math.random() * 0.3 + 1.2) * multiplier);

                DealRecordUpdater.updateSoldInfo(connection, playerSession.sessionToken, auctioningItem.itemKey, soldPrice);
                MoneyUpdater.addMoney(connection, playerSession.getSessionToken(), soldPrice);
                DisplayManagement.removeFromDisplay(connection, auctioningItem.itemKey);

                System.out.println("-".repeat(80));
                System.out.println("아이템 이름: " + auctioningItem.itemName);
                System.out.println("경매 수익: " + soldPrice);

                // // 세션토큰 키로 플레이어 키 받아오기 PlayerKeyByToken - getPlayerKey
                // int playerKey = PlayerKeyByToken.getPlayerKey(connection, PlayerSession.getInstance().getSessionToken());
                // // 현재 진행 중인 이벤트 가져오기[기존 쿼리 활용] TodaysEvent - getTodaysEvent
                // TodaysEvent[] events = TodaysEvent.getTodaysEvent(connection, playerKey);
                // // 경매 완료 - 판매 날짜 및 최종 판매가 기록 DealRecordUpdater - updateSoldInfo
                // // itemKey, sellingPrice, buyerKey
                // DealRecordUpdater.updateSoldInfo(connection, playerSession.getSessionToken(), );
                // // 경매 수익금 입금 MoneyUpdater -  addMoney
                // MoneyUpdater.addMoney(connection, playerSession.getSessionToken(),); // amount
                // // 전시장에서 제거 DisplayManagement - removeFromDisplay
                // DisplayManagement.removeFromDisplay();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }
        System.out.println("-".repeat(80));
    }

    private boolean finishRecover() {
        showChoices(TITLE_FINISH_RECOVER, CHOICES_FINISH_RECOVER, false);
        RestoringItems[] restoringItems;
        try {
            restoringItems = RestoringItems.getRestoringItems(connection, playerSession.sessionToken);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }
        for (RestoringItems restoringItem : restoringItems) {
            int flawBefore = restoringItem.flawEa;
            
            // 아이템 스테이트 복원 완료
            try {
                ExistingItemUpdater.removeFlaw(connection, restoringItem.itemKey);
                ExistingItemUpdater.updateAuthenticityFound(connection, restoringItem.itemKey);
                ExistingItemUpdater.updateItemState(connection, restoringItem.itemKey, ItemState.RECORVERED.value());
            } catch (SQLException e) {
                e.printStackTrace();
                throw new CloseGameException();
            }

            double gradeMultiplier;
            switch (restoringItem.grade) {
                case 0:
                    gradeMultiplier = 1.0;
                    break;
                case 1:
                    gradeMultiplier = 1.2;
                    break;
                case 2:
                    gradeMultiplier = 1.5;
                    break;
                case 3:
                    gradeMultiplier = 1.7;
                    break;
                default:
                    throw new IllegalStateException("Invalid grade");
            }

            double authenticityMultiplier = restoringItem.authenticity ? 1.0 : 0.7;

            double eventMultiplier = 1.0;
            TodaysEvent[] events;
            try {
                events = TodaysEvent.getTodaysEvent(connection, playerKey);
            } catch (SQLException e) {
                e.printStackTrace();
                throw new CloseGameException();
            }
            for (TodaysEvent event : events) {
                if (event.affectedPrice == 3 && event.categoryKey == restoringItem.itemCategory) {
                    eventMultiplier += event.amount * event.plusMinus * 0.01;
                }
            }

            int newAppraisedPrice = (int) (restoringItem.appraisedPrice * gradeMultiplier * authenticityMultiplier * eventMultiplier);

            try {
                DealRecordUpdater.updateAppraisedPrice(connection, restoringItem.itemKey, newAppraisedPrice);
            } catch (SQLException e) {
                e.printStackTrace();
                throw new CloseGameException();
            }

            PlayerInfo playerInfo;
            try {
                playerInfo = PlayerInfo.getPlayerInfo(connection, playerKey);
            } catch (SQLException e) {
                e.printStackTrace();
                throw new CloseGameException();
            }
            if (playerInfo.money < flawBefore * 10) {
                return true;
            }
            try {
                MoneyUpdater.addMoney(connection, playerSession.sessionToken, flawBefore * -10);
            } catch (SQLException e) {
                e.printStackTrace();
                throw new CloseGameException();
            }

            System.out.println("-".repeat(80));
            System.out.println("아이템 이름: " + restoringItem.itemName);
            System.out.println("복원 비용: " + flawBefore * -10);
            System.out.println("현재 감정가: " + newAppraisedPrice);

            // 세션토큰으로 플레이어 키 받아오기 PlayerKeyByToken - getPlayerKey
            // 현재 진행 중인 이벤트 가져오기[기존 쿼리 활용] TodaysEvent - getTodaysEvent
            // 복원 완료 - 진위 확정  ExistingItemUpdater - updateAuthenticityFound
            // 복원 완료 - 감정가 업데이트 DealRecordUpdater - updateAppraisedPrice
            // 복원 비용 차감 MoneyUpdater - subtractMoney
        }
        System.out.println("-".repeat(80));
        return false;
    }

    private void showGenerateDailyDeals() {
        System.out.println("\n=== 오늘의 거래 생성 ===");
        
        // 게임 세션 키 가져오기
        int gameSessionKey;
        try {
            gameSessionKey = GameSessionByToken.getGameSessionKey(connection, playerSession.getSessionToken());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }
        
        // 현재 적용 중인 이벤트(뉴스) 가져오기
        TodaysEvent[] events = null;
        try {
            events = TodaysEvent.getTodaysEvent(connection, playerKey);
            System.out.println("현재 적용 중인 이벤트: " + events.length + "개");
        } catch (NotASuchRowException e) {
            System.out.println("현재 적용 중인 이벤트 없음");
            events = new TodaysEvent[0];
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }
        
        // 1. 랜덤 고객 3명 선택
        RandomCustomersWithDetails[] customers;
        try {
            customers = RandomCustomersWithDetails.getRandomCustomersWithDetails(connection, 3);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }
        System.out.println("고객 3명 선택 완료");
        
        // 2. 각 고객별로 거래 생성
        for (int i = 0; i < customers.length; i++) {
            RandomCustomersWithDetails customer = customers[i];
            
            // 랜덤 아이템 카탈로그 선택 (고객의 선호 카테고리)
            ItemCatalog itemCatalog;
            try {
                itemCatalog = ItemCatalog.getRandomItemByCategory(connection, customer.categoryKey);
            } catch (SQLException e) {
                e.printStackTrace();
                throw new CloseGameException();
            }
            
            // 흠이 있을 삘 (랜덤 변수)
            float suspiciousFlawAura = (float) Math.random();
            
            // 흠 개수 계산: (0 + 10 * 부주의함) + (0 + 4 * 흠이 있을 삘)
            int flawEa = (int) Math.round((10 * customer.clumsy) + (4 * suspiciousFlawAura));
            flawEa = Math.min(14, Math.max(0, flawEa)); // 0~14 범위 제한
            
            // 진품/가품 결정
            // 가품 확률: 10 + 90 * 사기정도
            double fakeProbability = 10 + (90 * customer.fraud);
            boolean isAuthentic = (Math.random() * 100) >= fakeProbability;
            char authenticity = isAuthentic ? 'Y' : 'N';
            
            // 등급 결정
            int grade = determineGradeByCustomer(customer.wellCollect);
            
            // EXISTING_ITEM 생성
            try {
                InsertExistingItem.insertItem(connection, gameSessionKey, itemCatalog.itemCatalogKey, 
                                                grade, flawEa, suspiciousFlawAura, authenticity);
            } catch (SQLException e) {
                e.printStackTrace();
                throw new CloseGameException();
            }
            
            // 방금 생성한 아이템의 키 가져오기
            int itemKey;
            try {
                itemKey = GetLastInsertedItemKey.getLastInsertedItemKey(connection, gameSessionKey);
            } catch (SQLException e) {
                e.printStackTrace();
                throw new CloseGameException();
            }
            
            // 거래 기준가 계산
            int basePrice = itemCatalog.basePrice;
            int askingPrice = calculateTradingBasePrice(basePrice, flawEa, fakeProbability, grade, 
                                                        customer.fraud, customer.wellCollect, 
                                                        events, itemCatalog.categoryKey);
            int purchasePrice = askingPrice; // 초기 구매가 = 최초 제시가
            int appraisedPrice = askingPrice; // 초기 감정가 = 최초 제시가
            
            // DEAL_RECORD 생성
            try {
                InsertDealRecord.insertDealRecord(connection, gameSessionKey, customer.customerKey, 
                                                    itemKey, askingPrice, purchasePrice, appraisedPrice);
            } catch (SQLException e) {
                e.printStackTrace();
                throw new CloseGameException();
            }
            
            System.out.println((i + 1) + "번 거래 생성: " + itemCatalog.itemCatalogName + 
                                " (등급: " + getGradeString(grade) + ", 흠: " + flawEa + "개, " + 
                                (isAuthentic ? "진품" : "가품") + ", 제시가: " + String.format("%,dG", askingPrice) + ")");
        }
        
        System.out.println("\n총 " + customers.length + "개의 거래가 생성되었습니다!");
        System.out.println("======================");
        System.out.println("\n계속하려면 Enter를 누르세요...");
        scanner.nextLine();
    }

    private void showTrySell(int sellerKey) {
        // 고객 선호 Category와 Match되는 Item 존재 -> 고객 구매 시도
        PreferableItemsInDisplay[] preferableItems;
        try {
            preferableItems = PreferableItemsInDisplay.getPreferableItemsInDisplay(connection, sellerKey, playerKey);
        } catch (NotASuchRowException e) {
            System.out.println("고객이 선호하는 Item이 진열되어 있지 않습니다.");
            return;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }
        if (preferableItems.length > 0) {
            sellScreen.showSellScreen(preferableItems[0].itemKey);
        }
    }
    
    private int determineGradeByCustomer(float wellCollect) {
        // 레전더리 확률: 15 + (65 * 잘수집정도)
        // 유니크 확률: 20 + ((65 * (1 - 잘수집정도)) / 3)
        // 레어 확률: 30 + ((65 * (1 - 잘수집정도)) / 3)
        // 일반 확률: 35 + ((65 * (1 - 잘수집정도)) / 3)
        
        double legendaryP = 15 + (65 * wellCollect);
        double baseP = (65 * (1 - wellCollect)) / 3;
        double uniqueP = 20 + baseP;
        double rareP = 30 + baseP;
        // double normalP = 35 + baseP; // 나머지는 모두 일반
        
        double rand = Math.random() * 100.0;
        double cumulative = 0;
        
        cumulative += legendaryP;
        if (rand < cumulative) return 3; // 레전더리
        
        cumulative += uniqueP;
        if (rand < cumulative) return 2; // 유니크
        
        cumulative += rareP;
        if (rand < cumulative) return 1; // 레어
        
        return 0; // 일반
    }
    
    private int calculateTradingBasePrice(int basePrice, int flawEa, double fakeProbability, int grade,
                                          float fraud, float wellCollect, 
                                          TodaysEvent[] events, int itemCategoryKey) {
        // 거래 기준가 = 기준가 × [(1−0.02×흠 개수) × (1−0.3×가품 확률) × ((1+0.3×등급)/3) 
        //                      × (1+0.25×사기 정도) × (1+0.2×(수집력−0.5))]
        
        double flawFactor = 1 - (0.02 * flawEa);
        double fakeFactor = 1 - (0.3 * (fakeProbability / 100.0));
        double gradeFactor = (1 + 0.3 * grade) / 3.0;
        double fraudFactor = 1 + (0.25 * fraud);
        double collectFactor = 1 + (0.2 * (wellCollect - 0.5));
        
        double tradingBasePrice = basePrice * flawFactor * fakeFactor * gradeFactor * fraudFactor * collectFactor;
        
        // 최초 제시가 = 거래 기준가 × (이벤트 수치 +연산, 최대 3개)
        double eventMultiplier = 1.0;
        int appliedEventCount = 0;
        
        for (TodaysEvent event : events) {
            if (appliedEventCount >= 3) break; // 최대 3개까지만
            
            // 해당 카테고리에 영향을 주는 이벤트만 적용 (categoryKey가 0이면 전체 적용)
            if (event.categoryKey == 0 || event.categoryKey == itemCategoryKey) {
                double eventEffect = event.affectedPrice / 100.0; // 퍼센트를 비율로 변환
                
                if (event.plusMinus == 1) { // 더하기
                    eventMultiplier += eventEffect;
                } else { // 빼기
                    eventMultiplier -= eventEffect;
                }
                
                appliedEventCount++;
            }
        }
        
        int finalPrice = (int) Math.round(tradingBasePrice * eventMultiplier);
        return Math.max(1, finalPrice); // 최소 1G
    }
    
    private String getGradeString(int grade) {
        switch (grade) {
            case 0: return "일반";
            case 1: return "레어";
            case 2: return "유니크";
            case 3: return "레전더리";
            default: return "알 수 없음";
        }
    }
    
    private void showWin() {
        System.out.println("***게임 클리어***\n축하합니다!\n모든 빚을 갚으셨습니다.");
        
        // 게임 종료 기록
        showChoices(TITLE_RECORD_GAME_CLEAR, CHOICES_RECORD);
        PlayerInfo playerInfo;
        try {
            playerInfo = PlayerInfo.getPlayerInfo(connection, playerKey);
            GameSessionUpdater.setGameEnd(connection, playerSession.getSessionToken(), playerInfo.dayCount);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }
        showGameEnd();
        
        System.out.println("인트로 화면으로 돌아가시려면 Enter를 누르세요...");
        scanner.nextLine();
    }
    
    private void showDefeat() {
        // 게임 종료 기록
        showChoices(TITLE_RECORD_GAME_DEFEAT, CHOICES_RECORD);
        PlayerInfo playerInfo;
        try {
            playerInfo = PlayerInfo.getPlayerInfo(connection, playerKey);
            GameSessionUpdater.setGameEnd(connection, playerSession.getSessionToken(), -playerInfo.dayCount);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }
        showGameEnd();        
        System.out.println("인트로 화면으로 돌아가시려면 Enter를 누르세요...");
        scanner.nextLine();
    }
    
    private void showGameEnd() {
        CurrentGameSummary gameSummary = null;
        String[] notFoundItemNames = null;
        
        showChoices(TITLE_GET_CURRENT_GAME_SUMMARY, MESSAGE_GET_CURRENT_GAME_SUMMARY, CHOICES_GET, false);
        try {
            gameSummary = CurrentGameSummary.retrieveGameSummary(connection, playerSession.sessionToken);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }
        
        showChoices(TITLE_GET_NOT_FOUND_ITEM, MESSAGE_GET_NOT_FOUND_ITEM, CHOICES_GET, false);
        try {
            notFoundItemNames = NotFoundItem.getNotFoundItemName(connection, playerKey);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }
        
        System.out.println("          닉네임: " + gameSummary.nickName);
        System.out.println("       가게 이름: " + gameSummary.shopName);
        System.out.println("게임 진행한 날짜: " + Math.abs(gameSummary.gameEndDayCount));
        System.out.println("    게임 끝난 날: " + gameSummary.gameEndDate);
        
        System.out.println("현재까지 발견하지 못한 아이템");
        for (String notFoundItemName : notFoundItemNames) {
            System.out.println("  " + notFoundItemName);
        }
    }
}
