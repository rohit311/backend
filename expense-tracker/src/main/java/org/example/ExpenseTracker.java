package org.example;

import java.util.ArrayList;
import java.util.Scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"org.example"})
public class ExpenseTracker {
  public static void main(String[] args) {
    SpringApplication.run(ExpenseTracker.class, args);

    Scanner scanner = new Scanner(System.in);
    ArrayList<Expense> expenseList = ExpenseStorage.loadExpenses();

    while(true) {
      System.out.println("\nExpense Tracker Menu:");
      System.out.println("1. Add Expense");
      System.out.println("2. Update Expense");
      System.out.println("3. Delete Expense");
      System.out.println("4. View All Expenses");
      System.out.println("5. View Summary of All Expenses");
      System.out.println("6. View Summary of Expenses for a Specific Month");
      System.out.println("7. Exit");
      System.out.print("Enter your choice: ");
      int choice = scanner.nextInt();
      scanner.nextLine(); // Consume newline

      switch (choice) {
        case 1:
          addExpenses(scanner, expenseList);
          break;
        case 2:
          updateExpense(scanner, expenseList);
          break;
        case 3:
          deleteExpense(scanner, expenseList);
          break;
        case 4:
          viewAllExpenses(expenseList);
          break;
        case 5:
          viewSummary(expenseList);
          break;
        case 6:
          System.out.println("Coming soon");
          break;
        case 7:
          ExpenseStorage.saveExpenses(expenseList);
          System.out.println("Expenses saved. Exiting...");
          return;
        default:
        System.out.println("Invalid choice. Please try again.");
      }
    }
  }

  private static Expense inputExpense(Scanner scanner) {
    System.out.print("Enter date (YYYY-MM-DD): ");
    String date = scanner.nextLine();

    System.out.print("Enter description: ");
    String description = scanner.nextLine();

    System.out.print("Enter amount: ");
    double amount = scanner.nextDouble();
    scanner.nextLine(); // Consume newline

    System.out.print("Enter category: ");
    String category = scanner.nextLine();

    return new Expense(date, description, amount, category);
  }

  private static void addExpenses(Scanner scanner, ArrayList<Expense> expenseList) {
    expenseList.add(inputExpense(scanner));
    System.out.println("Expense added.");
  }

  private static void updateExpense(Scanner scanner, ArrayList<Expense> expenseList) {
    System.out.print("Enter the index of the expense to update: ");
    int index = scanner.nextInt();
    scanner.nextLine(); // Consume newline

    if (index >= 0 && index < expenseList.size()) {
      expenseList.set(index, inputExpense(scanner));
      System.out.println("Expense updated.");
    } else {
      System.out.println("Invalid index");
    }
  }

  private static void deleteExpense(Scanner scanner, ArrayList<Expense> expenseList) {
    System.out.println("Enter the index of the expense to delete:");
    int index = scanner.nextInt();
    scanner.nextLine(); // Consume newline

    if (index >=0 && index < expenseList.size()) {
      expenseList.remove(index);
      System.out.println("Expense removed");
    } else {
      System.out.println("Invalid index");
    }
  }

  private static void viewAllExpenses(ArrayList<Expense> expenseList) {
    for (int i = 0; i < expenseList.size(); i++) {
        System.out.println(i + ": " + expenseList.get(i));
    }
  }

  private static void viewSummary(ArrayList<Expense> expenseList) {
    double totalExpense = 0;

    for (Expense e : expenseList) {
      totalExpense += e.getAmount();
    }

    System.out.println("Total expenses: "+ totalExpense);
  }

}
