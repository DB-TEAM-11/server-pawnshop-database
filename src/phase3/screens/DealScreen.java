package phase3.screens;

import java.sql.Connection;
import java.util.Random;
import java.util.Scanner;

import phase3.PlayerSession;
import phase3.queries.*;

public class DealScreen extends BaseScreen {
    private static String TITLE_MAIN = "고객과 거래";
    private static String TITLE_ITEM_INSPECTION = "물건 조사";
    private static String TITLE_GRADE_FIND = "등급 감정";
    private static String TITLE_FLAW_FIND = "흠 찾기";
    private static String TITLE_OPEN_CUSTOMER_HINT = "새 고객 힌트 열기";
    private static String TITLE_ACCEPT_DEAL__CHECK_BALANCE = "구매 전 잔고 확인";
    private static String TITLE_ACCEPT_DEAL__SAVE = "구매 기록 저장";
    private static String TITLE_DENY_DEAL__REMOVE_DEAL = "거래 삭제";

    private static String MESSAGE_DENY_DEAL__REMOVE_DEAL = "거래를 거절하였으므로, 해당 거래 기록을 삭제합니다.";

    private static String[] CHOICES_MAIN = {
        "물건 조사",
        "등급 감정",
        "흠 찾기",
        "진위 판정 [200G 소모]",
        "새 고객 힌트 열기",
        "거래 수락",
        "거래 거절",
        "거래 일시정지, 메인으로 돌아가기"
    };
    private static String[] CHOICES_ITEM_INSPECTION = { "물건 힌트 1개 열기 [10G 소모]", "이전 화면으로 돌아가기" };
    private static String[] CHOICES_GRADE_FIND = { "레어 감정 [20G 소모]", "유니크 감정 [30G 소모]", "레전더리 감정 [50G 소모]", "이전 화면으로 돌아가기" };
    private static String[] CHOICES_FLAW_FIND = { "하급 흠 찾기 [20G 소모]", "중급 흠 찾기 [60G 소모]", "고급 흠 찾기 [100G 소모]", "이전 화면으로 돌아가기" };
    private static String[] CHOICES_OPEN_CUSTOMER_HINT = { "'사기칠 거 같은 비율' 힌트 열기 [50G 소모]", "'수집가 능력' 힌트 열기 [50G 소모]", "'대충 관리함' 힌트 열기 [50G 소모]", "이전 화면으로 돌아가기" };
    private static String[] CHOICES_ACCEPT_DEAL__CHECK_BALANCE = { "잔고 확인" };
    private static String[] CHOICES_ACCEPT_DEAL__SAVE = { "저장" };
    private static String[] CHOICES_DENY_DEAL__REMOVE_DEAL = { "삭제" };
    
    // 게임 세션 정보 저장 싱글톤(전역 변수 느낌으로 사용)
    private PlayerSession playerSession;
    
    // 현재 처리 중인 거래 키
    private int currentDrcKey;

    public DealScreen(Connection connection, Scanner scanner) {
        super(connection, scanner);
        this.playerSession = PlayerSession.getInstance();
    }

    public void showDealScreen(int drcKey) {
        this.currentDrcKey = drcKey;
        playerSession.setCurrentDrcKey(drcKey);
        while (true) {
            switch (showChoices(TITLE_MAIN, formatMainMessage(), CHOICES_MAIN)) {
                case 1:
                    showItemInspectionScreen();
                    break;
                case 2:
                    showGradeFindScreen();
                    break;
                case 3:
                    showFlawFindScreen();
                    break;
                case 4:
                    showItemAuthenticationScreen();
                    break;
                case 5:
                    showOpenCustomerHintScreen();
                    break;
                case 6:
                    if (showAcceptDealScreen()) {
                        return;
                    }
                    break;
                case 7:
                    showDenyDealScreen();
                    return;
                case 8:
                    return;
            }
        }
    }

