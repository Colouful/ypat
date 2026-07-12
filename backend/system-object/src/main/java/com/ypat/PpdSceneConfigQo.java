package com.ypat;

import java.io.Serializable;

public class PpdSceneConfigQo implements Serializable {
    private Long id;
    private String scene;
    private String sceneName;
    private Integer originalPpd;
    private String description;
    private Long version;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
}
