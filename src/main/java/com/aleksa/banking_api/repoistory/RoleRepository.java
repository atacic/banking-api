package com.aleksa.banking_api.repoistory;

import com.aleksa.banking_api.model.Role;
import com.aleksa.banking_api.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(RoleName roleName);
}
