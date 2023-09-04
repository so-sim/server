package com.sosim.server.group.service;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.group.domain.entity.Group;
import com.sosim.server.group.domain.repository.GroupRepository;
import com.sosim.server.group.domain.entity.NotificationSettingInfo;
import com.sosim.server.group.dto.request.NotificationSettingRequest;
import com.sosim.server.group.dto.response.NotificationSettingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.sosim.server.common.response.ResponseCode.NOT_FOUND_GROUP;

@Service
@RequiredArgsConstructor
public class GroupNotificationSettingService {

    private final GroupRepository groupRepository;

    @Transactional(readOnly = true)
    public NotificationSettingResponse getNotificationSetting(long groupId) {
        Group group = findGroupWithNotificationSettingInfo(groupId);
        NotificationSettingInfo settingInfo = group.getNotificationSettingInfo();

        return NotificationSettingResponse.toResponse(settingInfo);
    }

    @Transactional
    public void setNotificationSetting(long userId, long groupId, NotificationSettingRequest settingRequest) {
        NotificationSettingInfo settingInfo = settingRequest.toNotificationSettingInfo();

        Group group = findGroupWithNotificationSettingInfo(groupId);
        group.changeNotificationSettingInfo(userId, settingInfo);
    }

    private Group findGroupWithNotificationSettingInfo(long groupId) {
        return groupRepository.findByIdWithNotificationSettingInfo(groupId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_GROUP));
    }

}
