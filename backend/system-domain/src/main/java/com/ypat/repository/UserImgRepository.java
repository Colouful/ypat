package com.ypat.repository;

import com.ypat.entity.UserImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserImgRepository extends JpaRepository<UserImg, Long>, JpaSpecificationExecutor<UserImg> {

}
