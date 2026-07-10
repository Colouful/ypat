package com.ypat.repository;

import com.ypat.entity.InternalTestResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface InternalTestResourceRepository extends JpaRepository<InternalTestResource, Long>, JpaSpecificationExecutor<InternalTestResource> {
    List<InternalTestResource> findByIdInAndStatus(List<Long> ids, String status);

    InternalTestResource findByUrl(String url);

    List<InternalTestResource> findByGroupNoIn(List<String> groupNos);

    List<InternalTestResource> findByGroupNoInAndStatus(List<String> groupNos, String status);

    List<InternalTestResource> findByUsedBatchNo(String usedBatchNo);

    @Modifying
    @Query("update InternalTestResource r set r.usedFlag = 1, r.usedBatchNo = :batchNo, "
            + "r.usedTargetType = :targetType, r.usedTargetId = :targetId, r.usedAt = :usedAt, "
            + "r.updatedAt = :updatedAt where r.id in :ids and (r.usedFlag is null or r.usedFlag = 0)")
    int markResourcesUsedIfAvailable(@Param("ids") List<Long> ids,
                                     @Param("batchNo") String batchNo,
                                     @Param("targetType") String targetType,
                                     @Param("targetId") Long targetId,
                                     @Param("usedAt") Date usedAt,
                                     @Param("updatedAt") Date updatedAt);

    @Modifying
    @Query("update InternalTestResource r set r.usedFlag = 0, r.usedBatchNo = null, "
            + "r.usedTargetType = null, r.usedTargetId = null, r.usedAt = null, r.updatedAt = :updatedAt "
            + "where r.usedBatchNo = :batchNo")
    int releaseByUsedBatchNo(@Param("batchNo") String batchNo, @Param("updatedAt") Date updatedAt);

    @Modifying
    @Query("update InternalTestResource r set r.usedFlag = 0, r.usedBatchNo = null, "
            + "r.usedTargetType = null, r.usedTargetId = null, r.usedAt = null, r.updatedAt = :updatedAt "
            + "where (:batchNo is null or r.usedBatchNo = :batchNo) and r.usedTargetType = :targetType "
            + "and r.usedTargetId in :targetIds")
    int releaseByUsedTargets(@Param("batchNo") String batchNo,
                             @Param("targetType") String targetType,
                             @Param("targetIds") List<Long> targetIds,
                             @Param("updatedAt") Date updatedAt);

    @Query(value = "select r.group_no from t_internal_test_resource r "
            + "join (select group_no, count(*) total_count from t_internal_test_resource "
            + "where group_no is not null group by group_no) all_g on all_g.group_no = r.group_no "
            + "where r.usage_type = 'work' and r.status = 'enabled' and r.used_flag = 0 "
            + "and r.group_no is not null "
            + "and (:mediaType is null or r.media_type = :mediaType) "
            + "and (:styleCode is null or r.style_code = :styleCode) "
            + "and (:profession is null or r.profession = :profession) "
            + "and (:province is null or r.province = :province) "
            + "and (:city is null or r.city = :city) "
            + "and (:area is null or r.area = :area) "
            + "and (:groupNo is null or r.group_no = :groupNo) "
            + "and (:keyword is null or r.title like concat('%', :keyword, '%') "
            + "or r.description like concat('%', :keyword, '%') "
            + "or r.url like concat('%', :keyword, '%') "
            + "or r.remark like concat('%', :keyword, '%')) "
            + "group by r.group_no, all_g.total_count "
            + "having count(*) = all_g.total_count "
            + "order by min(coalesce(r.sort_no, 0)), min(r.id) desc limit :offset, :limit",
            nativeQuery = true)
    List<String> findAvailableGroupNos(@Param("mediaType") String mediaType,
                                       @Param("styleCode") String styleCode,
                                       @Param("profession") String profession,
                                       @Param("province") String province,
                                       @Param("city") String city,
                                       @Param("area") String area,
                                       @Param("groupNo") String groupNo,
                                       @Param("keyword") String keyword,
                                       @Param("offset") int offset,
                                       @Param("limit") int limit);

    @Query(value = "select count(*) from (select r.group_no from t_internal_test_resource r "
            + "join (select group_no, count(*) total_count from t_internal_test_resource "
            + "where group_no is not null group by group_no) all_g on all_g.group_no = r.group_no "
            + "where r.usage_type = 'work' and r.status = 'enabled' and r.used_flag = 0 "
            + "and r.group_no is not null "
            + "and (:mediaType is null or r.media_type = :mediaType) "
            + "and (:styleCode is null or r.style_code = :styleCode) "
            + "and (:profession is null or r.profession = :profession) "
            + "and (:province is null or r.province = :province) "
            + "and (:city is null or r.city = :city) "
            + "and (:area is null or r.area = :area) "
            + "and (:groupNo is null or r.group_no = :groupNo) "
            + "and (:keyword is null or r.title like concat('%', :keyword, '%') "
            + "or r.description like concat('%', :keyword, '%') "
            + "or r.url like concat('%', :keyword, '%') "
            + "or r.remark like concat('%', :keyword, '%')) "
            + "group by r.group_no, all_g.total_count "
            + "having count(*) = all_g.total_count) available_groups",
            nativeQuery = true)
    Long countAvailableGroups(@Param("mediaType") String mediaType,
                              @Param("styleCode") String styleCode,
                              @Param("profession") String profession,
                              @Param("province") String province,
                              @Param("city") String city,
                              @Param("area") String area,
                              @Param("groupNo") String groupNo,
                              @Param("keyword") String keyword);

    @Query(value = "select r.* from t_internal_test_resource r "
            + "where r.usage_type = 'work' and r.status = 'enabled' and r.used_flag = 0 "
            + "and r.group_no is null and :groupNo is null "
            + "and (:mediaType is null or r.media_type = :mediaType) "
            + "and (:styleCode is null or r.style_code = :styleCode) "
            + "and (:profession is null or r.profession = :profession) "
            + "and (:province is null or r.province = :province) "
            + "and (:city is null or r.city = :city) "
            + "and (:area is null or r.area = :area) "
            + "and (:keyword is null or r.title like concat('%', :keyword, '%') "
            + "or r.description like concat('%', :keyword, '%') "
            + "or r.url like concat('%', :keyword, '%') "
            + "or r.remark like concat('%', :keyword, '%')) "
            + "order by coalesce(r.sort_no, 0), r.id desc limit :offset, :limit",
            nativeQuery = true)
    List<InternalTestResource> findAvailableSingleResources(@Param("mediaType") String mediaType,
                                                            @Param("styleCode") String styleCode,
                                                            @Param("profession") String profession,
                                                            @Param("province") String province,
                                                            @Param("city") String city,
                                                            @Param("area") String area,
                                                            @Param("groupNo") String groupNo,
                                                            @Param("keyword") String keyword,
                                                            @Param("offset") int offset,
                                                            @Param("limit") int limit);

    @Query(value = "select count(*) from t_internal_test_resource r "
            + "where r.usage_type = 'work' and r.status = 'enabled' and r.used_flag = 0 "
            + "and r.group_no is null and :groupNo is null "
            + "and (:mediaType is null or r.media_type = :mediaType) "
            + "and (:styleCode is null or r.style_code = :styleCode) "
            + "and (:profession is null or r.profession = :profession) "
            + "and (:province is null or r.province = :province) "
            + "and (:city is null or r.city = :city) "
            + "and (:area is null or r.area = :area) "
            + "and (:keyword is null or r.title like concat('%', :keyword, '%') "
            + "or r.description like concat('%', :keyword, '%') "
            + "or r.url like concat('%', :keyword, '%') "
            + "or r.remark like concat('%', :keyword, '%'))",
            nativeQuery = true)
    Long countAvailableSingleResources(@Param("mediaType") String mediaType,
                                       @Param("styleCode") String styleCode,
                                       @Param("profession") String profession,
                                       @Param("province") String province,
                                       @Param("city") String city,
                                       @Param("area") String area,
                                       @Param("groupNo") String groupNo,
                                       @Param("keyword") String keyword);
}
