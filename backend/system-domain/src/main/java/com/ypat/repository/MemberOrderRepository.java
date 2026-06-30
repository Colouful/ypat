package com.ypat.repository;

import com.ypat.entity.MemberOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberOrderRepository extends JpaRepository<MemberOrder, Long> {

    MemberOrder findByOutTradeNo(@Param("outTradeNo") String outTradeNo);

    MemberOrder findById(@Param("id") Long id);

    Page<MemberOrder> findByUserIdOrderByCredateDesc(@Param("userId") Long userId, Pageable pageable);

    /**
     * 幂等标记已支付：只在 status=0 时更新为 1，避免重复回调再次开通。
     * 返回受影响行数（0 = 已被支付过或不存在，1 = 首次标记成功）。
     */
    @Modifying
    @Query("update MemberOrder o set o.status = '1', o.paidAt = :paidAt, o.wxTransactionId = :txId, o.updatedAt = :now " +
            "where o.outTradeNo = :outTradeNo and o.status = '0'")
    int markPaidIfPending(@Param("outTradeNo") String outTradeNo,
                          @Param("txId") String wxTransactionId,
                          @Param("paidAt") java.util.Date paidAt,
                          @Param("now") java.util.Date now);
}