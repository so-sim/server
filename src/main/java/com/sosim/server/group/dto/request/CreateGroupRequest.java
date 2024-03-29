package com.sosim.server.group.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sosim.server.group.domain.entity.Group;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CreateGroupRequest {
    @NotBlank
    @Size(min = 1, max = 15, message = "모임 이름은 최소 1글자, 최대 15글자까지 허용됩니다.")
    private String title;

    @NotBlank
    @Size(min = 1, max = 15, message = "닉네임은 최소 1글자, 최대 15글자까지 허용됩니다.")
    private String nickname;

    @NotNull
    @JsonProperty("type")
    private String groupType;

    @NotNull
    private String coverColor;

    public Group toEntity() {
        return Group.builder()
                .title(title)
                .groupType(groupType)
                .coverColor(coverColor)
                .build();
    }
}
