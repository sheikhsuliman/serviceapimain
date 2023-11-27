package com.siryus.swisscon.api.auth.role;

import com.siryus.swisscon.api.auth.AuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BasicRoleService implements RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    BasicRoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Role findCompanyMemberDefaultRole() {
        return roleRepository.findCompanyMemberDefaultRole()
                .orElseThrow(AuthException::companyMemberDefaultRoleNotFound);
    }

    @Override
    public Role findCompanyOwnerDefaultRole() {
        return roleRepository.findCompanyOwnerDefaultRole()
                .orElseThrow(AuthException::companyOwnerDefaultRoleNotFound);
    }

    @Override
    public Role findById(Integer roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(()-> AuthException.roleDoesNotExist(roleId));
    }

}
