package com.sosim.server.group.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class ModifyGroupRequest {
    @NotBlank
    @Size(min = 1, max = 15, message = "모임 이름은 최소 1글자, 최대 15글자까지 허용됩니다.")
    @JsonProperty("title")
    private String title;

    @JsonProperty("nickname")
    private String nickname;

    @NotNull
    @JsonProperty("type")
    private String groupType;

    @NotNull
    @JsonProperty("coverColor")
    private String coverColorType;

    @Builder
    public ModifyGroupRequest(String title, String nickname, String groupType, String coverColorType) {
        this.title = title;
        this.nickname = nickname;
        this.groupType = groupType;
        this.coverColorType = coverColorType;
    }
}
