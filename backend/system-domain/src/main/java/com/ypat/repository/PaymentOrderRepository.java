package com.ypat.repository;

import com.ypat.entity.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long>, JpaSpecificationExecutor<PaymentOrder> {

    PaymentOrder findByOutTradeNo(@Param("outTradeNo") String outTradeNo);

    @Modifying
    @Query("update PaymentOrder o set o.status = 'PAID', o.transactionId = :txId, o.paidAt = :paidAt, " +
            "o.updatedAt = :now, o.notifyEventId = :eventId, o.notifyDigest = :digest " +
            "where o.outTradeNo = :outTradeNo and o.status = 'PENDING'")
    int markPaidIfPending(@Param("outTradeNo") String outTradeNo,
                          @Param("txId") String txId,
                          @Param("paidAt") Date paidAt,
                          @Param("now") Date now,
                          @Param("eventId") String eventId,
                          @Param("digest") String digest);
}
