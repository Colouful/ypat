package com.ypat.repository;

import com.ypat.entity.UserMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMemberRepository extends JpaRepository<UserMember, Long> {
    UserMember findById(@Param("id") Long id);
}