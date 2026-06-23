package com.ypat.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "t_user_ypat")
@NamedEntityGraph(
        name="UserYpat.all",
        attributeNodes = {
                @NamedAttributeNode(value = "user",  subgraph = ""),
                @NamedAttributeNode(value = "ypatInfo", subgraph = "subUser")
        },
        subgraphs = {
                @NamedSubgraph(name="subUser",attributeNodes = {
                        @NamedAttributeNode(value = "user")
                })
        })
public class UserYpat implements java.io.Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "userid")
    @JsonIgnore
    private User user;
    @ManyToOne
    @JoinColumn(name = "ypatid")
    @JsonIgnore
    private YpatInfo ypatInfo;

    public UserYpat() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public YpatInfo getYpatInfo() {
        return ypatInfo;
    }

    public void setYpatInfo(YpatInfo ypatInfo) {
        this.ypatInfo = ypatInfo;
    }
}
