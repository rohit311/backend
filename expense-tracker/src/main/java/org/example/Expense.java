package org.example;

public class Expense {
  private String date;
  private String description;
  private double amount;
  private String category;

  public Expense(String date, String description, double amount, String category) {
    this.date = date;
    this.description = description;
    this.amount = amount;
    this.category = category;
  }

  // Getters and setters
  public String getDate() { return date; }
  public String getDescription() { return description; }
  public double getAmount() { return amount; }
  public String getCategory() { return category; }

  @Override
  public String toString() {
      return "Expense(date=" + date + ", description=" + description + ", amount=" + amount + ", category=" + category + ")";
  }
}