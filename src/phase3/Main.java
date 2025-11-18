package phase3;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

import phase3.exceptions.CloseGameException;
import phase3.screens.IntroScreen;
import phase3.screens.LoginScreen;
import phase3.screens.MainScreen;

public class Main {
    public static final String URL = "jdbc:oracle:thin:@localhost:1523:orcl";
    public static final String USER_GAME = "MYGAME";
    public static final String USER_PASSWD = "GAME1234";
    
    public static void main(String[] args) {
        Connection connection = null;
        
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch(ClassNotFoundException e) {
            System.err.println("error = " + e.getMessage());
            System.exit(1);
        }
        try {
            connection = DriverManager.getConnection(URL, USER_GAME, USER_PASSWD);
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.err.println("Cannot get a connection: " + ex.getLocalizedMessage());
            System.err.println("Cannot get a connection: " + ex.getMessage());
            System.exit(1);
        }
        
        
        Scanner scanner = new Scanner(System.in);
        LoginScreen loginScreen = new LoginScreen(connection, scanner);
        IntroScreen introScreen = new IntroScreen(connection, scanner);
        MainScreen main = new MainScreen(connection, scanner);
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
