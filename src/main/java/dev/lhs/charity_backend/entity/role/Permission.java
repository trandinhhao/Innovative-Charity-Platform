package dev.lhs.charity_backend.entity.role;

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
@Table(name = "permission")
public class Permission {

    @Id
    private String name;

    @Column(name = "description")
    private String description;

    // n per - n role
    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles = new HashSet<>();

}
