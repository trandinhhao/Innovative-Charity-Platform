package dev.lhs.charity_backend.repository;

import dev.lhs.charity_backend.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Organization findOrganizationById(Long id);
}