    private String formatMainMessage() {
        try {
            DealRecordByDrcKey dealRecord = DealRecordByDrcKey.getDealRecordByDrcKey(connection, currentDrcKey);
            CustomerInfo customer = CustomerInfo.getCustomerInfo(connection, dealRecord.sellerKey);
            ItemCatalog itemCatalog = ItemCatalog.getItemByKey(connection, dealRecord.itemCatalogKey);
            int money = UpdateMoney.getMoney(connection, playerSession.getSessionToken());
            
            String gradeStr = getGradeString(dealRecord.foundGrade);
            String authenticityStr = dealRecord.isAuthenticityFound 
                ? (dealRecord.authenticity ? "진품 (확정)" : "가품 (확정)")
                : "미확인";
            
            StringBuilder sb = new StringBuilder();
            sb.append("=====================================\n");
            sb.append(String.format("고객: %s (%s)\n", customer.customerName, customer.imgId));
            sb.append(String.format("아이템: %s\n", itemCatalog.itemCatalogName));
            sb.append(String.format("발견 등급: %s\n", gradeStr));
            sb.append(String.format("발견 흠: %d개\n", dealRecord.foundFlawEa));
            sb.append(String.format("진위 판정: %s\n", authenticityStr));
            sb.append("\n");
            sb.append(String.format("최초 제시가: %,dG\n", dealRecord.askingPrice));
            sb.append(String.format("현재 구매가: %,dG\n", dealRecord.purchasePrice));
            sb.append(String.format("현재 감정가: %,dG\n", dealRecord.appraisedPrice));
            sb.append("\n");
            sb.append(String.format("---공개된 고객 힌트---\n"));
            
            // 공개된 힌트 조회
            try {
                int playerKey = PlayerKeyByToken.getPlayerKey(connection, playerSession.getSessionToken());
                PlayerInfo playerInfo = PlayerInfo.getPlayerInfo(connection, playerKey);
                int gameSessionKey = playerInfo.gameSessionKey;
                
                int hintRevealedFlag = CustomerHiddenDiscovered.getHintRevealedFlag(connection, gameSessionKey, dealRecord.sellerKey);
                
                boolean hasRevealedHint = false;
                
                // 000 중에서 [FRAUD][WELL-COLLECT][CLUMSY] 순으로 저장 되어있음
                // Bit 0: 사기칠 거 같은 비율 (FRAUD)
                if ((hintRevealedFlag & (1 << 2)) != 0) {
                    sb.append(String.format("사기칠 거 같은 비율: %.2f%%\n", customer.fraud * 100));
                    hasRevealedHint = true;
                }
                // Bit 1: 수집가 능력 (WELL_COLLECT)
                if ((hintRevealedFlag & (1 << 1)) != 0) {
                    sb.append(String.format("수집가 능력: %.2f%%\n", customer.wellCollect * 100));
                    hasRevealedHint = true;
                }
                // Bit 2: 대충 관리함 (CLUMSY)
                if ((hintRevealedFlag & (1 << 0)) != 0) {
                    sb.append(String.format("대충 관리함: %.2f%%\n", customer.clumsy * 100));
                    hasRevealedHint = true;
                }
                if (!hasRevealedHint) {
                    sb.append("(공개된 힌트 없음)\n");
                }
            } catch (Exception e) {
                // 힌트가 없거나 오류 발생 시
                sb.append("(공개된 힌트 없음)\n");
            }
            sb.append("\n");
            sb.append(String.format("현재 잔액: %,dG\n", money));
            sb.append("=====================================");
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "거래 정보를 불러오는 중 오류가 발생했습니다: " + e.getMessage();
        }
    }
    
    private String getGradeString(int grade) {
        switch (grade) {
            case 0: return "일반";
            case 1: return "레어";
            case 2: return "유니크";
            case 3: return "레전더리";
            default: return "등급 없음";
        }
    }
    
    // 이벤트 수치 반영 가격 배율 계산 함수
    private double getEventMultiplier(int itemCategoryKey) {
        try {
            int playerKey = PlayerKeyByToken.getPlayerKey(connection, playerSession.getSessionToken());
            TodaysEvent[] events = TodaysEvent.getTodaysEvent(connection, playerKey);
            
            double multiplier = 1.0;
            for (TodaysEvent event : events) {
                if (event.categoryKey == itemCategoryKey) {
                    double change = event.affectedPrice / 100.0;
                    if (event.plusMinus == 1) {
                        multiplier += change;
                    } else {
                        multiplier -= change;
                    }
                }
            }
            return multiplier;
        } catch (Exception e) {
            // 이벤트가 없거나 오류 발생 시 기본값 1.0 반환
            return 1.0;
        }
    }

