package dev.lhs.charity_backend.entity.auth;

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
@Table(name = "roles")
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
            joinColumns = @JoinColumn(name = "role_name"),
            inverseJoinColumns = @JoinColumn(name = "permission_name")
    )
    private Set<Permission> permissions = new HashSet<>();

}
