package dev.lhs.charity_backend.repository;

import dev.lhs.charity_backend.entity.auth.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, String> {
}
