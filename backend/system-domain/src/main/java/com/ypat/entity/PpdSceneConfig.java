package com.ypat.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "t_ppd_scene_config",
        uniqueConstraints = @UniqueConstraint(name = "uk_ppd_scene", columnNames = "scene"))
public class PpdSceneConfig implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 32)
    private String scene;
    @Column(name = "original_ppd", nullable = false)
    private Integer originalPpd;
    @Column(length = 256)
    private String description;
    @Version
    private Long version;
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getScene() { return scene; }
    public void setScene(String scene) { this.scene = scene; }
    public Integer getOriginalPpd() { return originalPpd; }
    public void setOriginalPpd(Integer originalPpd) { this.originalPpd = originalPpd; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
