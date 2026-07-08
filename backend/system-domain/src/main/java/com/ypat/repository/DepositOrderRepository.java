package com.ypat.repository;

import com.ypat.entity.DepositOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface DepositOrderRepository extends JpaRepository<DepositOrder, Long>, JpaSpecificationExecutor<DepositOrder> {

    DepositOrder findByOutTradeNo(@Param("outTradeNo") String outTradeNo);

    Page<DepositOrder> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    Page<DepositOrder> findByUserIdAndStatusOrderByCreatedAtDesc(@Param("userId") Long userId,
                                                                 @Param("status") String status,
                                                                 Pageable pageable);

    @Query("select o.userId from DepositOrder o where o.outTradeNo = :outTradeNo")
    Long findUserIdByOutTradeNo(@Param("outTradeNo") String outTradeNo);

    @Modifying
    @Query("update DepositOrder o set o.status = 'PAID', o.transactionId = :txId, o.paidAt = :paidAt, o.updatedAt = :now " +
            "where o.outTradeNo = :outTradeNo and o.status = 'PENDING'")
    int markPaidIfPending(@Param("outTradeNo") String outTradeNo,
                          @Param("txId") String txId,
                          @Param("paidAt") Date paidAt,
                          @Param("now") Date now);
}