    private void showItemInspectionScreen() {
        switch (showChoices(TITLE_ITEM_INSPECTION, CHOICES_ITEM_INSPECTION)) {
            case 1:
                openRandomItemHint();
                return;
            case 2:
                return;
        }
    }
    
    private void openRandomItemHint() {
        try {
            int money = UpdateMoney.getMoney(connection, playerSession.getSessionToken());
            if (money < 10) {
                System.out.println("잔액이 부족합니다! (필요: 10G, 현재: " + money + "G)");
                scanner.nextLine();
                return;
            }
            
            DealRecordByDrcKey dealRecord = DealRecordByDrcKey.getDealRecordByDrcKey(connection, currentDrcKey);
            CustomerProperty customerProp = CustomerProperty.getCustomerProperty(connection, dealRecord.sellerKey);
            
            Random random = new Random();
            int hintChoice = random.nextInt(6);
            String hintName = "";
            String hintValue = "";
            
            switch (hintChoice) {
                case 0:
                    hintName = "흠이 있을 거 같은 느낌";
                    hintValue = String.format("%.2f", dealRecord.suspiciousFlawAura);
                    break;
                case 1:
                    hintName = "레전더리 확률";
                    hintValue = String.format("%.2f%%", customerProp.legendaryProbability);
                    break;
                case 2:
                    hintName = "유니크 확률";
                    hintValue = String.format("%.2f%%", customerProp.uniqueProbability);
                    break;
                case 3:
                    hintName = "레어 확률";
                    hintValue = String.format("%.2f%%", customerProp.rareProbability);
                    break;
                case 4:
                    hintName = "진품 확률";
                    hintValue = String.format("%.2f%%", customerProp.geniueProbability);
                    break;
                case 5:
                    hintName = "최소 흠 개수 추정";
                    hintValue = String.format("%.1f개 이상", customerProp.flawBase * 0.8);
                    break;
            }
            
            UpdateMoney.subtractMoney(connection, playerSession.getSessionToken(), 10);
            int newMoney = UpdateMoney.getMoney(connection, playerSession.getSessionToken());
            
            System.out.println("\n[물건 힌트]");
            System.out.println("힌트: " + hintName);
            System.out.println("값: " + hintValue);
            System.out.println("\n잔액: " + String.format("%,dG -> %,dG (-10G)", money, newMoney));
            System.out.println("\nEnter를 눌러 계속...");
            scanner.nextLine();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("힌트 열기 중 오류 발생: " + e.getMessage());
            scanner.nextLine();
        }
    }

    private void showGradeFindScreen() {
        switch (showChoices(TITLE_GRADE_FIND, CHOICES_GRADE_FIND)) {
            case 1:
                performGradeAppraisal(1, 20);
                return;
            case 2:
                performGradeAppraisal(2, 30);
                return;
            case 3:
                performGradeAppraisal(3, 50);
                return;
            case 4:
                return;
        }
    }
    
