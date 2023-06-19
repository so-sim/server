package com.sosim.server.group;

import org.springframework.data.domain.Slice;

public interface GroupRepositoryDsl {
    public Slice<Group> findMyGroups(long userId, int offset, int limit);
}
