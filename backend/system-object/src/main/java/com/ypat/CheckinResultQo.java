package com.ypat;

import java.io.Serializable;

public class CheckinResultQo implements Serializable {
    private Boolean checkedIn;
    private Integer rewardPpd;
    private Integer currentPpd;
    private Long recordId;
    private String message;

    public Boolean getCheckedIn() { return checkedIn; }
    public void setCheckedIn(Boolean checkedIn) { this.checkedIn = checkedIn; }
    public Integer getRewardPpd() { return rewardPpd; }
    public void setRewardPpd(Integer rewardPpd) { this.rewardPpd = rewardPpd; }
    public Integer getCurrentPpd() { return currentPpd; }
    public void setCurrentPpd(Integer currentPpd) { this.currentPpd = currentPpd; }
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
