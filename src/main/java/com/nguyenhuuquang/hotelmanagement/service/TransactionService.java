package com.nguyenhuuquang.hotelmanagement.service;

import java.time.LocalDate;
import java.util.List;

import com.nguyenhuuquang.hotelmanagement.dto.FinanceSummaryDTO;
import com.nguyenhuuquang.hotelmanagement.dto.TransactionDTO;
import com.nguyenhuuquang.hotelmanagement.entity.enums.TransactionType;

public interface TransactionService {
    TransactionDTO createTransaction(TransactionDTO transactionDTO);

    List<TransactionDTO> getAllTransactions();

    List<TransactionDTO> getTransactionsByType(TransactionType type);

    List<TransactionDTO> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate);

    FinanceSummaryDTO getFinanceSummary(LocalDate startDate, LocalDate endDate);

    void deleteTransaction(Long id);
}