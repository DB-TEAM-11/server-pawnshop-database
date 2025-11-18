package phase3;

import java.util.Scanner;

import phase3.exceptions.CloseGameException;
import phase3.screens.IntroScreen;
import phase3.screens.LoginScreen;
import phase3.screens.MainScreen;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        LoginScreen loginScreen = new LoginScreen(scanner);
        IntroScreen introScreen = new IntroScreen(scanner);
        MainScreen main = new MainScreen(scanner);
        try {
            while (true) {
                loginScreen.showLoginScreen();
                if (introScreen.showIntroScreen() == IntroScreen.NextScreen.MAIN) {
                    break;
                }
            }
            main.showMainScreen();
        } catch (CloseGameException e) {
            System.out.println("Exiting...");
        }
    }
}
