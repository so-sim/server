package com.sosim.server.group.dto.request;

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
    private String title;

    @NotBlank
    @Size(min = 1, max = 15, message = "닉네임은 최소 1글자, 최대 15글자까지 허용됩니다.")
    private String nickname;

    @NotNull
    private String type;

    @NotNull
    private String coverColor;

    @Builder
    public ModifyGroupRequest(String title, String nickname, String type, String coverColor) {
        this.title = title;
        this.nickname = nickname;
        this.type = type;
        this.coverColor = coverColor;
    }
}
