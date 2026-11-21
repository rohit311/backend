package com.example.multimodal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.multimodal.model.entity.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
}