    private void performGradeAppraisal(int maxGrade, int cost) {
        try {
            int money = UpdateMoney.getMoney(connection, playerSession.getSessionToken());
            if (money < cost) {
                System.out.println("잔액이 부족합니다! (필요: " + cost + "G, 현재: " + money + "G)");
                scanner.nextLine();
                return;
            }
            
            DealRecordByDrcKey dealRecord = DealRecordByDrcKey.getDealRecordByDrcKey(connection, currentDrcKey);
            
            if (dealRecord.foundGrade >= maxGrade) {
                System.out.println("이미 " + getGradeString(dealRecord.foundGrade) + " 등급을 발견하였습니다!");
                scanner.nextLine();
                return;
            }
            
            Random random = new Random();
            int resultGrade = determineGradeByAppraisal(maxGrade, dealRecord.grade, random);
            
            if (resultGrade > dealRecord.foundGrade) {
                UpdateExistingItem.updateFoundGrade(connection, dealRecord.itemKey, resultGrade);
            }
            
            // 이벤트 수치 적용
            ItemCatalog itemCatalog = ItemCatalog.getItemByKey(connection, dealRecord.itemCatalogKey);
            double eventMultiplier = getEventMultiplier(itemCatalog.categoryKey);
            
            double gradeMultiplier = getGradeMultiplier(resultGrade);
            int newAppraisedPrice = (int)(dealRecord.askingPrice * (1 - dealRecord.foundFlawEa * 0.05) * gradeMultiplier * eventMultiplier);
            
            if (dealRecord.isAuthenticityFound && !dealRecord.authenticity) {
                newAppraisedPrice = (int)(newAppraisedPrice * 0.8);
            }
            
            UpdateDealRecord.updatePrices(connection, currentDrcKey, dealRecord.purchasePrice, newAppraisedPrice);
            
            UpdateMoney.subtractMoney(connection, playerSession.getSessionToken(), cost);
            int newMoney = UpdateMoney.getMoney(connection, playerSession.getSessionToken());
            
            System.out.println("\n[등급 감정 결과]");
            System.out.println("발견 등급: " + getGradeString(resultGrade));
            System.out.println("감정가 변경: " + String.format("%,dG -> %,dG", dealRecord.appraisedPrice, newAppraisedPrice));
            System.out.println("잔액: " + String.format("%,dG -> %,dG (-%dG)", money, newMoney, cost));
            System.out.println("\nEnter를 눌러 계속...");
            scanner.nextLine();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("등급 감정 중 오류 발생: " + e.getMessage());
            scanner.nextLine();
        }
    }
    
    private double getGradeMultiplier(int grade) {
        switch (grade) {
            case 0: return 1.0;
            case 1: return 1.2;
            case 2: return 1.5;
            case 3: return 1.7;
            default: return 1.0;
        }
    }
    
    private int determineGradeByAppraisal(int maxGrade, int realGrade, Random random) {
        if (realGrade <= maxGrade) {
            return realGrade;
        }
        double failProbability = 0.5;
        if (random.nextDouble() < failProbability) {
            return Math.max(0, maxGrade - 1);
        } else {
            return maxGrade;
        }
    }

    private void showFlawFindScreen() {
        switch (showChoices(TITLE_FLAW_FIND, CHOICES_FLAW_FIND)) {
            case 1:
                performFlawFind(1, 20);
                return;
            case 2:
                performFlawFind(4, 60);
                return;
            case 3:
                performFlawFind(7, 100);
                return;
            case 4:
                return;
        }
    }
    
