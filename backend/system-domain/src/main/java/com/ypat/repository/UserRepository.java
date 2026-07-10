package com.ypat.repository;

import com.ypat.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    @Query("select distinct u from User u where u.id= :id")
    User findById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select distinct u from User u where u.id= :id")
    User findByIdForUpdate(@Param("id") Long id);

    User findByMobile(@Param("mobile") String mobile);

    @Override
    @EntityGraph(value = "User.all")
    Page<User> findAll(Specification<User> var1, Pageable var2);

    @Query(value = "select u.* from t_user u ,t_ypat_info y where y.userid=u.id and y.city like:city and u.profess =:profess " +
            " union select u.* from t_user u where u.city like:city and u.profess =:profess ", nativeQuery = true)
    List<User> findByCityAndProfess(@Param("city") String city, @Param("profess") String profess);

    @Modifying
    @Query("update User u set u.status = :status where u.dataFlag = 'internal_test' and (:batchNo is null or u.internalBatchNo = :batchNo)")
    int updateInternalTestUsersStatus(@Param("batchNo") String batchNo, @Param("status") String status);

    @Modifying
    @Query("update User u set u.status = :status where u.dataFlag = 'internal_test' and u.id in :userIds and (:batchNo is null or u.internalBatchNo = :batchNo)")
    int updateInternalTestUsersStatusByIds(@Param("userIds") List<Long> userIds, @Param("batchNo") String batchNo, @Param("status") String status);

    @Modifying(clearAutomatically = true)
    @Query("update User u set u.ppd = coalesce(u.ppd, 0) + :delta where u.id = :id")
    int increasePpdById(@Param("id") Long id, @Param("delta") Integer delta);

    @Query("select count(u) from User u where (u.dataFlag is null or u.dataFlag <> 'internal_test') and (:batchNo is null or u.internalBatchNo = :batchNo) and (:city is null or u.city = :city) and (:area is null or u.area = :area) and (:profess is null or u.profess = :profess) and (:gender is null or u.gender = :gender)")
    Long countRealUsersForCleanup(@Param("batchNo") String batchNo, @Param("city") String city, @Param("area") String area, @Param("profess") String profess, @Param("gender") String gender);

    @Query("select count(u) from User u where (u.dataFlag is null or u.dataFlag <> 'internal_test') and u.id in :userIds and (:batchNo is null or u.internalBatchNo = :batchNo) and (:city is null or u.city = :city) and (:area is null or u.area = :area) and (:profess is null or u.profess = :profess) and (:gender is null or u.gender = :gender)")
    Long countRealUsersForCleanupByIds(@Param("userIds") List<Long> userIds, @Param("batchNo") String batchNo, @Param("city") String city, @Param("area") String area, @Param("profess") String profess, @Param("gender") String gender);

    @Query("select u.id from User u where u.dataFlag = 'internal_test' and u.id > :afterId and (:batchNo is null or u.internalBatchNo = :batchNo) and (:city is null or u.city = :city) and (:area is null or u.area = :area) and (:profess is null or u.profess = :profess) and (:gender is null or u.gender = :gender)")
    List<Long> findInternalTestUserIdsForCleanup(@Param("afterId") Long afterId, @Param("batchNo") String batchNo, @Param("city") String city, @Param("area") String area, @Param("profess") String profess, @Param("gender") String gender, Pageable pageable);

    @Query("select u.id from User u where u.dataFlag = 'internal_test' and u.id in :userIds and (:batchNo is null or u.internalBatchNo = :batchNo) and (:city is null or u.city = :city) and (:area is null or u.area = :area) and (:profess is null or u.profess = :profess) and (:gender is null or u.gender = :gender)")
    List<Long> findInternalTestUserIdsForCleanupByIds(@Param("userIds") List<Long> userIds, @Param("batchNo") String batchNo, @Param("city") String city, @Param("area") String area, @Param("profess") String profess, @Param("gender") String gender);

    @Query("select u.internalBatchNo, count(u), min(u.regisdate) from User u where u.dataFlag = 'internal_test' and u.internalBatchNo is not null and (:batchNo is null or u.internalBatchNo = :batchNo) group by u.internalBatchNo")
    List<Object[]> aggregateInternalTestBatches(@Param("batchNo") String batchNo);
}
