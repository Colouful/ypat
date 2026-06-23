package com.ypat.repository;

import com.ypat.entity.UserYpat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserYpatRepository extends JpaRepository<UserYpat, Long>, JpaSpecificationExecutor<UserYpat> {


    @Query("select count(uy.id) from UserYpat uy where uy.user.id= :userid and uy.ypatInfo.id= :ypatid ")
    int countByUseridAndYpatid(@Param("userid") Long userid, @Param("ypatid") Long ypatid);

    @Override
    @EntityGraph(value = "UserYpat.all")
    Page<UserYpat> findAll(Specification<UserYpat> var1, Pageable var2);

}
