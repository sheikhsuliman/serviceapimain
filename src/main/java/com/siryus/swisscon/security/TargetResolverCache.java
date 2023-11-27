package com.siryus.swisscon.security;

import com.siryus.swisscon.api.company.company.Company;
import com.siryus.swisscon.api.contract.repos.ContractCommentEntity;
import com.siryus.swisscon.api.contract.repos.ContractCommentRepository;
import com.siryus.swisscon.api.contract.repos.ContractEntity;
import com.siryus.swisscon.api.contract.repos.ContractRepository;
import com.siryus.swisscon.api.contract.repos.ContractTaskEntity;
import com.siryus.swisscon.api.contract.repos.ContractTaskRepository;
import com.siryus.swisscon.api.general.reference.ReferenceService;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.location.location.Location;
import com.siryus.swisscon.api.location.location.LocationRepository;
import com.siryus.swisscon.api.project.project.Project;
import com.siryus.swisscon.api.tasks.entity.CommentEntity;
import com.siryus.swisscon.api.tasks.entity.MainTaskEntity;
import com.siryus.swisscon.api.tasks.entity.SubTaskCheckListEntity;
import com.siryus.swisscon.api.tasks.entity.SubTaskEntity;
import com.siryus.swisscon.api.tasks.entity.TaskLinkEntity;
import com.siryus.swisscon.api.tasks.repos.CommentRepository;
import com.siryus.swisscon.api.tasks.repos.MainTaskRepository;
import com.siryus.swisscon.api.tasks.repos.SubTaskCheckListRepository;
import com.siryus.swisscon.api.tasks.repos.SubTaskRepository;
import com.siryus.swisscon.api.tasks.repos.TaskLinkRepository;
import com.siryus.swisscon.api.util.entitytree.EntityTreeNodeDTO;
import com.siryus.swisscon.api.util.entitytree.EntityTreeService;
import com.siryus.swisscon.api.util.entitytree.nestedsets.EntityTreeNode;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service("com.siryus.swisscon.security.SiryusPermissionChecker$ProjectResolverFactory")
public class TargetResolverCache implements SiryusPermissionChecker.TargetResolverCache, SiryusPermissionChecker.TargetResolver {
    private final LocationRepository locationRepository;
    private final MainTaskRepository taskRepository;
    private final SubTaskRepository subTaskRepository;
    private final EntityTreeService treeService;
    private final SubTaskCheckListRepository checkListRepository;
    private final TaskLinkRepository taskLinkRepository;
    private final ContractRepository contractRepository;
    private final ContractTaskRepository contractTaskRepository;
    private final ContractCommentRepository contractCommentRepository;
    private final ReferenceService referenceService;
    private final CommentRepository commentRepository;

    private final Map<Class, SiryusPermissionChecker.TargetResolver> resolverMap = new ConcurrentHashMap<>();

    public TargetResolverCache(
            LocationRepository locationRepository,
            MainTaskRepository taskRepository,
            SubTaskRepository subTaskRepository,
            EntityTreeService treeService,
            SubTaskCheckListRepository checkListRepository,
            TaskLinkRepository taskLinkRepository,
            ContractRepository contractRepository,
            ContractTaskRepository contractTaskRepository,
            ContractCommentRepository contractCommentRepository,
            ReferenceService referenceService,
            CommentRepository commentRepository
    ) {
        this.locationRepository = locationRepository;
        this.taskRepository = taskRepository;
        this.subTaskRepository = subTaskRepository;
        this.treeService = treeService;
        this.checkListRepository = checkListRepository;
        this.taskLinkRepository = taskLinkRepository;
        this.contractRepository = contractRepository;
        this.contractTaskRepository = contractTaskRepository;
        this.contractCommentRepository = contractCommentRepository;
        this.referenceService = referenceService;
        this.commentRepository = commentRepository;
    }

    @Override
    public SiryusPermissionChecker.TargetResolver getResolver(Class targetType) {
        if (EntityTreeNodeDTO.class.isAssignableFrom(targetType)) {
            return this;
        }

        return resolverMap.computeIfAbsent(targetType, k -> createTargetResolver(targetType));
    }

    private SiryusPermissionChecker.TargetResolver createTargetResolver(Class targetType) {
        if (Project.class.isAssignableFrom(targetType)) {
            return new ProjectResolver();
        }
        if (Company.class.isAssignableFrom(targetType)) {
            return new CompanyResolver();
        }
        if (MainTaskEntity.class.isAssignableFrom(targetType)) {
            return new MainTaskResolver(taskRepository);
        }
        if (SubTaskEntity.class.isAssignableFrom(targetType)) {
            return new SubTaskResolver(subTaskRepository);
        }
        if (Location.class.isAssignableFrom(targetType)) {
            return new LocationResolver(locationRepository);
        }
        if (SubTaskCheckListEntity.class.isAssignableFrom(targetType)) {
            return new SubTaskCheckListEntityResolver(subTaskRepository, checkListRepository);
        }
        if (CommentEntity.class.isAssignableFrom(targetType)) {
            return new SubTaskCommentEntityResolver(subTaskRepository, commentRepository);
        }
        if (TaskLinkEntity.class.isAssignableFrom(targetType)) {
            return new TaskLinkResolver(taskLinkRepository);
        }
        if(ContractEntity.class.isAssignableFrom(targetType)) {
            return new ContractResolver(contractRepository);
        }
        if(ContractTaskEntity.class.isAssignableFrom(targetType)) {
            return new ContractTaskResolver(contractTaskRepository);
        }
        if(ContractCommentEntity.class.isAssignableFrom(targetType)) {
            return new ContractCommentResolver(contractCommentRepository, contractRepository);
        }
        
        if (EntityTreeNode.class.isAssignableFrom(targetType)) {
            return new MediaWidgetFileResolver(treeService, referenceService);
        }        
        
        throw SecurityException.unsupportedTargetType(targetType.toString());
    }

    @Override
    public SiryusPermissionChecker.TargetResolver getResolver(String referenceTypeOrClassName) {
        try {
            final Class<?> targetClass;
            if (ReferenceType.isValidReferenceType(referenceTypeOrClassName)) {
                targetClass = ReferenceType.valueOf(referenceTypeOrClassName).getReferencedClass();
            } else {
                targetClass = Class.forName(referenceTypeOrClassName);
            }
            return this.getResolver(targetClass);
        } catch (ClassNotFoundException e) {
            throw SecurityException.unknownTargetType(referenceTypeOrClassName);
        }
    }

    @Override
    public AuthorizationTarget resolveTarget(Object target) {
        if (!EntityTreeNodeDTO.class.isAssignableFrom(target.getClass())) {
            throw SecurityException.unsupportedTargetType(target.getClass().toString());
        }

        final ReferenceType referenceType = ((EntityTreeNodeDTO) target).getOwnerReferenceType();
        SiryusPermissionChecker.TargetResolver delegate = this.getResolver(referenceType.getReferencedClass());
        return delegate.resolveTarget(target);
    }

    @Override
    public AuthorizationTarget resolveTarget(Serializable target) {
        EntityTreeNodeDTO node = this.treeService.getNode(Integer.valueOf(target.toString()));
        return this.resolveTarget(node);
    }
}
