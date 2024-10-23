package com.sobczyk.walletMicroservices.service;

import com.sobczyk.walletMicroservices.dto.PositionPerformanceDto;
import com.sobczyk.walletMicroservices.dto.requests.PositionPerformanceRequest;
import com.sobczyk.walletMicroservices.dto.responses.PositionPerformanceResponse;
import com.sobczyk.walletMicroservices.dto.responses.TransactionResponse;
import com.sobczyk.walletMicroservices.entity.Transaction;
import com.sobczyk.walletMicroservices.entity.TransactionType;
import com.sobczyk.walletMicroservices.position.performance.PositionPerfKey;
import com.sobczyk.walletMicroservices.position.performance.PositionPerfValue;
import com.sobczyk.walletMicroservices.position.performance.TimeSeries;
import com.sobczyk.walletMicroservices.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PositionPerfService {

    private final TransactionRepository transactionRepository;
    private final FinancialDataProvider financialDataProvider;
    private final PositionPerfSummaryService positionPerfSummaryService;
    private final FinancialNewsProvider financialNewsProvider;

    private final Map<PositionPerfKey, PositionPerfValue> positionsMap = new HashMap<>();
    private Map<PositionPerfKey, BigDecimal> priceMap = new HashMap<>();
    private PositionPerformanceDto previousPerf;
    private PositionPerformanceResponse response;

    public PositionPerformanceResponse getPositionPerformance(PositionPerformanceRequest request) {
        clearVariables();
        TimeSeries timeSeries = TimeSeries.getById(request.timeSeries());
        transactionRepository.findTransactions(request.investorId())
                .forEach(t -> {
                    onTickerChangedAction(timeSeries, t.getAsset().getTicker());
                    populatePositions(t.getAsset().getTicker(), previousPerf.getQuantity(), previousPerf.getInvestedValue(),
                            timeSeries, t.getTransaction_date(), false);
                    setPreviousPerf(t);
                    addTransactionToResponse(t);
                });
        //populate from now to last transaction
        populatePositions(previousPerf.getTicker(), previousPerf.getQuantity(), previousPerf.getInvestedValue(),
                timeSeries, LocalDate.now().plusDays(1), !this.response.getTransactions().isEmpty());
        this.summarizePortfolio(timeSeries);
        return response;
    }

    private void setPreviousPerf(Transaction t) {
        previousPerf.setQuantity(afterTransactionQuantityCalc(previousPerf.getQuantity(), t.getTransaction_quantity(), t.getTransactionType()));
        previousPerf.setInvestedValue(previousPerf.getInvestedValue().add(t.getTransaction_quantity().multiply(t.getPurchase_price()).add(t.getTrading_fees() == null ? BigDecimal.ZERO : t.getTrading_fees())));
        previousPerf.setTicker(t.getAsset().getTicker());
    }

    private BigDecimal afterTransactionQuantityCalc(BigDecimal currentAmount, BigDecimal changedAmount, TransactionType transactionType) {
        if (TransactionType.BUY.equals(transactionType) || TransactionType.DEPOSIT.equals(transactionType)) {
            return currentAmount.add(changedAmount);
        } else {
            return currentAmount.add(changedAmount.negate());
        }
    }

    private void populatePositions(String ticker, BigDecimal afterTransQuantity, BigDecimal investedValue,
                                   TimeSeries timeSeries, LocalDate date, boolean lastPosition) {
        while ((LocalDate.now().minusMonths(timeSeries.getSerieInMonth()).isBefore(date) || lastPosition) && !this.priceMap.isEmpty()) {
            PositionPerfKey key = new PositionPerfKey(date, ticker);
            PositionPerfValue posValue = new PositionPerfValue(afterTransQuantity);
            posValue.setInvestedValue(investedValue);
            if (priceMap.get(key) != null) {
                posValue.setMarketValue(afterTransQuantity.multiply(priceMap.get(key)));
            } else {
                date = date.minusDays(1);
                continue;
            }
            if (this.positionsMap.containsKey(key)) {
                if (lastPosition) {
                    this.response.getCurrentPositions().add(convertPositionMapEntryToDto(new AbstractMap.SimpleEntry<>(key, posValue)));
                    this.response.getPositionsNews().addAll(financialNewsProvider.getFinancialNews(ticker));
                }
                break;
            }
            this.positionsMap.put(key, posValue);
            if (lastPosition) {
                this.response.getCurrentPositions().add(convertPositionMapEntryToDto(new AbstractMap.SimpleEntry<>(key, posValue)));
                this.response.getPositionsNews().addAll(financialNewsProvider.getFinancialNews(ticker));
                lastPosition = false;
            }
            date = date.minusDays(1);
        }
    }

    private void onTickerChangedAction(TimeSeries timeSeries, String ticker) {
        if (!ticker.equals(previousPerf.getTicker())) {
            if (previousPerf.getTicker() != null) {
                populatePositions(previousPerf.getTicker(), previousPerf.getQuantity(), previousPerf.getInvestedValue(),
                        timeSeries, LocalDate.now().plusDays(1), true);
            }
            priceMap = financialDataProvider.retrieveStockData(ticker, timeSeries.getTimeSpan(), timeSeries.getMultiplier(),
                    LocalDate.now().minusMonths(timeSeries.getSerieInMonth()));
            previousPerf.clearValues();
        }
    }

    private void summarizePortfolio(TimeSeries timeSeries) {
        positionPerfSummaryService.generateOverallPosition(timeSeries, this.positionsMap);
        response.setCash(positionPerfSummaryService.cashValue);
        response.setOverall(positionPerfSummaryService.getOvValue());
        response.setAllTimePositions(new ArrayList<>(this.positionsMap.entrySet().stream()
                .map(this::convertPositionMapEntryToDto)
                .toList()));
        response.setAllTimeOverall(new ArrayList<>(positionPerfSummaryService.getAllTimeOverall().entrySet().stream()
                .map(this::convertPositionMapEntryToDto)
                .toList()));
    }

    private void addTransactionToResponse(Transaction transaction) {
        response.getTransactions().add(TransactionResponse.builder()
                .ticker(transaction.getAsset().getTicker())
                .date(transaction.getTransaction_date())
                .assetType(transaction.getAsset().getAssetType().name())
                .transactionType(transaction.getTransactionType().name())
                .quantity(transaction.getTransaction_quantity())
                .price(transaction.getPurchase_price())
                .fees(transaction.getTrading_fees())
                .description(transaction.getDescription())
                .build());
    }

    private PositionPerformanceDto convertPositionMapEntryToDto(Map.Entry<PositionPerfKey, PositionPerfValue> entry) {
        return PositionPerformanceDto.builder()
                .date(entry.getKey().getDate())
                .ticker(entry.getKey().getTicker())
                .quantity(entry.getValue().getQuantity())
                .marketValue(entry.getValue().getMarketValue())
                .investedValue(entry.getValue().getInvestedValue())
                .TWR(entry.getValue().getTWR())
                .build();
    }

    private void clearVariables() {
        this.positionsMap.clear();
        this.priceMap.clear();
        this.previousPerf = new PositionPerformanceDto();
        this.response = new PositionPerformanceResponse();
    }
}

