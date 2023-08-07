package com.sosim.server.notification.domain.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class GroupInfo {

    @Column(name = "GROUP_ID")
    private long groupId;

    @Column(name = "GROUP_TITLE")
    private String groupTitle;

    @Builder
    public GroupInfo(long groupId, String groupTitle) {
        this.groupId = groupId;
        this.groupTitle = groupTitle;
    }
}
