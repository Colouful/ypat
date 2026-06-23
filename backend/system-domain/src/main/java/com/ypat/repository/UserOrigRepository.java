package com.ypat.repository;

import com.ypat.entity.UserOrig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserOrigRepository extends JpaRepository<UserOrig, Long>, JpaSpecificationExecutor<UserOrig> {

}
