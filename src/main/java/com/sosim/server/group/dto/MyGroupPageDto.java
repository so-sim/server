package com.sosim.server.group.dto;

import lombok.Data;

@Data
public class MyGroupPageDto {
    private int offset;
    private int limit;

    public MyGroupPageDto(int offset, int limit) {
        this.offset = offset;
        this.limit = limit;
    }
}
