package com.ypat.repository;

import com.ypat.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    @Query("select distinct u from User u where u.id= :id")
    User findById(@Param("id") Long id);

    User findByMobile(@Param("mobile") String mobile);

    @Override
    @EntityGraph(value = "User.all")
    Page<User> findAll(Specification<User> var1, Pageable var2);

    @Query(value = "select u.* from t_user u ,t_ypat_info y where y.userid=u.id and y.city like:city and u.profess =:profess " +
            " union select u.* from t_user u where u.city like:city and u.profess =:profess ", nativeQuery = true)
    List<User> findByCityAndProfess(@Param("city") String city, @Param("profess") String profess);
}
