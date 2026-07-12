package com.ypat.repository;

import com.ypat.entity.PpdSceneConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PpdSceneConfigRepository extends JpaRepository<PpdSceneConfig, Long> {
    PpdSceneConfig findByScene(String scene);
    List<PpdSceneConfig> findAllByOrderBySceneAsc();
}
