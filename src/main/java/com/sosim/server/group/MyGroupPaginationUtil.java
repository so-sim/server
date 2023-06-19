package com.sosim.server.group;

import com.sosim.server.group.dto.MyGroupPageDto;

public class MyGroupPaginationUtil {
    private static final int FIRST_SIZE = 17;
    private static final int OTHER_SIZE = 18;

    public static MyGroupPageDto calculateOffsetAndSize(int page) {
        int offset = calculateOffset(page);
        int limit = isFirstPage(page) ? FIRST_SIZE : OTHER_SIZE;

        return new MyGroupPageDto(offset, limit);
    }

    private static boolean isFirstPage(int page) {
        return page == 0;
    }

    private static int calculateOffset(int page) {
        int offset = (page - 1) * OTHER_SIZE + FIRST_SIZE;
        if (offset < 0) {
            offset = 0;
        }
        return offset;
    }
}
