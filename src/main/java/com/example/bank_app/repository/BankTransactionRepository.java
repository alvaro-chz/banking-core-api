package com.example.bank_app.repository;

import com.example.bank_app.model.BankTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BankTransactionRepository extends JpaRepository <BankTransaction, Long> {
    boolean existsByReferenceCode(String referenceCode);

    @Query("""
        SELECT t FROM BankTransaction t
        WHERE (t.sourceAccount.id = :accountId OR t.targetAccount.id = :accountId)
        AND (:status IS NULL OR t.transactionStatus.name = :status)
        AND (CAST(:minDate AS timestamp) IS NULL OR t.createdAt >= :minDate)
        AND (CAST(:maxDate AS timestamp) IS NULL OR t.createdAt <= :maxDate)
    """)
    Page<BankTransaction> findAllByAccountId(
            @Param("accountId") Long accountId,
            @Param("status") String status,
            @Param("minDate") LocalDateTime minDate,
            @Param("maxDate") LocalDateTime maxDate,
            Pageable pageable
    );

    @Query("""
        SELECT COUNT(DISTINCT u)
        FROM User u
        WHERE EXISTS (
            SELECT 1
            FROM BankTransaction bt
            WHERE u = bt.sourceAccount.user OR u = bt.targetAccount.user
        )
    """)
    Long getRetainedUsers();

    @Query("""
        SELECT
            CAST(t.createdAt AS LocalDate),
            t.currency.code,
            SUM(t.amount)
        FROM BankTransaction t
        GROUP BY CAST(t.createdAt AS LocalDate), t.currency.code
        ORDER BY CAST(t.createdAt AS LocalDate) ASC
    """)
    List<Object[]> getTransactionCurveDataGroupedByCurrency();

    @Query("""
        SELECT t FROM BankTransaction t
        LEFT JOIN t.sourceAccount s
        LEFT JOIN t.targetAccount ta
        WHERE (:accountNumber IS NULL OR s.accountNumber = :accountNumber OR ta.accountNumber = :accountNumber)
        AND (:status IS NULL OR t.transactionStatus = :status)
        AND (CAST(:minDate AS timestamp) IS NULL OR t.createdAt >= :minDate)
        AND (CAST(:maxDate AS timestamp) IS NULL OR t.createdAt <= :maxDate)
    """)
    Page<BankTransaction> findAllByFilter(
            @Param("accountNumber") String accountNumber,
            @Param("status") String status,
            @Param("minDate") LocalDateTime minDate,
            @Param("maxDate") LocalDateTime maxDate,
            Pageable pageable
    );
}
