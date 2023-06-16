package com.sosim.server.group;

import com.sosim.server.common.auditing.BaseTimeEntity;
import com.sosim.server.common.auditing.Status;
import com.sosim.server.group.dto.request.CreateGroupRequest;
import com.sosim.server.group.dto.request.ModifyGroupRequest;
import com.sosim.server.participant.Participant;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "GROUPS")
public class Group extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GROUP_ID")
    private Long id;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "ADMIN_ID")
    private Long adminId;

    @Column(name = "ADMIN_NICKNAME")
    private String adminNickname;

    @Column(name = "COVER_COLOR")
    private String coverColor;

    @Column(name = "GROUP_TYPE")
    private String groupType;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    private List<Participant> participantList = new ArrayList<>();

    @Builder
    private Group(String title, Long adminId, String adminNickname, String coverColor, String groupType) {
        this.title = title;
        this.adminId = adminId;
        this.adminNickname = adminNickname;
        this.coverColor = coverColor;
        this.groupType = groupType;
        status = Status.ACTIVE;
    }

    public static Group create(Long adminId, CreateGroupRequest createGroupRequest) {
        return Group.builder()
                .title(createGroupRequest.getTitle())
                .adminId(adminId)
                .adminNickname(createGroupRequest.getNickname())
                .groupType(createGroupRequest.getGroupType())
                .coverColor(createGroupRequest.getCoverColor())
                .build();
    }

    public void modify(ModifyGroupRequest modifyGroupRequest) {
        this.title = modifyGroupRequest.getTitle();
        this.groupType = modifyGroupRequest.getGroupType();
        this.coverColor = modifyGroupRequest.getCoverColorType();
    }

    public void modifyAdmin(Participant participant) {
        adminId = participant.getUser().getId();
        adminNickname = participant.getNickname();
    }
}
