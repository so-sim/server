package com.sosim.server.group.domain.repository;

import com.sosim.server.group.domain.entity.Group;
import com.sosim.server.group.dto.response.MyGroupDto;
import org.springframework.data.domain.Slice;

public interface GroupRepositoryDsl {
    public Slice<MyGroupDto> findMyGroups(long userId, long offset, long limit);
}
