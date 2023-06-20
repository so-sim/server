package com.sosim.server.group;

import org.springframework.data.domain.Slice;

public interface GroupRepositoryDsl {
    public Slice<Group> findMyGroups(long userId, long offset, long limit);
}
