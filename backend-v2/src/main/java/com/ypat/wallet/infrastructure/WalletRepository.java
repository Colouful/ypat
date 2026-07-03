package com.ypat.wallet.infrastructure;

import com.ypat.wallet.domain.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * PR-19: Wallet repository.
 *
 * {@link #findByUserIdForUpdate(long)} is the locking read that
 * {@link com.ypat.wallet.application.WalletService} uses at the
 * start of every ledger transaction. Spring Data JPA translates
 * {@code @Lock(PESSIMISTIC_WRITE)} into MySQL's
 * {@code SELECT ... FOR UPDATE}, which is the row-level lock
 * V1.1 §3.2 requires for strongly-consistent wallet writes.
 */
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.userId = :userId")
    Optional<Wallet> findByUserIdForUpdate(@Param("userId") Long userId);
}