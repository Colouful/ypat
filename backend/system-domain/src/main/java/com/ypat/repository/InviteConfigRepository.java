package com.ypat.repository;

import com.ypat.entity.InviteConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InviteConfigRepository extends JpaRepository<InviteConfig, Long> {
}
