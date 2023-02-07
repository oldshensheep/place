package com.oldshensheep.place.entity;

import jakarta.persistence.*;
import lombok.*;

import java.nio.ByteBuffer;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "operation", indexes = {
        @Index(name = "idx_operation_created_at", columnList = "created_at")
})
public class Operation extends Base {

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "offset")
    private Integer offset;

    @Column(name = "color")
    private Integer color;

    @Column(name = "ip")
    private String ip;

    public byte[] getColorBytes() {
        var put = ByteBuffer.allocate(4).putInt(color);
        return put.array();
    }

}