    private void performFlawFind(int maxFind, int cost) {
        try {
            int money = UpdateMoney.getMoney(connection, playerSession.getSessionToken());
            if (money < cost) {
                System.out.println("잔액이 부족합니다! (필요: " + cost + "G, 현재: " + money + "G)");
                scanner.nextLine();
                return;
            }
            
            DealRecordByDrcKey dealRecord = DealRecordByDrcKey.getDealRecordByDrcKey(connection, currentDrcKey);
            
            // 실제 존재하는 흠 중 아직 발견하지 못한 흠의 개수
            int remainingFlaws = dealRecord.flawEa - dealRecord.foundFlawEa;
            
            // 최대로 찾을 수 있는 개수. 남은 거만큼만 찾을 수 있음
            int maxPossibleFlaws = Math.min(14 - dealRecord.foundFlawEa, remainingFlaws);
            
            // 업데이트 될 찾은 흠 개수
            int newFoundFlawEa = dealRecord.foundFlawEa + maxPossibleFlaws;
            
            UpdateExistingItem.updateFoundFlaw(connection, dealRecord.itemKey, maxPossibleFlaws);
            
            // 이벤트 수치 적용
            ItemCatalog itemCatalog = ItemCatalog.getItemByKey(connection, dealRecord.itemCatalogKey);
            double eventMultiplier = getEventMultiplier(itemCatalog.categoryKey);
            
            int newPurchasePrice = (int)(dealRecord.askingPrice * (1 - newFoundFlawEa * 0.05) * eventMultiplier);
            
            if (dealRecord.isAuthenticityFound && !dealRecord.authenticity) {
                newPurchasePrice = (int)(newPurchasePrice * 0.5);
            }
            
            double gradeMultiplier = getGradeMultiplier(dealRecord.foundGrade);
            int newAppraisedPrice = (int)(dealRecord.askingPrice * (1 - newFoundFlawEa * 0.05) * gradeMultiplier * eventMultiplier);
            
            if (dealRecord.isAuthenticityFound && !dealRecord.authenticity) {
                newAppraisedPrice = (int)(newAppraisedPrice * 0.8);
            }
            
            UpdateDealRecord.updatePrices(connection, currentDrcKey, newPurchasePrice, newAppraisedPrice);
            
            UpdateMoney.subtractMoney(connection, playerSession.getSessionToken(), cost);
            int newMoney = UpdateMoney.getMoney(connection, playerSession.getSessionToken());
            
            System.out.println("\n[흠 찾기 결과]");
            System.out.println("발견한 흠: " + maxPossibleFlaws + "개");
            System.out.println("총 발견 흠: " + newFoundFlawEa + "개");
            System.out.println("구매가 변경: " + String.format("%,dG -> %,dG", dealRecord.purchasePrice, newPurchasePrice));
            System.out.println("감정가 변경: " + String.format("%,dG -> %,dG", dealRecord.appraisedPrice, newAppraisedPrice));
            System.out.println("잔액: " + String.format("%,dG -> %,dG (-%dG)", money, newMoney, cost));
            System.out.println("\nEnter를 눌러 계속...");
            scanner.nextLine();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("흠 찾기 중 오류 발생: " + e.getMessage());
            scanner.nextLine();
        }
    }

    private void showItemAuthenticationScreen() {
        try {
            int money = UpdateMoney.getMoney(connection, playerSession.getSessionToken());
            if (money < 200) {
                System.out.println("잔액이 부족합니다! (필요: 200G, 현재: " + money + "G)");
                scanner.nextLine();
                return;
            }
            
            DealRecordByDrcKey dealRecord = DealRecordByDrcKey.getDealRecordByDrcKey(connection, currentDrcKey);
            
            if (dealRecord.isAuthenticityFound) {
                System.out.println("이미 진위 판정을 완료했습니다!");
                System.out.println("결과: " + (dealRecord.authenticity ? "진품" : "가품"));
                scanner.nextLine();
                return;
            }
            
            UpdateExistingItem.updateAuthenticityFound(connection, dealRecord.itemKey);
            
            int oldPurchasePrice = dealRecord.purchasePrice;
            int oldAppraisedPrice = dealRecord.appraisedPrice;
            int newPurchasePrice = dealRecord.purchasePrice;
            int newAppraisedPrice = dealRecord.appraisedPrice;
            
            if (!dealRecord.authenticity) {
                // 이벤트 수치 적용
                ItemCatalog itemCatalog = ItemCatalog.getItemByKey(connection, dealRecord.itemCatalogKey);
                double eventMultiplier = getEventMultiplier(itemCatalog.categoryKey);
                
                newPurchasePrice = (int)(dealRecord.askingPrice * (1 - dealRecord.foundFlawEa * 0.05) * 0.5 * eventMultiplier);
                double gradeMultiplier = getGradeMultiplier(dealRecord.foundGrade);
                newAppraisedPrice = (int)(dealRecord.askingPrice * (1 - dealRecord.foundFlawEa * 0.05) * gradeMultiplier * 0.8 * eventMultiplier);
                UpdateDealRecord.updatePrices(connection, currentDrcKey, newPurchasePrice, newAppraisedPrice);
            }
            
            UpdateMoney.subtractMoney(connection, playerSession.getSessionToken(), 200);
            int newMoney = UpdateMoney.getMoney(connection, playerSession.getSessionToken());
            
            System.out.println("\n[진위 판정 결과]");
            System.out.println("결과: " + (dealRecord.authenticity ? "진품" : "가품"));
            if (!dealRecord.authenticity) {
                System.out.println("가품이므로 구매가 50%, 감정가 20% 감소합니다.");
                System.out.println("구매가 변경: " + String.format("%,dG -> %,dG (%+,dG)", oldPurchasePrice, newPurchasePrice, newPurchasePrice - oldPurchasePrice));
                System.out.println("감정가 변경: " + String.format("%,dG -> %,dG (%+,dG)", oldAppraisedPrice, newAppraisedPrice, newAppraisedPrice - oldAppraisedPrice));
            }
            System.out.println("잔액: " + String.format("%,dG -> %,dG (-200G)", money, newMoney));
            System.out.println("\nEnter를 눌러 계속...");
            scanner.nextLine();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("진위 판정 중 오류 발생: " + e.getMessage());
            scanner.nextLine();
        }
    }

