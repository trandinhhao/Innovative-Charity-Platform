package dev.lhs.charity_backend.entity.role;

import dev.lhs.charity_backend.entity.User;
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
@Table(name = "role")
public class Role {

    @Id
    private String name;

    @Column(name = "description")
    private String description;

    // n role - n user
    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();

    // n role - n permission
    @ManyToMany
    @JoinTable(
            name = "role_permission",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

}
