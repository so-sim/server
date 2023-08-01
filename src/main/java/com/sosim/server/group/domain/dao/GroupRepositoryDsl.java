package com.sosim.server.group.domain.dao;

import com.sosim.server.group.domain.entity.Group;
import org.springframework.data.domain.Slice;

public interface GroupRepositoryDsl {
    public Slice<Group> findMyGroups(long userId, long offset, long limit);
}
