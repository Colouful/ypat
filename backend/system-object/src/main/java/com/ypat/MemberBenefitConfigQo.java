package com.ypat;

import java.io.Serializable;
import java.util.List;

public class MemberBenefitConfigQo implements Serializable {
    private String scene;
    private String sceneName;
    private Integer originalPpd;
    private String description;
    private Long version;
    private List<MemberBenefitRuleQo> rules;

    public String getScene() { return scene; }
    public void setScene(String scene) { this.scene = scene; }
    public String getSceneName() { return sceneName; }
    public void setSceneName(String sceneName) { this.sceneName = sceneName; }
    public Integer getOriginalPpd() { return originalPpd; }
    public void setOriginalPpd(Integer originalPpd) { this.originalPpd = originalPpd; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public List<MemberBenefitRuleQo> getRules() { return rules; }
    public void setRules(List<MemberBenefitRuleQo> rules) { this.rules = rules; }
}
