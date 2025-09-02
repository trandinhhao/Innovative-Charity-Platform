package dev.lhs.charity_backend.repository;

import dev.lhs.charity_backend.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    Skill findSkillById(Long id);
}
