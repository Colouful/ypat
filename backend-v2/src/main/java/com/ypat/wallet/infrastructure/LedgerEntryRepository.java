package com.ypat.wallet.infrastructure;

import com.ypat.wallet.domain.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    List<LedgerEntry> findByUserIdOrderByCreatedAtDesc(Long userId);
}