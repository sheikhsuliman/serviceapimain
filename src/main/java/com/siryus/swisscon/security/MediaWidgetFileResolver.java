package com.siryus.swisscon.security;

import com.siryus.swisscon.api.general.reference.ReferenceService;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.location.location.Location;
import com.siryus.swisscon.api.project.project.Project;
import com.siryus.swisscon.api.tasks.entity.MainTaskEntity;
import com.siryus.swisscon.api.util.entitytree.EntityTreeNodeDTO;
import com.siryus.swisscon.api.util.entitytree.EntityTreeService;
import java.io.Serializable;

public class MediaWidgetFileResolver implements SiryusPermissionChecker.TargetResolver {
    private final EntityTreeService treeService;
    private final ReferenceService referenceService;

    public MediaWidgetFileResolver(EntityTreeService treeService, ReferenceService referenceService) {
        this.treeService = treeService;
        this.referenceService = referenceService;
    }

    @Override
    public AuthorizationTarget resolveTarget(Object target) {
        throw new UnsupportedOperationException("Not supported"); 
    }

    @Override
    public AuthorizationTarget resolveTarget(Serializable target) {
        return getAuthorizationTargetFromEntityTreeNodeId(Integer.valueOf(target.toString()));
    }

    private AuthorizationTarget getAuthorizationTargetFromReferenceType(Integer ownerId, ReferenceType ownerReferenceType) {

        switch(ownerReferenceType) {
            case MAIN_TASK:
                MainTaskEntity mainTask = referenceService.getForeignEntityAndValidateType(ownerReferenceType, ownerId);

                return AuthorizationTarget.builder().projectId(mainTask.getProjectId()).build();
            case PROJECT:
                Project project = referenceService.getForeignEntityAndValidateType(ownerReferenceType, ownerId);

                return AuthorizationTarget.builder().projectId(project.getId()).build();
            case LOCATION:
                Location location = referenceService.getForeignEntityAndValidateType(ownerReferenceType, ownerId);

                return AuthorizationTarget.builder().projectId(location.getProject().getId()).build();                
            default:
                throw SecurityException.requiredResolutionFailed(ownerId.toString(), ownerReferenceType.name());
        }
    }

    private AuthorizationTarget getAuthorizationTargetFromEntityTreeNodeId(Integer nodeId) {
        EntityTreeNodeDTO nodeDto = treeService.getNode(nodeId);

        return getAuthorizationTargetFromReferenceType(nodeDto.getOwnerReferenceId(), nodeDto.getOwnerReferenceType());
    }
}
