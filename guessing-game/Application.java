import java.util.Random;
import java.util.Scanner;

public class Application {
  public static void main(String[] args) {
    System.out.println(generateRandomNumber());
    Scanner scannerObj = new Scanner(System.in);
    int randomNumber = generateRandomNumber();

    printWelcomeMessages();
    int maxAllowedAttempts = getDifficultyLevel();
    int attempts = 0;

    System.out.println("Let's start the game!");

    while (attempts < maxAllowedAttempts) {
      System.out.println("Enter your guess:");
      attempts += 1;
      int userChoosenNumber = scannerObj.nextInt();

      if (userChoosenNumber == randomNumber) {
        System.out.println("Congratulations! You guessed the correct number in "+ attempts+ " attempts.");
        break;
      }

    }

    if (attempts == maxAllowedAttempts) {
      System.out.println("Sorry, maximum atempts reached !");
    }

    scannerObj.close();
  }

  private static int generateRandomNumber() {
    Random random = new Random();
    int randomNumber = random.nextInt(100) + 1;

    return randomNumber;
  }

  private static void printWelcomeMessages() {
    System.out.println("Welcome to the Number Guessing Game!");
    System.out.println("I'm thinking of a number between 1 and 100.");
  }

  private static int getDifficultyLevel() {
    Scanner scannerObj = new Scanner(System.in);

    System.out.println("Please select the difficulty level");
    System.out.println("1. Easy (10 chances)");
    System.out.println("2. Medium (5 chances)");
    System.out.println("3. Hard (3 chances)");
    System.out.println("Enter your choice:");

    int userChoice = scannerObj.nextInt();
    scannerObj.close();

    return userChoice;
  }
}