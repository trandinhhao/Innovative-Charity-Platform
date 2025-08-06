package dev.lhs.charity_backend.repository;

import dev.lhs.charity_backend.entity.role.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, String> {
}
