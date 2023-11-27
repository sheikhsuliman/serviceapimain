package com.siryus.swisscon.api.auth.permission;

import com.siryus.swisscon.api.auth.role.Role;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "role_permission")
public class RolePermission {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name="role_id", referencedColumnName = "id", nullable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(name="permission_id", referencedColumnName = "id", nullable = false)
    private Permission permission;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RolePermission that = (RolePermission) o;
        return Objects.equals(role.getId(), that.role.getId()) &&
                Objects.equals(permission.getId(), that.permission.getId());
    }
}
