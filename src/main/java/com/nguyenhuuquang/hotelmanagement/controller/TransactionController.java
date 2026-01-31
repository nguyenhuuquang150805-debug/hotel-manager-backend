package com.nguyenhuuquang.hotelmanagement.controller;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nguyenhuuquang.hotelmanagement.dto.FinanceSummaryDTO;
import com.nguyenhuuquang.hotelmanagement.dto.TransactionDTO;
import com.nguyenhuuquang.hotelmanagement.entity.enums.TransactionType;
import com.nguyenhuuquang.hotelmanagement.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(@Valid @RequestBody TransactionDTO transactionDTO) {
        TransactionDTO created = transactionService.createTransaction(transactionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions(
            @RequestParam(required = false) String period) {

        if (period != null) {
            LocalDate[] dateRange = calculateDateRange(period);
            return ResponseEntity.ok(
                    transactionService.getTransactionsByDateRange(dateRange[0], dateRange[1]));
        }

        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByType(@PathVariable String type) {
        TransactionType transactionType = TransactionType.valueOf(type.toUpperCase());
        return ResponseEntity.ok(transactionService.getTransactionsByType(transactionType));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(transactionService.getTransactionsByDateRange(startDate, endDate));
    }

    @GetMapping("/summary")
    public ResponseEntity<FinanceSummaryDTO> getFinanceSummary(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDate start, end;

        if (period != null) {
            LocalDate[] dateRange = calculateDateRange(period);
            start = dateRange[0];
            end = dateRange[1];
        } else {
            start = startDate;
            end = endDate;
        }

        return ResponseEntity.ok(transactionService.getFinanceSummary(start, end));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    private LocalDate[] calculateDateRange(String period) {
        LocalDate now = LocalDate.now();
        LocalDate start, end;

        switch (period.toLowerCase()) {
            case "yesterday":
                start = now.minusDays(1);
                end = now.minusDays(1);
                break;

            case "day":
                start = now;
                end = now;
                break;

            case "last_week":
                LocalDate lastWeekEnd = now.minusDays(now.getDayOfWeek().getValue());
                start = lastWeekEnd.minusDays(6);
                end = lastWeekEnd;
                break;

            case "week":
                start = now.minusDays(now.getDayOfWeek().getValue() - 1);
                end = now;
                break;

            case "last_month":
                LocalDate lastMonthEnd = now.with(TemporalAdjusters.firstDayOfMonth()).minusDays(1);
                start = lastMonthEnd.with(TemporalAdjusters.firstDayOfMonth());
                end = lastMonthEnd;
                break;

            case "month":
                start = now.with(TemporalAdjusters.firstDayOfMonth());
                end = now;
                break;

            case "last_year":
                start = LocalDate.of(now.getYear() - 1, 1, 1);
                end = LocalDate.of(now.getYear() - 1, 12, 31);
                break;

            case "year":
                start = now.with(TemporalAdjusters.firstDayOfYear());
                end = now;
                break;

            default:
                start = now.with(TemporalAdjusters.firstDayOfMonth());
                end = now;
        }

        return new LocalDate[] { start, end };
    }
}