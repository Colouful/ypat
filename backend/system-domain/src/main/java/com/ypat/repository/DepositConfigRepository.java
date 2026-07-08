package com.ypat.repository;

import com.ypat.entity.DepositConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepositConfigRepository extends JpaRepository<DepositConfig, Long> {
}
