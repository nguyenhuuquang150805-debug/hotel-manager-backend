package com.nguyenhuuquang.hotelmanagement.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nguyenhuuquang.hotelmanagement.dto.FinanceSummaryDTO;
import com.nguyenhuuquang.hotelmanagement.dto.TransactionDTO;
import com.nguyenhuuquang.hotelmanagement.entity.Transaction;
import com.nguyenhuuquang.hotelmanagement.entity.enums.LogType;
import com.nguyenhuuquang.hotelmanagement.entity.enums.PaymentMethod;
import com.nguyenhuuquang.hotelmanagement.entity.enums.TransactionType;
import com.nguyenhuuquang.hotelmanagement.exception.ResourceNotFoundException;
import com.nguyenhuuquang.hotelmanagement.repository.TransactionRepository;
import com.nguyenhuuquang.hotelmanagement.service.SystemLogService;
import com.nguyenhuuquang.hotelmanagement.service.TransactionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepo;
    private final SystemLogService logService;

    @Override
    @Transactional
    public TransactionDTO createTransaction(TransactionDTO transactionDTO) {
        Transaction transaction = Transaction.builder()
                .type(TransactionType.valueOf(transactionDTO.getType()))
                .category(transactionDTO.getCategory())
                .amount(transactionDTO.getAmount())
                .description(transactionDTO.getDescription())
                .transactionDate(transactionDTO.getTransactionDate())
                .paymentMethod(PaymentMethod.valueOf(transactionDTO.getPaymentMethod()))
                .build();

        transaction = transactionRepo.save(transaction);

        String logMessage = transaction.getType() == TransactionType.INCOME
                ? String.format("Đã ghi nhận thu nhập: %s", transaction.getDescription())
                : String.format("Đã ghi nhận chi phí: %s", transaction.getDescription());

        logService.log(LogType.SUCCESS, "Giao dịch mới", "Admin", logMessage,
                String.format("Số tiền: %s, Phương thức: %s",
                        transaction.getAmount(), transaction.getPaymentMethod()));

        return convertToDTO(transaction);
    }

    @Override
    public List<TransactionDTO> getAllTransactions() {
        return transactionRepo.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionDTO> getTransactionsByType(TransactionType type) {
        return transactionRepo.findByType(type)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionDTO> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        return transactionRepo.findByTransactionDateBetween(startDate, endDate)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public FinanceSummaryDTO getFinanceSummary(LocalDate startDate, LocalDate endDate) {
        Double income = transactionRepo.sumAmountByTypeAndDateRange(
                TransactionType.INCOME, startDate, endDate);
        Double expense = transactionRepo.sumAmountByTypeAndDateRange(
                TransactionType.EXPENSE, startDate, endDate);

        BigDecimal totalIncome = income != null ? BigDecimal.valueOf(income) : BigDecimal.ZERO;
        BigDecimal totalExpense = expense != null ? BigDecimal.valueOf(expense) : BigDecimal.ZERO;
        BigDecimal profit = totalIncome.subtract(totalExpense);

        return FinanceSummaryDTO.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .profit(profit)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    @Override
    @Transactional
    public void deleteTransaction(Long id) {
        Transaction transaction = transactionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch"));

        String description = transaction.getDescription();
        transactionRepo.delete(transaction);

        logService.log(LogType.WARNING, "Xóa giao dịch", "Admin",
                String.format("Đã xóa giao dịch: %s", description));
    }

    private TransactionDTO convertToDTO(Transaction transaction) {
        return TransactionDTO.builder()
                .id(transaction.getId())
                .type(transaction.getType().name())
                .category(transaction.getCategory())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .paymentMethod(transaction.getPaymentMethod().name())
                .bookingId(transaction.getBooking() != null ? transaction.getBooking().getId() : null)
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}