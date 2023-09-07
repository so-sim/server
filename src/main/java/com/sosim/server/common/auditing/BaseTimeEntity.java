package com.sosim.server.common.auditing;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

import static com.sosim.server.common.auditing.Status.*;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseTimeEntity {
    @Column(name = "CREATE_DATE", updatable = false)
    @CreatedDate
    private LocalDateTime createDate;

    @Column(name = "UPDATE_DATE")
    @LastModifiedDate
    private LocalDateTime updateDate;

    @Column(name = "DELETE_DATE")
    private LocalDateTime deleteDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    protected Status status;

    public void delete() {
        status = DELETED;
        deleteDate = LocalDateTime.now();
    }

    public boolean isActive() {
        return ACTIVE.equals(status);
    }

    public void reActive() {
        status = ACTIVE;
        deleteDate = null;
    }
}
