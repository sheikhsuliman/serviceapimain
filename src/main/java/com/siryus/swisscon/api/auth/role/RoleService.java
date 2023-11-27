package com.siryus.swisscon.api.auth.role;

public interface RoleService {

    Role findCompanyMemberDefaultRole();

    Role findCompanyOwnerDefaultRole();

    Role findById(Integer roleId);

}
