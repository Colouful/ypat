package com.ypat.repository;

import com.ypat.entity.UserMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMemberRepository extends JpaRepository<UserMember, Long> {
    // 继承父类 Optional<T> findById(ID)；不要在本接口重写 findById，
    // 否则 Spring Data 会把它当作派生方法，要求实体存在 "id" 属性。
}