package com.example.multimodal.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "expense_items")
public class ExpenseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id")
    private Expense expense;

    public ExpenseItem() {}

    public ExpenseItem(String description, Expense expense) {
        this.description = description;
        this.expense = expense;
    }

    public Long getId() { return id; }
    public String getDescription() { return description; }
}
