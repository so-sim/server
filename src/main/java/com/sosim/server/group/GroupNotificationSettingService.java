package com.sosim.server.group;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.group.dto.request.NotificationSettingRequest;
import com.sosim.server.group.dto.response.NotificationSettingResponse;
import com.sosim.server.notification.util.NotificationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.sosim.server.common.response.ResponseCode.NOT_FOUND_GROUP;

@Service
@RequiredArgsConstructor
public class GroupNotificationSettingService {

    private final GroupRepository groupRepository;
    private final NotificationUtil notificationUtil;

    @Transactional(readOnly = true)
    public NotificationSettingResponse getNotificationSetting(long userId, long groupId) {
        Group group = findGroupWithNotificationSettingInfo(groupId);
        NotificationSettingInfo settingInfo = group.getNotificationSettingInfo(userId);

        return NotificationSettingResponse.toResponse(settingInfo);
    }

    @Transactional
    public void setNotificationSetting(long userId, long groupId, NotificationSettingRequest settingRequest) {
        NotificationSettingInfo settingInfo = settingRequest.toSettingInfoVO();

        Group group = findGroupWithNotificationSettingInfo(groupId);
        group.changeNotificationSettingInfo(userId, settingInfo);
        notificationUtil.changeReservedRegularNotifications(group);
    }

    private Group findGroupWithNotificationSettingInfo(long groupId) {
        return groupRepository.findByIdWithNotificationSettingInfo(groupId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_GROUP));
    }

}
