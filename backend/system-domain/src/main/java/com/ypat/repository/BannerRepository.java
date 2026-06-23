package com.ypat.repository;

import com.ypat.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long>, JpaSpecificationExecutor<Banner> {
    Banner findById(@Param("id") Long id);
}
