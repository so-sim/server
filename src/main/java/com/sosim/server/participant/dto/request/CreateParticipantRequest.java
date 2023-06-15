package com.sosim.server.participant.dto.request;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@EqualsAndHashCode
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateParticipantRequest {
    @NotBlank
    @Size(min = 1, max = 15, message = "닉네임은 최소 1글자, 최대 15글자까지 허용됩니다.")
    private String nickname;
}
