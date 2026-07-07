package com.ypat.repository;

import com.ypat.entity.MessInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface MessInfoRepository extends JpaRepository<MessInfo, Long>, JpaSpecificationExecutor<MessInfo> {
    //@Query("select new MessInfo(m.id, m.type, m.content, m.status, m.messviewflag, m.linkwayflag, m.credate) from MessInfo m where m.id =:id ")
    MessInfo findById(@Param("id") Long id);

    @Query("select count(m.id) from MessInfo m join m.recper where m.type =:type and m.recper.id =:recperid")
    Long countRec(@Param("type") String type, @Param("recperid") Long recperid);

    @Query("select count(m.id) from MessInfo m join m.sendper where m.type =:type and m.sendper.id =:sendperid and m.ypatInfo.id =:ypatid")
    Long countSend(@Param("type") String type, @Param("sendperid") Long sendperid, @Param("ypatid") Long ypatid);

    @Query("select count(m.id) from MessInfo m join m.sendper where m.type =:type and m.sendper.id =:sendperid and m.ypatInfo.workId =:workId")
    Long countSendByWorkId(@Param("type") String type, @Param("sendperid") Long sendperid, @Param("workId") Long workId);

    @Query("select count(m.id) from MessInfo m join m.recper where m.type =:type and m.recper.id =:recperid and m.messviewflag =:messviewflag")
    Long countRecUnread(@Param("type") String type, @Param("recperid") Long recperid, @Param("messviewflag") String messviewflag);

    @Query("select count(m.id) from MessInfo m join m.sendper where m.type =:type and m.sendper.id =:sendperid and m.messviewflag =:messviewflag")
    Long countSendUnread(@Param("type") String type, @Param("sendperid") Long sendperid, @Param("messviewflag") String messviewflag);

    @Override
    @EntityGraph(value = "MessInfo.all")
    Page<MessInfo> findAll(Specification<MessInfo> var1, Pageable var2);

    @Modifying
    @Query("update MessInfo m set m.messviewflag=:messviewflag where m.id =:id ")
    void updateMessviewflag(@Param("messviewflag") String messviewflag, @Param("id") Long id);

    @Modifying
    @Query("update MessInfo m set m.linkwayflag=:linkwayflag where m.id =:id ")
    void updatelinkwayflag(@Param("linkwayflag") String linkwayflag, @Param("id") Long id);

}
