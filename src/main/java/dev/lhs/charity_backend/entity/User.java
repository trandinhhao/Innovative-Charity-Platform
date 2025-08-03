package dev.lhs.charity_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "phone_number", unique = true, nullable = false)
    private String phoneNumber;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "avatar_url")
    private String avatar_url;

    @Column(name = "role", nullable = false)
    private String role;
    //----------------------
    // user - skill_auc
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SkillAuction> skillAuctions = new ArrayList<>();

    // user - n skill
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Skill> skills = new ArrayList<>();

    //----------------------
    // user - n user_challenge
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserChallenge> userChallenges = new ArrayList<>();

    // user - challenge
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Challenge> challenges = new ArrayList<>();
}
