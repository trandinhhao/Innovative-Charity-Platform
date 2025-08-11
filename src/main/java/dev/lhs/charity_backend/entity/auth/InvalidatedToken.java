package dev.lhs.charity_backend.entity.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invalidated_tokens")
public class InvalidatedToken {

    @Id
    private String id;

    @Column(name = "expiry_time")
    private Date expiryTime;

}
