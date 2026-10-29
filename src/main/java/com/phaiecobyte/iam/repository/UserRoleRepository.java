package com.phaiecobyte.iam.repository;

import com.phaiecobyte.iam.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole,Long> {
    @Query("""
        SELECT DISTINCT p.code
        FROM UserRole ur, RolePermission rp, Permission p
        WHERE ur.user.uuid = :userUuid
          AND rp.role = ur.role
          AND p = rp.permission
          AND ur.role.organization IS NULL
          AND ur.role.isSystem = true
        """)
    List<String> findGlobalPermissionCodesByUserUuid(@Param("userUuid") UUID userUuid);
}
