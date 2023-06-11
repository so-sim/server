package com.sosim.server.common.auditing;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

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
        status = Status.DELETED;
        deleteDate = LocalDateTime.now();
    }
}