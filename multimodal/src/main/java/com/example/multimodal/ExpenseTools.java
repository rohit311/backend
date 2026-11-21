package com.example.multimodal;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component 
public class ExpenseTools {

    private final ExpenseRepository expenseRepository;

    public ExpenseTools(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }
    
    @Tool(description = "Logs an extracted expense amount and merchant into the database")
    public String logExpense(String merchant, double amount, String currency) {
        System.out.println(">>> EXECUTING logExpense TOOL: " + merchant + " | " + amount + " " + currency);
        Expense saved = expenseRepository.save(new Expense(merchant, amount, currency));
        System.out.println("TOOL CALLED -> " + merchant + " | " + amount + " " + currency);

        return "Saved expense: " + saved.getId() + " " + currency + " at " + merchant;
    }
}
