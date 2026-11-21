package com.example.multimodal.model.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseItem> items = new ArrayList<>();

    public Expense() {}

    public Expense(String merchant, double amount, String currency, List<String> itemDescriptions) {
        this.merchant = merchant;
        this.amount = amount;
        this.currency = currency;
        if (itemDescriptions != null) {
            for (String desc : itemDescriptions) {
                this.items.add(new ExpenseItem(desc, this));
            }
        }
    }

    public Long getId() { return id; }
    public String getMerchant() { return merchant; }
    public double getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public LocalDateTime getLoggedAt() { return loggedAt; }
    public List<ExpenseItem> getItems() { return items; }
}
