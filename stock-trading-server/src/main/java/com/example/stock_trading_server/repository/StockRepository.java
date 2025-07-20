package com.example.stock_trading_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.stock_trading_server.entity.Stock;

public interface StockRepository extends JpaRepository<Stock,Long> {
    Stock findByStockSymbol(String stockSymbol);

}