    private void showOpenCustomerHintScreen() {
        switch (showChoices(TITLE_OPEN_CUSTOMER_HINT, CHOICES_OPEN_CUSTOMER_HINT)) {
            case 1:
                openCustomerHint(2, "사기칠 거 같은 비율 (FRAUD)");
                return;
            case 2:
                openCustomerHint(1, "수집가 능력 (WELL_COLLECT)");
                return;
            case 3:
                openCustomerHint(0, "대충 관리함 (CLUMSY)");
                return;
            case 4:
                return;
        }
    }
    
    private void openCustomerHint(int hintIndex, String hintName) {
        try {
            int money = UpdateMoney.getMoney(connection, playerSession.getSessionToken());
            if (money < 50) {
                System.out.println("잔액이 부족합니다! (필요: 50G, 현재: " + money + "G)");
                scanner.nextLine();
                return;
            }
            
            DealRecordByDrcKey dealRecord = DealRecordByDrcKey.getDealRecordByDrcKey(connection, currentDrcKey);
            int playerKey = PlayerKeyByToken.getPlayerKey(connection, playerSession.getSessionToken());
            PlayerInfo playerInfo = PlayerInfo.getPlayerInfo(connection, playerKey);
            int gameSessionKey = playerInfo.gameSessionKey;
            
            int bitMask = 1 << hintIndex;
            int hintRevealedFlag;
            try {
                hintRevealedFlag = CustomerHiddenDiscovered.getHintRevealedFlag(connection, gameSessionKey, dealRecord.sellerKey);
            } catch (Exception e) {
                hintRevealedFlag = 0;
            }
            
            if ((hintRevealedFlag & bitMask) != 0) {
                System.out.println("이미 공개한 힌트입니다!");
                System.out.println("\nEnter를 눌러 계속...");
                scanner.nextLine();
                return;
            }
            
            CustomerInfo customer = CustomerInfo.getCustomerInfo(connection, dealRecord.sellerKey);
            double hintValue;
            switch (hintIndex) {
                case 0: hintValue = customer.fraud; break;
                case 1: hintValue = customer.wellCollect; break;
                case 2: hintValue = customer.clumsy; break;
                default: hintValue = 0.0;
            }
            
            int newHintRevealedFlag = hintRevealedFlag | bitMask;
            CustomerHiddenDiscovered.upsertHintRevealedFlag(connection, gameSessionKey, dealRecord.sellerKey, newHintRevealedFlag);
            
            UpdateMoney.subtractMoney(connection, playerSession.getSessionToken(), 50);
            int newMoney = UpdateMoney.getMoney(connection, playerSession.getSessionToken());
            
            System.out.println("\n[고객 힌트]");
            System.out.println("고객: " + customer.customerName);
            System.out.println("힌트: " + hintName);
            System.out.println("값: " + String.format("%.2f%%", hintValue * 100));
            System.out.println("잔액: " + String.format("%,dG -> %,dG (-50G)", money, newMoney));
            System.out.println("\nEnter를 눌러 계속...");
            scanner.nextLine();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("고객 힌트 열기 중 오류 발생: " + e.getMessage());
            scanner.nextLine();
        }
    }

