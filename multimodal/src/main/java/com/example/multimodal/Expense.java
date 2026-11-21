package com.example.multimodal;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "expenses")
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String merchant;
    private double amount;
    private String currency;
    private LocalDateTime loggedAt = LocalDateTime.now();

    public Expense() {}

    public Expense(String merchant, double amount, String currency) {
        this.merchant = merchant;
        this.amount = amount;
        this.currency = currency;
    }

    public Long getId() { return id; }
    public String getMerchant() { return merchant; }
    public double getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public LocalDateTime getLoggedAt() { return loggedAt; }
}
