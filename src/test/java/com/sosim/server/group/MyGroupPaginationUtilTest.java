package com.sosim.server.group;

import com.sosim.server.group.dto.MyGroupPageDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

class MyGroupPaginationUtilTest {

    int FIRST_SIZE = 17;
    int OTHER_SIZE = 18;

    @DisplayName("page가 0이면 offset은 0")
    @Test
    void offset_test_page_0() {
        //given
        int page = 0;

        //when
        MyGroupPageDto pageDto = MyGroupPaginationUtil.calculateOffsetAndSize(page);

        //then
        assertThat(pageDto.getOffset()).isEqualTo(0);
    }

    @DisplayName("page가 0이면 limit은 FIRST_SIZE")
    @Test
    void limit_test_page_0() {
        //given
        int page = 0;

        //when
        MyGroupPageDto pageDto = MyGroupPaginationUtil.calculateOffsetAndSize(page);

        //then
        assertThat(pageDto.getLimit()).isEqualTo(FIRST_SIZE);
    }

    @DisplayName("page가 1이면 offset은 17")
    @Test
    void offset_test_page_1() {
        //given
        int page = 1;

        //when
        MyGroupPageDto pageDto = MyGroupPaginationUtil.calculateOffsetAndSize(page);

        //then
        assertThat(pageDto.getOffset()).isEqualTo(17);
    }

    @DisplayName("page가 0이면 offset은 0")
    @Test
    void limit_test_page_1() {
        //given
        int page = 1;

        //when
        MyGroupPageDto pageDto = MyGroupPaginationUtil.calculateOffsetAndSize(page);

        //then
        assertThat(pageDto.getLimit()).isEqualTo(OTHER_SIZE);
    }
}