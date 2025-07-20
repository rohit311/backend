package com.example.stock_trading_server.service;

import org.springframework.grpc.server.service.GrpcService;

import com.example.stock_trading_server.entity.Stock;
import com.example.stock_trading_server.repository.StockRepository;
import com.javatechie.grpc.StockRequest;
import com.javatechie.grpc.StockResponse;
import com.javatechie.grpc.StockTradingServiceGrpc;

import io.grpc.stub.StreamObserver;

@GrpcService
public class StockTradingServiceImpl extends StockTradingServiceGrpc.StockTradingServiceImplBase {

  private final StockRepository stockRepository;


  public StockTradingServiceImpl(StockRepository stockRepository) {
    this.stockRepository = stockRepository;
  }


  @Override
  public void getStockPrice(StockRequest request, StreamObserver<StockResponse> responseObserver) {
    String symbolName = request.getStockSymbol();
    Stock stockEntity = stockRepository.findByStockSymbol(symbolName);

    StockResponse resp = StockResponse.newBuilder()
    .setStockSymbol(stockEntity.getStockSymbol())
    .setPrice(stockEntity.getPrice())
    .setTimestamp(stockEntity.getLastUpdated().toString())
    .build();

    responseObserver.onNext(resp);
    responseObserver.onCompleted();
  }
}
