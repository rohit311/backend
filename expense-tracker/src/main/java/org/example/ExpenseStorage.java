package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class ExpenseStorage {
  private static final String FILENAME = "expenses.txt";

  public static ArrayList<Expense> loadExpenses() {
    ArrayList<Expense> expenseList = new ArrayList<>();

    try(BufferedReader reader = new BufferedReader(new FileReader(FILENAME))) {
      String line;

      while((line = reader.readLine()) != null ) {
        String[] parts = line.split(",");
        expenseList.add(new Expense(parts[0], parts[1], Double.parseDouble(parts[2]), parts[3]));
      }

    } catch (IOException e) {
      System.out.println("Error loading expenses: " + e.getMessage());
    }

    return expenseList;
  }

  public static void saveExpenses(ArrayList<Expense> expenseList) {
    try(PrintWriter writer = new PrintWriter(new FileWriter(FILENAME))) {
      for(Expense e : expenseList) {
        writer.println(e.getDate() + "," + e.getDescription() + "," + e.getAmount() + "," + e.getCategory());
      }
    } catch (IOException e) {
      System.out.println("Error saving expenses: " + e.getMessage());
    }
  }
}
