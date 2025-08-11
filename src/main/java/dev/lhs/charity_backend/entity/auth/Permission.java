package dev.lhs.charity_backend.entity.auth;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "permissions")
public class Permission {

    @Id
    private String name;

    @Column(name = "description")
    private String description;

    // n per - n role
    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles = new HashSet<>();

}
