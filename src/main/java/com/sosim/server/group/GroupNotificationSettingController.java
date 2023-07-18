package com.sosim.server.group;

import com.sosim.server.common.resolver.AuthUserId;
import com.sosim.server.common.response.Response;
import com.sosim.server.group.dto.request.NotificationSettingRequest;
import com.sosim.server.group.dto.response.NotificationSettingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.*;

import static com.sosim.server.common.response.ResponseCode.GET_GROUP_NOTIFICATION_SETTING;
import static com.sosim.server.common.response.ResponseCode.SET_GROUP_NOTIFICATION_SETTING;

@RequiredArgsConstructor
@RequestMapping("/api")
@Controller
public class GroupNotificationSettingController {

    private final GroupNotificationSettingService groupSettingService;

    @GetMapping("/group/{groupId}/notification-info")
    public ResponseEntity<?> getNotificationSetting(@AuthUserId long userId, @PathVariable long groupId) {
        NotificationSettingResponse response = groupSettingService.getNotificationSetting(userId, groupId);
        return new ResponseEntity<>(Response.create(GET_GROUP_NOTIFICATION_SETTING, response), GET_GROUP_NOTIFICATION_SETTING.getHttpStatus());
    }

    @PutMapping("/group/{groupId}/notification-info")
    public ResponseEntity<?> setNotificationSetting(@AuthUserId long userId, @PathVariable long groupId,
                                                    @RequestBody NotificationSettingRequest settingRequest) throws BindException {
        settingRequest.validate();
        groupSettingService.setNotificationSetting(userId, groupId, settingRequest);
        return new ResponseEntity<>(Response.create(SET_GROUP_NOTIFICATION_SETTING, null), SET_GROUP_NOTIFICATION_SETTING.getHttpStatus());
    }
}
