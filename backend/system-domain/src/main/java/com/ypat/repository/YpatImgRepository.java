package com.ypat.repository;

import com.ypat.entity.YpatImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface YpatImgRepository extends JpaRepository<YpatImg, Long>, JpaSpecificationExecutor<YpatImg> {

    int deleteByYpatid(Long ypatid);

    List<YpatImg> findByYpatidOrderById(Long ypatid);


}
