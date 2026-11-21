package com.example.multimodal.agent;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import com.example.multimodal.model.entity.Expense;
import com.example.multimodal.repository.ExpenseRepository;

@Component 
public class ExpenseTools {

    private final ExpenseRepository expenseRepository;

    public ExpenseTools(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }
    
    @Tool(description = "Logs an extracted expense amount and merchant into the database")
    public String logExpense(String merchant, double amount, String currency, List<String> items) {
        System.out.println(">>> EXECUTING logExpense TOOL: " + merchant + " | " + amount + " " + currency);
        Expense saved = expenseRepository.save(new Expense(merchant, amount, currency, items));
        System.out.println("TOOL CALLED -> " + merchant + " | " + amount + " " + currency);

        return "Saved expense ID #" + saved.getId()
                + " with " + saved.getItems().size() + " line items ("
                + amount + " " + currency + " at " + merchant + ")";
    }
}
