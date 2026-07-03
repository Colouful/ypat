package com.ypat.service;

import com.ypat.PageQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.WorkDetailQo;
import com.ypat.WorkListItem;
import com.ypat.WorkListQo;
import com.ypat.WorkQuickApplyQo;
import com.ypat.WorkSubmitQo;
import com.ypat.entity.User;
import com.ypat.entity.Work;
import com.ypat.entity.WorkComplain;
import com.ypat.entity.WorkFavorite;
import com.ypat.entity.WorkLike;
import com.ypat.entity.WorkMedia;
import com.ypat.entity.WorkTag;
import com.ypat.entity.WorkTagRel;
import com.ypat.enums.UserProfess;
import com.ypat.enums.WorkStatus;
import com.ypat.enums.YpatTarget;
import com.ypat.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import com.ypat.repository.WorkComplainRepository;
import com.ypat.repository.WorkFavoriteRepository;
import com.ypat.repository.WorkLikeRepository;
import com.ypat.repository.WorkMediaRepository;
import com.ypat.repository.WorkRepository;
import com.ypat.repository.WorkTagRelRepository;
import com.ypat.repository.WorkTagRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 作品核心服务
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class WorkService {

    @Autowired private WorkRepository workRepository;
    @Autowired private WorkMediaRepository workMediaRepository;
    @Autowired private WorkTagRepository workTagRepository;
    @Autowired private WorkTagRelRepository workTagRelRepository;
    @Autowired private WorkLikeRepository workLikeRepository;
    @Autowired private WorkFavoriteRepository workFavoriteRepository;
    @Autowired private WorkComplainRepository workComplainRepository;
    @Autowired private UserRepository userRepository;

    private static final Pattern MOBILE = Pattern.compile("(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}");
    private static final Pattern WX = Pattern.compile("(?i)(wx|wechat|vx)[^a-z0-9]?[a-z][a-z0-9_-]{5,19}", Pattern.CASE_INSENSITIVE);
    private static final Pattern QQ = Pattern.compile("(qq|扣扣)[^a-z0-9]?[1-9][0-9]{4,11}", Pattern.CASE_INSENSITIVE);
    private static final int TAG_LIMIT = 5;
    private static final int MAX_TOTAL_IMG_SIZE = 100 * 1024 * 1024;
    private static final int MAX_VIDEO_SIZE = 200 * 1024 * 1024;
    private static final int MAX_IMG_COUNT = 9;
    private static final int MAX_VIDEO_COUNT = 1;

    /**
     * 提交作品（事务方法）
     */
    public Work submit(WorkSubmitQo qo) {
        if (qo == null) throw new SysException(ResponseCode.FAIL_PARA);
        if (StringUtils.isBlank(qo.getUserid())) throw new SysException(ResponseCode.FAIL_AUTH);
        if (StringUtils.isBlank(qo.getDescription())) throw new SysException(ResponseCode.FAIL_PARA);
        if (qo.getDescription().length() < 5 || qo.getDescription().length() > 500) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        // 敏感联系方式
        if (containsContact(qo.getDescription())) {
            throw new SysException(ResponseCode.FAIL_DESC_SENSITIVE);
        }
        if (StringUtils.isNotBlank(qo.getDevice()) && qo.getDevice().length() > 100) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        if (StringUtils.isNotBlank(qo.getShootLocation()) && qo.getShootLocation().length() > 100) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        if (!YesNoValid(qo.getReturnPhotoFlag())) throw new SysException(ResponseCode.FAIL_PARA);
        if (!"1".equals(qo.getMediaType()) && !"2".equals(qo.getMediaType())) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }

        Long userId = Long.parseLong(qo.getUserid());
        User user = userRepository.findById(userId);
        if (user == null) throw new SysException(ResponseCode.FAIL_AUTH);

        // 解析 mediaIds / tagIds
        List<Long> mediaIds = qo.getMediaIdList();
        List<Long> tagIds = qo.getTagIdList();
        if (mediaIds.isEmpty()) throw new SysException(ResponseCode.FAIL_PARA, "至少上传一个媒体");
        if (tagIds.isEmpty()) throw new SysException(ResponseCode.FAIL_PARA, "请选择主题标签");
        if (tagIds.size() > TAG_LIMIT) throw new SysException(ResponseCode.FAIL_TAG_OVERCOUNT);

        // 校验媒体数量
        if ("1".equals(qo.getMediaType())) {
            if (mediaIds.size() > MAX_IMG_COUNT) throw new SysException(ResponseCode.FAIL_IMG_OVERCOUNT);
        } else if ("2".equals(qo.getMediaType())) {
            if (mediaIds.size() > MAX_VIDEO_COUNT) throw new SysException(ResponseCode.FAIL_VIDEO_COUNT);
        }

        // 校验媒体归属 + 累计大小
        List<WorkMedia> medias = workMediaRepository.findByIdInAndUserId(mediaIds, userId);
        if (medias.size() != mediaIds.size()) {
            throw new SysException(ResponseCode.FAIL_VAL, "媒体不属于当前用户");
        }
        long totalSize = 0L;
        for (WorkMedia m : medias) totalSize += (m.getFileSize() == null ? 0 : m.getFileSize());
        if ("1".equals(qo.getMediaType()) && totalSize > MAX_TOTAL_IMG_SIZE) {
            throw new SysException(ResponseCode.FAIL_IMG_TOTAL_OVERSIZE);
        }
        if ("2".equals(qo.getMediaType()) && totalSize > MAX_VIDEO_SIZE) {
            throw new SysException(ResponseCode.FAIL_VIDEO_OVERSIZE);
        }

        // 校验标签存在
        for (Long tagId : tagIds) {
            if (workTagRepository.findOne(tagId) == null) {
                throw new SysException(ResponseCode.FAIL_PARA, "标签不存在");
            }
        }

        // 创建作品
        Work work = new Work();
        work.setUserid(userId);
        work.setDescription(qo.getDescription());
        work.setDevice(qo.getDevice());
        work.setShootLocation(qo.getShootLocation());
        work.setReturnPhotoFlag("1".equals(qo.getReturnPhotoFlag()) ? 1 : 0);
        work.setMediaType(qo.getMediaType());
        work.setIsNationwide("1".equals(qo.getIsNationwide()) ? 1 : 0);
        work.setStatus(WorkStatus.ytj.value);
        work.setReadCount(0);
        work.setLikeCount(0);
        work.setFavoriteCount(0);
        work.setPublishTime(new Date());
        work.setCreatedAt(new Date());
        work.setUpdatedAt(new Date());
        work.setDeletedFlag(0);
        work.setCity(user.getCity());
        work.setArea(user.getArea());
        work = workRepository.save(work);

        // 绑定媒体
        workMediaRepository.bindWorkId(work.getId(), mediaIds, userId);
        // 更新 sortNo
        for (int i = 0; i < medias.size(); i++) {
            WorkMedia m = medias.get(i);
            m.setWorkId(work.getId());
            m.setSortNo(i);
            workMediaRepository.save(m);
        }
        // 绑定标签
        for (Long tagId : tagIds) {
            WorkTagRel rel = new WorkTagRel();
            rel.setWorkId(work.getId());
            rel.setTagId(tagId);
            rel.setCreatedAt(new Date());
            workTagRelRepository.save(rel);
        }
        return work;
    }

    /**
     * 详情（含发布者信息、媒体、标签、点赞/收藏状态）
     */
    public Map<String, Object> getDetail(WorkDetailQo qo) {
        if (qo == null || StringUtils.isBlank(qo.getId())) throw new SysException(ResponseCode.FAIL_PARA);
        Long workId = Long.parseLong(qo.getId());
        Long viewerId = null;
        if (StringUtils.isNotBlank(qo.getViewerUserId())) {
            try { viewerId = Long.parseLong(qo.getViewerUserId()); } catch (NumberFormatException ignored) {}
        }

        Work work = workRepository.findByIdAndStatusAndDeletedFlag(workId, WorkStatus.shtg.value, 0);
        // 作者本人可看任意状态
        if (work == null && viewerId != null) {
            Work mine = workRepository.findByIdAndUseridAndDeletedFlag(workId, viewerId, 0);
            work = mine;
        }
        if (work == null) throw new SysException(ResponseCode.FAIL_WORK_NOT_FOUND);

        // 阅读量 +1（异步不阻塞）
        try { workRepository.incrReadCount(workId); } catch (RuntimeException ignored) {}

        Map<String, Object> res = new HashMap<>();
        res.put("id", work.getId());
        res.put("description", work.getDescription());
        res.put("device", work.getDevice());
        res.put("shootLocation", work.getShootLocation());
        res.put("returnPhotoFlag", work.getReturnPhotoFlag());
        res.put("mediaType", work.getMediaType());
        res.put("isNationwide", work.getIsNationwide());
        res.put("status", work.getStatus());
        res.put("readCount", work.getReadCount());
        res.put("likeCount", work.getLikeCount());
        res.put("favoriteCount", work.getFavoriteCount());
        res.put("publishTime", work.getPublishTime());

        // 媒体
        List<WorkMedia> medias = workMediaRepository.findByWorkIdOrderBySortNoAsc(workId);
        List<Map<String, Object>> mediaList = new ArrayList<>();
        for (WorkMedia m : medias) {
            Map<String, Object> mm = new HashMap<>();
            mm.put("id", m.getId());
            mm.put("type", m.getType());
            mm.put("url", m.getUrl());
            mm.put("fileSize", m.getFileSize());
            mm.put("width", m.getWidth());
            mm.put("height", m.getHeight());
            mm.put("duration", m.getDuration());
            mediaList.add(mm);
        }
        res.put("medias", mediaList);

        // 标签
        List<WorkTagRel> rels = workTagRelRepository.findByWorkId(workId);
        List<Map<String, Object>> tagList = new ArrayList<>();
        for (WorkTagRel r : rels) {
            WorkTag tag = workTagRepository.findOne(r.getTagId());
            if (tag != null) {
                Map<String, Object> t = new HashMap<>();
                t.put("id", tag.getId());
                t.put("name", tag.getName());
                t.put("code", tag.getCode());
                tagList.add(t);
            }
        }
        res.put("tags", tagList);

        // 发布者
        User user = userRepository.findById(work.getUserid());
        Map<String, Object> userMap = new HashMap<>();
        if (user != null) {
            userMap.put("id", user.getId());
            userMap.put("nickname", user.getNickname());
            userMap.put("avatar", user.getAvatarurl());
            userMap.put("gender", user.getGender());
            userMap.put("profession", user.getProfess());
            userMap.put("city", user.getCity());
            userMap.put("mobile", null); // 不返回
            userMap.put("activeTime", "刚刚");
        }
        res.put("user", userMap);

        // 当前用户状态
        res.put("isLiked", viewerId != null && workLikeRepository.existsByWorkIdAndUserId(workId, viewerId));
        res.put("isFavorited", viewerId != null && workFavoriteRepository.existsByWorkIdAndUserId(workId, viewerId));
        res.put("isOwner", viewerId != null && viewerId.equals(work.getUserid()));
        return res;
    }

    /**
     * 列表（按筛选条件）
     */
    public Map<String, Object> pageList(WorkListQo qo) {
        if (qo == null) qo = new WorkListQo();
        final int page = qo.getPage() == null || qo.getPage() < 1 ? 1 : qo.getPage();
        final int size = qo.getSize() == null || qo.getSize() < 1 ? 10 : Math.min(qo.getSize(), 20);
        final String category = qo.getCategory();
        final String city = qo.getCity();
        final String gender = qo.getGender();
        final String profession = qo.getProfession();
        final String tagIds = qo.getTagIds();
        final Long viewerId = StringUtils.isNotBlank(qo.getViewerUserId())
            ? Long.parseLong(qo.getViewerUserId()) : null;

        Specification<Work> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            // 仅展示已通过 + 未下架 + 未删除
            ps.add(cb.equal(root.get("status"), WorkStatus.shtg.value));
            ps.add(cb.equal(root.get("deletedFlag"), 0));

            if (StringUtils.isNotBlank(city)) {
                ps.add(cb.equal(root.get("city"), city));
            }
            if (StringUtils.isNotBlank(profession)) {
                Join<Object, Object> userJoin = root.join("user", JoinType.LEFT);
                ps.add(cb.equal(userJoin.get("profess"), profession));
            }
            if (StringUtils.isNotBlank(gender)) {
                Join<Object, Object> userJoin = root.join("user", JoinType.LEFT);
                ps.add(cb.equal(userJoin.get("gender"), gender));
            }
            if (StringUtils.isNotBlank(tagIds)) {
                // 标签筛选：IN 子查询（简化为查询后再过滤）
            }
            // category 映射
            if (StringUtils.isNotBlank(category)) {
                if ("模特".equals(category)) {
                    Join<Object, Object> userJoin = root.join("user", JoinType.LEFT);
                    ps.add(cb.equal(userJoin.get("profess"), UserProfess.mt.value));
                } else if ("摄影".equals(category) || "摄影师".equals(category)) {
                    Join<Object, Object> userJoin = root.join("user", JoinType.LEFT);
                    ps.add(cb.equal(userJoin.get("profess"), UserProfess.sys.value));
                } else if ("化妆".equals(category) || "化妆师".equals(category)) {
                    Join<Object, Object> userJoin = root.join("user", JoinType.LEFT);
                    ps.add(cb.equal(userJoin.get("profess"), UserProfess.zzs.value));
                } else if ("修图".equals(category) || "修图师".equals(category)) {
                    Join<Object, Object> userJoin = root.join("user", JoinType.LEFT);
                    ps.add(cb.equal(userJoin.get("profess"), UserProfess.xts.value));
                }
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };

        Page<Work> p = workRepository.findAll(spec,
            new PageRequest(page - 1, size, new Sort(new Sort.Order(Sort.Direction.DESC, "publishTime"))));

        List<Long> workIds = new ArrayList<>();
        Map<Long, List<Long>> tagIdsMap = new HashMap<>();
        for (Work w : p.getContent()) {
            workIds.add(w.getId());
        }
        // 标签筛选（如有）
        List<Long> filterTagIds = new ArrayList<>();
        if (StringUtils.isNotBlank(tagIds)) {
            for (String s : tagIds.split(",")) {
                try { filterTagIds.add(Long.parseLong(s.trim())); } catch (NumberFormatException ignored) {}
            }
        }
        Map<Long, List<String>> workTags = new HashMap<>();
        if (!workIds.isEmpty()) {
            for (WorkTagRel rel : workTagRelRepository.findAll()) {
                if (workIds.contains(rel.getWorkId())) {
                    if (filterTagIds.isEmpty() || filterTagIds.contains(rel.getTagId())) {
                        WorkTag t = workTagRepository.findOne(rel.getTagId());
                        if (t != null) {
                            workTags.computeIfAbsent(rel.getWorkId(), k -> new ArrayList<>()).add(t.getName());
                        }
                    }
                }
            }
        }

        // 拼装 DTO
        List<WorkListItem> items = new ArrayList<>();
        for (Work w : p.getContent()) {
            // 标签筛选：作品必须含至少一个指定 tag
            if (!filterTagIds.isEmpty()) {
                List<String> ts = workTags.get(w.getId());
                if (ts == null || ts.isEmpty()) continue;
            }
            WorkListItem item = new WorkListItem();
            item.setId(w.getId());
            item.setDescription(w.getDescription());
            item.setMediaType(w.getMediaType());
            item.setIsVideo("2".equals(w.getMediaType()) ? "1" : "0");
            item.setReadCount(w.getReadCount());
            item.setLikeCount(w.getLikeCount());
            item.setFavoriteCount(w.getFavoriteCount());
            item.setPublishTime(w.getPublishTime());
            // 媒体首图
            List<WorkMedia> ms = workMediaRepository.findByWorkIdOrderBySortNoAsc(w.getId());
            if (!ms.isEmpty()) item.setCoverUrl(ms.get(0).getUrl());
            // 发布者
            User u = userRepository.findById(w.getUserid());
            if (u != null) {
                item.setUserId(u.getId());
                item.setNickname(u.getNickname());
                item.setAvatar(u.getAvatarurl());
                item.setGender(u.getGender());
                item.setProfession(u.getProfess());
                item.setCity(u.getCity());
                item.setArea(u.getArea());
            }
            // 标签
            item.setTags(workTags.getOrDefault(w.getId(), Collections.emptyList()));
            items.add(item);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("page", page);
        res.put("size", size);
        res.put("total", p.getTotalElements());
        res.put("items", items);
        return res;
    }

    /**
     * 我的作品
     */
    public Map<String, Object> myWorks(Long userId, Integer page, Integer size, String status) {
        if (userId == null) throw new SysException(ResponseCode.FAIL_AUTH);
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 10 : Math.min(size, 20);
        Page<Work> result;
        if (StringUtils.isNotBlank(status)) {
            result = workRepository.findByUseridAndStatusAndDeletedFlagOrderByIdDesc(userId, status, 0,
                new PageRequest(p - 1, s));
        } else {
            result = workRepository.findByUseridAndDeletedFlagOrderByIdDesc(userId, 0,
                new PageRequest(p - 1, s));
        }
        List<WorkListItem> items = new ArrayList<>();
        for (Work w : result.getContent()) {
            WorkListItem item = new WorkListItem();
            item.setId(w.getId());
            item.setDescription(w.getDescription());
            item.setMediaType(w.getMediaType());
            item.setIsVideo("2".equals(w.getMediaType()) ? "1" : "0");
            item.setReadCount(w.getReadCount());
            item.setLikeCount(w.getLikeCount());
            item.setFavoriteCount(w.getFavoriteCount());
            item.setPublishTime(w.getPublishTime());
            List<WorkMedia> ms = workMediaRepository.findByWorkIdOrderBySortNoAsc(w.getId());
            if (!ms.isEmpty()) item.setCoverUrl(ms.get(0).getUrl());
            items.add(item);
        }
        Map<String, Object> res = new HashMap<>();
        res.put("page", p);
        res.put("size", s);
        res.put("total", result.getTotalElements());
        res.put("items", items);
        return res;
    }

    /**
     * 下架作品（仅作者本人，幂等）
     */
    public void offline(Long workId, Long userId) {
        if (workId == null || userId == null) throw new SysException(ResponseCode.FAIL_AUTH);
        Work work = workRepository.findOne(workId);
        if (work == null) throw new SysException(ResponseCode.FAIL_WORK_NOT_FOUND);
        if (!userId.equals(work.getUserid())) throw new SysException(ResponseCode.FAIL_WORK_FORBIDDEN);
        if (!WorkStatus.xj.value.equals(work.getStatus())) {
            workRepository.updateStatus(workId, WorkStatus.xj.value);
        }
    }

    /**
     * 点赞
     */
    public void like(Long workId, Long userId) {
        if (workId == null || userId == null) throw new SysException(ResponseCode.FAIL_AUTH);
        if (workLikeRepository.existsByWorkIdAndUserId(workId, userId)) {
            throw new SysException(ResponseCode.FAIL_EXIST, "已点赞");
        }
        WorkLike like = new WorkLike();
        like.setWorkId(workId);
        like.setUserId(userId);
        like.setCreatedAt(new Date());
        workLikeRepository.save(like);
        workRepository.incrLikeCount(workId);
    }

    /**
     * 取消点赞
     */
    public void unlike(Long workId, Long userId) {
        if (workId == null || userId == null) throw new SysException(ResponseCode.FAIL_AUTH);
        if (!workLikeRepository.existsByWorkIdAndUserId(workId, userId)) {
            throw new SysException(ResponseCode.FAIL_NOT, "未点赞");
        }
        workLikeRepository.deleteByWorkIdAndUserId(workId, userId);
        workRepository.decrLikeCount(workId);
    }

    /**
     * 收藏
     */
    public void favorite(Long workId, Long userId) {
        if (workId == null || userId == null) throw new SysException(ResponseCode.FAIL_AUTH);
        if (workFavoriteRepository.existsByWorkIdAndUserId(workId, userId)) {
            throw new SysException(ResponseCode.FAIL_EXIST, "已收藏");
        }
        WorkFavorite fav = new WorkFavorite();
        fav.setWorkId(workId);
        fav.setUserId(userId);
        fav.setCreatedAt(new Date());
        workFavoriteRepository.save(fav);
        workRepository.incrFavoriteCount(workId);
    }

    /**
     * 取消收藏
     */
    public void unfavorite(Long workId, Long userId) {
        if (workId == null || userId == null) throw new SysException(ResponseCode.FAIL_AUTH);
        if (!workFavoriteRepository.existsByWorkIdAndUserId(workId, userId)) {
            throw new SysException(ResponseCode.FAIL_NOT, "未收藏");
        }
        workFavoriteRepository.deleteByWorkIdAndUserId(workId, userId);
        workRepository.decrFavoriteCount(workId);
    }

    /**
     * 投诉
     */
    public void complain(Work work, Long userId, String reason, String contact) {
        if (work == null) throw new SysException(ResponseCode.FAIL_WORK_NOT_FOUND);
        if (userId == null) throw new SysException(ResponseCode.FAIL_AUTH);
        if (StringUtils.isBlank(reason) || reason.length() < 10 || reason.length() > 500) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        if (contact != null && contact.length() > 100) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        WorkComplain c = new WorkComplain();
        c.setWorkId(work.getId());
        c.setUserId(userId);
        c.setReason(reason);
        c.setContact(contact);
        c.setStatus("0");
        c.setCreatedAt(new Date());
        workComplainRepository.save(c);
    }

    /**
     * 立即约拍：根据作者身份 → target
     */
    public Map<String, Object> quickApply(WorkQuickApplyQo qo) {
        if (qo == null || StringUtils.isBlank(qo.getWorkId())) throw new SysException(ResponseCode.FAIL_PARA);
        if (StringUtils.isBlank(qo.getViewerUserId())) throw new SysException(ResponseCode.FAIL_AUTH);
        Long workId = Long.parseLong(qo.getWorkId());
        Long viewerId = Long.parseLong(qo.getViewerUserId());

        Work work = workRepository.findByIdAndStatusAndDeletedFlag(workId, WorkStatus.shtg.value, 0);
        if (work == null) throw new SysException(ResponseCode.FAIL_WORK_NOT_FOUND);
        if (viewerId.equals(work.getUserid())) {
            throw new SysException(ResponseCode.FAIL_VAL, "不能给自己约拍");
        }
        User author = userRepository.findById(work.getUserid());
        if (author == null) throw new SysException(ResponseCode.FAIL_NOT);

        String target = mapProfessionToTarget(author.getProfess());

        Map<String, Object> res = new HashMap<>();
        res.put("workId", workId);
        res.put("authorId", author.getId());
        res.put("authorNickname", author.getNickname());
        res.put("target", target);
        res.put("targetLabel", YpatTarget.getNameByCode(target));
        res.put("profession", author.getProfess());
        return res;
    }

    /**
     * 身份 → 约拍 target 映射
     * 0摄影师→0约摄影师
     * 1模特→1约模特
     * 2妆造→4约化妆师
     * 3修图→5约修图师
     * 4个人→1约模特（兼容）
     * 5演员→1约模特
     * 6商家→3约商家
     * 7其他→1约模特（兜底）
     * 8素人模特→1约模特
     */
    private String mapProfessionToTarget(String profess) {
        if (profess == null) return YpatTarget.wysf.value;
        switch (profess) {
            case "0": return YpatTarget.xwgm.value;
            case "1": case "4": case "5": case "7": case "8": return YpatTarget.wysf.value;
            case "2": return YpatTarget.hzsj.value;
            case "3": return YpatTarget.xtsj.value;
            case "6": return YpatTarget.sjfw.value;
            default: return YpatTarget.wysf.value;
        }
    }

    private boolean YesNoValid(String s) {
        return "0".equals(s) || "1".equals(s);
    }

    private boolean containsContact(String text) {
        if (text == null || text.isEmpty()) return false;
        Matcher m1 = MOBILE.matcher(text);
        if (m1.find()) return true;
        if (WX.matcher(text).find()) return true;
        if (QQ.matcher(text).find()) return true;
        return false;
    }

    // ─────────────────────────────────────────────────────────────
    // PR-09: idempotent like / favorite.
    //
    // The existing like(), unlike(), favorite(), unfavorite() above
    // use a "check then act" pattern (existsByWorkIdAndUserId →
    // save/delete → increment/decrement). Two concurrent requests
    // can both see "not yet liked" and both INSERT; one wins, the
    // loser hits DataIntegrityViolation and the user sees a 500.
    //
    // The methods below do not pre-check. They rely on the
    // UNIQUE KEY (work_id, user_id) on t_work_like and
    // t_work_favorite (see tools/ddl/V3__favorite_like_unique_keys.sql)
    // and treat DataIntegrityViolationException as "already in that
    // state" — i.e. success. This is the database-backed
    // idempotency guarantee V1.1 §3.2 calls for.
    //
    // Why we keep the old methods:
    //   The current controllers call like() / favorite() and expect
    //   a SysException("already liked") for the duplicate case. The
    //   controllers and the legacy error contract stay untouched in
    //   PR-09. PR-09 just adds the safe methods. PR-13 (WorkService
    //   split) + PR-15 (user migration) are when we actually swap
    //   the controllers over to these new names and update the
    //   error contract.
    //
    // Counters: the existing workRepository.incrLikeCount /
    // decrLikeCount / incrFavoriteCount / decrFavoriteCount are
    // SQL 'SET count = count ± 1'. They do NOT use GREATEST(., 0).
    // That means a stray cancel of a never-liked work would push the
    // counter negative. We accept this for PR-09 and track the fix
    // in PR-13 / PR-15. Today's volume is small enough that the
    // reconciliation script in docs/architecture/schema-migration.md
    // can keep things tidy.
    // ─────────────────────────────────────────────────────────────

    /**
     * Idempotent like. Caller can invoke any number of times; only
     * the first call increments like_count.
     */
    public void likeIdempotent(Long workId, Long userId) {
        if (workId == null || userId == null) throw new SysException(ResponseCode.FAIL_AUTH);
        WorkLike like = new WorkLike();
        like.setWorkId(workId);
        like.setUserId(userId);
        like.setCreatedAt(new Date());
        try {
            workLikeRepository.save(like);
        } catch (DataIntegrityViolationException dup) {
            // UNIQUE (work_id, user_id) on t_work_like — already liked, idempotent OK.
            return;
        }
        workRepository.incrLikeCount(workId);
    }

    /**
     * Idempotent unlike. Cancellation is a no-op when the user
     * never liked the work; no error thrown.
     */
    public void unlikeIdempotent(Long workId, Long userId) {
        if (workId == null || userId == null) throw new SysException(ResponseCode.FAIL_AUTH);
        int deleted = workLikeRepository.deleteByWorkIdAndUserId(workId, userId);
        if (deleted == 0) return;     // never liked — nothing to do
        workRepository.decrLikeCount(workId);
    }

    /**
     * Idempotent favorite. Mirrors {@link #likeIdempotent(Long, Long)}.
     */
    public void favoriteIdempotent(Long workId, Long userId) {
        if (workId == null || userId == null) throw new SysException(ResponseCode.FAIL_AUTH);
        WorkFavorite fav = new WorkFavorite();
        fav.setWorkId(workId);
        fav.setUserId(userId);
        fav.setCreatedAt(new Date());
        try {
            workFavoriteRepository.save(fav);
        } catch (DataIntegrityViolationException dup) {
            return;
        }
        workRepository.incrFavoriteCount(workId);
    }

    /**
     * Idempotent unfavorite. Mirrors {@link #unlikeIdempotent(Long, Long)}.
     */
    public void unfavoriteIdempotent(Long workId, Long userId) {
        if (workId == null || userId == null) throw new SysException(ResponseCode.FAIL_AUTH);
        int deleted = workFavoriteRepository.deleteByWorkIdAndUserId(workId, userId);
        if (deleted == 0) return;
        workRepository.decrFavoriteCount(workId);
    }
}
