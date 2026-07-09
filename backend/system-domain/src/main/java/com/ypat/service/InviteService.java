package com.ypat.service;

import com.ypat.InviteConfigQo;
import com.ypat.InviteRelationQo;
import com.ypat.InviteSummaryQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.UserQo;
import com.ypat.entity.InviteConfig;
import com.ypat.entity.InviteRelation;
import com.ypat.entity.User;
import com.ypat.repository.InviteConfigRepository;
import com.ypat.repository.InviteRelationRepository;
import com.ypat.repository.UserRepository;
import com.ypat.util.Constant;
import com.ypat.util.CopyUtil;
import com.ypat.util.InviteCodeCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class InviteService {

    private static final Logger logger = LoggerFactory.getLogger(InviteService.class);
    public static final String INVITE_ENABLED = "1";
    public static final String INVITE_DISABLED = "0";
    private static final String REWARD_UNIT = "拍拍豆";
    private static final String DEFAULT_RULE_TEXT = "好友通过你的邀请码注册后，自动到账 3 拍拍豆。";
    private static final String DEFAULT_SHARE_TITLE = "好友邀请你加入爱去拍，找摄影师、找模特更方便";
    private static final String DEFAULT_LANDING_TITLE = "我正在使用爱去拍，找摄影师、找模特特别方便，推荐你也来体验。";

    @Autowired
    private InviteRelationRepository inviteRelationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private InviteConfigRepository inviteConfigRepository;

    public InviteConfigQo getConfig() {
        InviteConfig config = loadConfigEntity();
        InviteConfigQo qo = new InviteConfigQo();
        qo.setId(config.getId());
        qo.setEnabled(config.getEnabled());
        qo.setRewardPpd(config.getRewardPpd());
        qo.setRewardUnit(REWARD_UNIT);
        qo.setRuleText(config.getRuleText());
        qo.setShareTitle(config.getShareTitle());
        qo.setLandingTitle(config.getLandingTitle());
        qo.setCreatedAt(config.getCreatedAt());
        qo.setUpdatedAt(config.getUpdatedAt());
        return qo;
    }

    public InviteConfigQo saveConfig(InviteConfigQo qo) {
        validateConfig(qo);
        Date now = new Date();
        InviteConfig config = loadConfigEntity();
        if (config.getCreatedAt() == null) config.setCreatedAt(now);
        config.setUpdatedAt(now);
        config.setEnabled(qo.getEnabled());
        config.setRewardPpd(qo.getRewardPpd());
        config.setRuleText(qo.getRuleText().trim());
        config.setShareTitle(qo.getShareTitle().trim());
        config.setLandingTitle(qo.getLandingTitle().trim());
        InviteConfig saved = inviteConfigRepository.save(config);
        return toConfigQo(saved);
    }

    private InviteConfig loadConfigEntity() {
        List<InviteConfig> configs = inviteConfigRepository.findAll();
        if (!configs.isEmpty()) return fillDefaultConfig(configs.get(0));
        return fillDefaultConfig(new InviteConfig());
    }

    private InviteConfig fillDefaultConfig(InviteConfig config) {
        if (StringUtils.isEmpty(config.getEnabled())) config.setEnabled(INVITE_ENABLED);
        if (config.getRewardPpd() == null) config.setRewardPpd(Constant.INVITE_NEED_PPD);
        if (StringUtils.isEmpty(config.getRuleText())) config.setRuleText(DEFAULT_RULE_TEXT);
        if (StringUtils.isEmpty(config.getShareTitle())) config.setShareTitle(DEFAULT_SHARE_TITLE);
        if (StringUtils.isEmpty(config.getLandingTitle())) config.setLandingTitle(DEFAULT_LANDING_TITLE);
        return config;
    }

    private InviteConfigQo toConfigQo(InviteConfig config) {
        fillDefaultConfig(config);
        InviteConfigQo qo = new InviteConfigQo();
        qo.setId(config.getId());
        qo.setEnabled(config.getEnabled());
        qo.setRewardPpd(config.getRewardPpd());
        qo.setRewardUnit(REWARD_UNIT);
        qo.setRuleText(config.getRuleText());
        qo.setShareTitle(config.getShareTitle());
        qo.setLandingTitle(config.getLandingTitle());
        qo.setCreatedAt(config.getCreatedAt());
        qo.setUpdatedAt(config.getUpdatedAt());
        return qo;
    }

    private void validateConfig(InviteConfigQo qo) {
        if (qo == null) throw new SysException(ResponseCode.FAIL_PARA);
        if (!INVITE_ENABLED.equals(qo.getEnabled()) && !INVITE_DISABLED.equals(qo.getEnabled())) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        if (qo.getRewardPpd() == null || qo.getRewardPpd() < 0 || qo.getRewardPpd() > 999) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        if (StringUtils.isEmpty(qo.getRuleText()) || qo.getRuleText().trim().length() > 300) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        if (StringUtils.isEmpty(qo.getShareTitle()) || qo.getShareTitle().trim().length() > 120) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        if (StringUtils.isEmpty(qo.getLandingTitle()) || qo.getLandingTitle().trim().length() > 160) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
    }

    /**
     * 注册时由 {@link UserService#save} 调用，把"被邀请人 → 邀请人"关系写入幂等表。
     * 不在此处发奖（奖励仍由 UserService 用 Record + ppd 累加保持兼容）。
     *
     * - inviterUserid 与 inviteeUserid 不能相等（拒绝自我邀请）
     * - 同 invitee 已存在关系直接返回，不抛错（注册主流程不被阻塞）
     * - 唯一约束兜底：并发场景下两个事务同时写入也只会保留第一条
     */
    public void bindRelation(Long inviterUserid, Long inviteeUserid, String inviteCode, String source) {
        if (inviterUserid == null || inviteeUserid == null) return;
        if (inviterUserid.equals(inviteeUserid)) {
            logger.warn("invite.bind.self_reject inviter={} invitee={}", inviterUserid, inviteeUserid);
            return;
        }
        InviteRelation existing = inviteRelationRepository.findByInviteeUserid(inviteeUserid);
        if (existing != null) return;

        InviteRelation relation = new InviteRelation();
        relation.setInviterUserid(inviterUserid);
        relation.setInviteeUserid(inviteeUserid);
        relation.setInviteCode(inviteCode);
        relation.setSource(StringUtils.isEmpty(source) ? "register" : source);
        relation.setRewardPpd(Constant.INVITE_NEED_PPD);
        relation.setCredate(new Date());
        try {
            inviteRelationRepository.save(relation);
        } catch (DataIntegrityViolationException ex) {
            // 唯一约束兜底：高并发下另一事务先写入了，本次跳过
            logger.warn("invite.bind.race_skip inviter={} invitee={}", inviterUserid, inviteeUserid);
        }
    }

    /**
     * 解析邀请来源（优先 inviteCode → 退化到 recmobile）→ 邀请人 user.id。
     */
    public Long resolveInviterId(String inviteCode, String recmobile, UserService userService) {
        if (!StringUtils.isEmpty(inviteCode)) {
            Long id = InviteCodeCodec.decode(inviteCode);
            if (id != null) {
                User user = userRepository.findOne(id);
                if (user != null) return user.getId();
            }
        }
        if (!StringUtils.isEmpty(recmobile)) {
            UserQo recUser = userService.findByMobile(recmobile);
            if (recUser != null) return recUser.getId();
        }
        return null;
    }

    public InviteSummaryQo getSummary(Long userId) {
        InviteSummaryQo summary = new InviteSummaryQo();
        InviteConfigQo config = getConfig();
        int rewardPpd = config.getRewardPpd() == null ? Constant.INVITE_NEED_PPD : config.getRewardPpd();
        summary.setInviteCode(InviteCodeCodec.encode(userId));
        summary.setRewardPpd(rewardPpd);
        summary.setEnabled(config.getEnabled());
        summary.setRuleText(config.getRuleText());
        summary.setShareTitle(config.getShareTitle());
        summary.setLandingTitle(config.getLandingTitle());
        if (userId == null) {
            summary.setTotalInvited(0L);
            summary.setTotalReward(0);
            return summary;
        }
        long count = inviteRelationRepository.countByInviterUserid(userId);
        summary.setTotalInvited(count);
        summary.setTotalReward((int) count * rewardPpd);
        return summary;
    }

    public Map<String, Object> findPage(InviteRelationQo qo) {
        if (qo == null || qo.getInviterUserid() == null) {
            return toPage(new PageImpl<>(Collections.emptyList()), Collections.emptyList());
        }
        Sort sort = new Sort(Sort.Direction.DESC, "credate");
        Pageable pageable = new PageRequest(
                qo.getPage() == null ? 0 : qo.getPage(),
                qo.getSize() == null ? 10 : qo.getSize(),
                sort
        );

        Specification<InviteRelation> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("inviterUserid"), qo.getInviterUserid()));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<InviteRelation> page = inviteRelationRepository.findAll(spec, pageable);

        List<InviteRelationQo> content = new ArrayList<>(page.getNumberOfElements());
        for (InviteRelation r : page.getContent()) {
            InviteRelationQo dto = CopyUtil.copy(r, InviteRelationQo.class);
            User invitee = userRepository.findOne(r.getInviteeUserid());
            if (invitee != null) {
                dto.setInviteeNickname(invitee.getNickname());
                String mobile = invitee.getMobile();
                if (mobile != null && mobile.length() >= 11) {
                    dto.setInviteeMobileMask(mobile.substring(0, 3) + "****" + mobile.substring(7));
                }
                // 头像在 UserImg type=head；此处不再二次查询以避免 N+1，由前端按需查 /user/get
            }
            content.add(dto);
        }
        return toPage(page, content);
    }

    private Map<String, Object> toPage(Page<?> page, List<?> content) {
        Map<String, Object> body = new HashMap<>();
        body.put("content", content);
        body.put("totalElements", page.getTotalElements());
        body.put("totalPages", page.getTotalPages());
        body.put("number", page.getNumber());
        body.put("size", page.getSize());
        return body;
    }

    /** 仅用于 register 后异步补偿（当前未启用，保留接口供 UserService 调用方便单测）。 */
    public boolean isBound(Long inviteeUserid) {
        return inviteeUserid != null
                && inviteRelationRepository.findByInviteeUserid(inviteeUserid) != null;
    }
}
