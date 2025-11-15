package com.project.airBnbApp.entity;

import com.project.airBnbApp.entity.enums.Gender;
import com.project.airBnbApp.entity.enums.Role;
import jakarta.persistence.*;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter

@Table(name="app_user")
public class User  implements UserDetails {

    @Id
    @GeneratedValue(strategy= IDENTITY)
    private Long id;

    @Column(unique=true)
    private String email;

    @Column(nullable=false)
    private String password;//encoded

    private String name;

    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @ElementCollection(fetch=FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(getId(), user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role->new SimpleGrantedAuthority("ROLE_"+role.name()))
                .collect(Collectors.toSet());

    }

    @Override
    public String getUsername() {
        return email;
    }
}
