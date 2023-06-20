package com.sosim.server.group.dto;

import lombok.Data;

@Data
public class MyGroupPageDto {
    private long offset;
    private long limit;

    public MyGroupPageDto(long offset, long limit) {
        this.offset = offset;
        this.limit = limit;
    }
}