    private boolean showAcceptDealScreen() {
        try {
            DealRecordByDrcKey dealRecord = DealRecordByDrcKey.getDealRecordByDrcKey(connection, currentDrcKey);
            int money = UpdateMoney.getMoney(connection, playerSession.getSessionToken());
            
            showChoices(TITLE_ACCEPT_DEAL__CHECK_BALANCE, 
                String.format("구매가: %,dG\n현재 잔액: %,dG\n\n구매 가능 여부: %s", 
                    dealRecord.purchasePrice, money, 
                    money >= dealRecord.purchasePrice ? "가능" : "불가능 (잔액 부족)"),
                CHOICES_ACCEPT_DEAL__CHECK_BALANCE, false);
            
            if (money < dealRecord.purchasePrice) {
                System.out.println("잔액이 부족하여 구매할 수 없습니다!");
                scanner.nextLine();
                return false;
            }
            
            int playerKey = PlayerKeyByToken.getPlayerKey(connection, playerSession.getSessionToken());
            PlayerInfo playerInfo = PlayerInfo.getPlayerInfo(connection, playerKey);
            int gameSessionKey = playerInfo.gameSessionKey;
            
            // 빈 전시대 위치 찾기
            int[] usedPositions = DisplayManagement.getUsedDisplayPositions(connection, playerSession.getSessionToken());
            int unlockedShowcaseCount = playerInfo.unlockedShowcaseCount;
            int emptyPos = -1;
            for (int i = 1; i <= unlockedShowcaseCount; i++) {
                boolean isUsed = false;
                for (int used : usedPositions) {
                    if (used == i) {
                        isUsed = true;
                        break;
                    }
                }
                if (!isUsed) {
                    emptyPos = i;
                    break;
                }
            }
            
            if (emptyPos == -1) {
                System.out.println("전시대가 가득 찼습니다! 먼저 아이템을 판매하거나 처리하세요.");
                scanner.nextLine();
                return false;
            }
            
            showChoices(TITLE_ACCEPT_DEAL__SAVE, 
                String.format("구매를 확정하고 전시대 %d번 위치에 배치합니다.", emptyPos),
                CHOICES_ACCEPT_DEAL__SAVE, false);
            
            UpdateDealRecord.updateBoughtDate(connection, playerSession.getSessionToken(), currentDrcKey);
            UpdateExistingItem.updateItemState(connection, dealRecord.itemKey, 1);
            DisplayManagement.addToDisplay(connection, gameSessionKey, emptyPos, dealRecord.itemKey);
            
            UpdateMoney.subtractMoney(connection, playerSession.getSessionToken(), dealRecord.purchasePrice);
            int newMoney = UpdateMoney.getMoney(connection, playerSession.getSessionToken());
            
            System.out.println("\n구매가 완료되었습니다!");
            System.out.println("전시 위치: " + emptyPos + "번");
            System.out.println("잔액: " + String.format("%,dG -> %,dG", money, newMoney));
            System.out.println("\nEnter를 눌러 계속...");
            scanner.nextLine();
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("구매 처리 중 오류 발생: " + e.getMessage());
            scanner.nextLine();
            return false;
        }
    }

    private void showDenyDealScreen() {
        try {
            DealRecordByDrcKey dealRecord = DealRecordByDrcKey.getDealRecordByDrcKey(connection, currentDrcKey);
            
            showChoices(TITLE_DENY_DEAL__REMOVE_DEAL, MESSAGE_DENY_DEAL__REMOVE_DEAL, CHOICES_DENY_DEAL__REMOVE_DEAL, false);
            
            // 무결성 제약 조건 방지를 위해 거래 먼저 삭제
            DeleteDeal.deleteDealRecord(connection, currentDrcKey);
            DeleteDeal.deleteItem(connection, dealRecord.itemKey);
            
            System.out.println("\n거래가 거절되었습니다. 기록이 삭제되었습니다.");
            System.out.println("\nEnter를 눌러 메인으로 돌아갑니다...");
            scanner.nextLine();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("거래 거절 처리 중 오류 발생: " + e.getMessage());
            scanner.nextLine();
        }
    }
}
