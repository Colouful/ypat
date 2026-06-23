package com.ypat.repository;


import com.ypat.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    Order findById(@Param("id") Long id);
    @Query("select o from Order o where o.out_trade_no= :out_trade_no")
    Order findByOut_trade_no(@Param("out_trade_no") String out_trade_no);
    List<Order> findByUseridAndStatus(@Param("userid") Long userid, @Param("status") String status);
    @Query("select count(id) from Order o where o.userid=:userid and o.type=:type and o.status=:status ")
    int countByUseridAndTypeAndStatus(@Param("userid") Long userid, @Param("type") String type, @Param("status") String status);
}
