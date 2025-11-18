package phase3.screens;

import java.util.Scanner;

import phase3.exceptions.CloseGameException;

public abstract class BaseScreen {
    private static final int COLUMN_WIDTH = 80;

    protected Scanner scanner;

    public BaseScreen(Scanner scanner) {
        if (scanner == null) {
            throw new RuntimeException("Scanner must not null");
        }
        this.scanner = scanner;
    }

    protected int showChoices(String title, String[] choices) {
        return showChoices(title, null, choices, true);
    }

    protected int showChoices(String title, String[] choices, boolean canExit) {
        return showChoices(title, null, choices, canExit);
    }

    protected int showChoices(String title, String message, String[] choices) {
        return showChoices(title, message, choices, true);
    }

    protected int showChoices(String title, String message, String[] choices, boolean canExit) {
        int midWidth = COLUMN_WIDTH - title.length() - 2;

        System.out.println("#".repeat(COLUMN_WIDTH));
        System.out.print("#");
        System.out.print(" ".repeat(midWidth - midWidth / 2));
        System.out.print(title);
        System.out.println(" ".repeat(midWidth / 2));
        System.out.println("#");
        System.out.println("#".repeat(COLUMN_WIDTH));
        if (message != null)
            System.out.println(message);

        for (int i = 0; i < choices.length; i++) {
            System.out.printf("%d. %s\n", i + 1, choices[i]);
        }
        if (canExit) {
            System.out.println("0. 게임 종료");
        }

        String line;
        int selection;
        while (true) {
            System.out.printf("선택 (0~%d): ", choices.length);
            line = scanner.nextLine();
            try {
                selection = Integer.parseInt(line);
                if ((1 <= selection && selection <= choices.length) || (selection == 0 && canExit)) {
                    break;
                }
            } catch (NumberFormatException e) {
            }
            System.out.println("잘못된 입력입니다. 다시 입력해 주세요.");
        }

        if (selection == 0) {
            throw new CloseGameException();
        }

        return selection;
    }
}
