package com.siryus.swisscon.api.contract;

import com.amazonaws.util.StringUtils;
import com.naturalprogrammer.spring.lemon.commonsweb.util.LecwUtils;
import com.siryus.swisscon.api.auth.user.AuthorDTO;
import com.siryus.swisscon.api.auth.user.UserService;
import com.siryus.swisscon.api.contract.dto.ContractCommentDTO;
import com.siryus.swisscon.api.contract.dto.ContractCommentsDTO;
import com.siryus.swisscon.api.contract.dto.CreateContractCommentRequest;
import com.siryus.swisscon.api.contract.repos.ContractCommentEntity;
import com.siryus.swisscon.api.contract.repos.ContractCommentRepository;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.mediawidget.MediaConstants;
import com.siryus.swisscon.api.mediawidget.MediaWidgetException;
import com.siryus.swisscon.api.mediawidget.MediaWidgetFileDTO;
import com.siryus.swisscon.api.mediawidget.MediaWidgetQueryScope;
import com.siryus.swisscon.api.mediawidget.MediaWidgetService;
import com.siryus.swisscon.api.util.security.SecurityHelper;
import com.siryus.swisscon.api.util.validator.Reference;
import com.siryus.swisscon.soa.EventsEmitter;
import com.siryus.swisscon.soa.notification.contract.NotificationEvent;
import com.siryus.swisscon.soa.notification.contract.NotificationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Validated
class ContractCommentService {
    private final ContractReader reader;
    private final ContractCommentRepository contractCommentRepository;
    private final SecurityHelper securityHelper;
    private final MediaWidgetService mediaWidgetService;
    private final UserService userService;
    private final EventsEmitter eventsEmitter;

    public ContractCommentService(
            ContractReader reader,
            ContractCommentRepository contractCommentRepository,
            SecurityHelper securityHelper,
            MediaWidgetService mediaWidgetService,
            UserService userService,
            EventsEmitter eventsEmitter) {
        this.reader = reader;
        this.contractCommentRepository = contractCommentRepository;
        this.securityHelper = securityHelper;
        this.mediaWidgetService = mediaWidgetService;
        this.userService = userService;
        this.eventsEmitter = eventsEmitter;
    }

    @Transactional
    public ContractCommentDTO addComment(@Valid CreateContractCommentRequest comment, Integer userId) {
        validateCreateCommentRequest(comment.getFileId(), comment.getText(), comment.getContractId(), userId);

        var contractEntity = reader.getValidContract(comment.getContractId());

        var commentEntity = materializeComment(comment.getFileId(), contractEntity.getPrimaryContractId(), comment.getContractId(), comment.getText());
        
        var attachmentDto = materializeAttachment(comment.getFileId(), comment.getContractId());

        final ContractCommentDTO commentDTO = ContractCommentDTO.from(commentEntity, userService.getAuthor(commentEntity.getCreatedBy()), attachmentDto);

        eventsEmitter.emitNotification(NotificationEvent.builder()
                .projectId(contractEntity.getProjectId())
                .notificationType(NotificationType.CONTRACT_COMMENTED)
                .referenceId(commentEntity.getId())
                .subjectId(commentDTO.getId())
                .senderId(securityHelper.currentUserId())
                .build());
        return commentDTO;
    }    
    
    List<ContractCommentDTO> listComments(@Reference(ReferenceType.CONTRACT) Integer contractId) {
        validateListCommentsRequest(contractId, Integer.parseInt(LecwUtils.currentUser().getId()));
        
        return dtosFromEntities(contractId, contractCommentRepository.findByContractId(contractId));
    }

    List<ContractCommentDTO> listCommentsForPrimaryContract(@Reference(ReferenceType.CONTRACT) Integer primaryContractId) {
        validateListCommentsRequest(primaryContractId, Integer.parseInt(LecwUtils.currentUser().getId()));

        return dtosFromEntities(primaryContractId, contractCommentRepository.findByPrimaryContractId(primaryContractId));
    }

    ContractCommentsDTO listContractComments(Integer contractId) {
        return ContractCommentsDTO.from(
                reader.getValidContract(contractId),
                listComments(contractId)
        );
    }


    @Transactional
    public void removeComment(@Reference(ReferenceType.CONTRACT_COMMENT) Integer commentId) {
        ContractCommentEntity comment = validateRemoveCommentRequest(commentId);
        
        if (comment.getAttachmentId() != null) {
            MediaWidgetFileDTO file = mediaWidgetService.findByFileId(ReferenceType.CONTRACT, comment.getContractId(), comment.getAttachmentId());
            mediaWidgetService.disableSystemFile(file.getId());
        }
                
        contractCommentRepository.disable(commentId);        
    }

    private List<ContractCommentDTO> dtosFromEntities(
            @Reference(ReferenceType.CONTRACT) Integer contractId,
            List<ContractCommentEntity> comments
    ) {
        Map<Integer, AuthorDTO> authors = getAuthors(comments);
        Map<Integer, MediaWidgetFileDTO> attachments = getAttachments(contractId);

        return comments.stream()
                .map(comment ->
                             ContractCommentDTO.from(
                                     comment,
                                     authors.get(comment.getCreatedBy()),
                                     comment.getAttachmentId() != null ? attachments.get(comment.getAttachmentId()) : null
                             )
                )
                .collect(Collectors.toList());
    }

    private ContractCommentEntity materializeComment(Integer fileId, Integer primaryContractId, Integer contractId, String text) {
        return contractCommentRepository.save(
            ContractCommentEntity.builder()
                .attachmentId(fileId)
                .primaryContractId(primaryContractId)
                .contractId(contractId)
                .text(text)
                .build()
        );
    }

    private MediaWidgetFileDTO materializeAttachment(Integer fileId, Integer contractId) {
        if (fileId == null) {
            return null;
        }        

        MediaWidgetFileDTO commentsSystemFolder = mediaWidgetService.getSystemFolderByName(
                ReferenceType.CONTRACT,
                contractId,
                ContractConstants.COMMENTS_FOLDER
        ).orElseThrow(() -> MediaWidgetException.systemFolderNotFound(ContractConstants.COMMENTS_FOLDER));
        
        return mediaWidgetService.convertTemporaryFileToSystemFile(
            fileId,
            ReferenceType.CONTRACT,
            contractId,
            commentsSystemFolder.getId()
        );
    }
    
    private void validateCreateCommentRequest(Integer fileId, String text, Integer contractId, Integer userId) {                
        if (StringUtils.isNullOrEmpty(text) && (fileId == null)) {
            throw ContractExceptions.cannotAddEmptyCommentOnContract(contractId);
        }       
        
        securityHelper.validateUserIsPartOfContractCompanies(contractId, userId);
        
        if (fileId != null) {
            securityHelper.validateUserOwnsTemporaryFile(userId, fileId);
        }
    }
    
    private void validateListCommentsRequest(Integer contractId, Integer userId) {        
        securityHelper.validateUserIsPartOfContractCompanies(contractId, userId);        
    }
    
    private ContractCommentEntity validateRemoveCommentRequest(Integer commentId) {
        Integer userId = securityHelper.currentUserId();
        
        ContractCommentEntity comment = contractCommentRepository.findById(commentId)
                .orElseThrow(() -> ContractExceptions.commentDoesNotExist(commentId));
        
        securityHelper.validateUserIsPartOfContractCompanies(comment.getContractId(), userId);
        
        return comment;
    }    

    private Map<Integer, AuthorDTO> getAuthors(List<ContractCommentEntity> comments) {
        return comments.stream()
                .map(ContractCommentEntity::getCreatedBy)
                .distinct()
                .map(userService::getAuthor)
                .collect(Collectors.toMap(AuthorDTO::getId, Function.identity()));
    }
    
    private Map<Integer, MediaWidgetFileDTO> getAttachments(Integer contractId) {        
        Optional<MediaWidgetFileDTO> parent = mediaWidgetService.getSystemFolderByName(ReferenceType.CONTRACT, contractId, MediaConstants.COMMENTS_FOLDER);        
        if (parent.isEmpty()) {
            return Collections.emptyMap();
        }
        
        return mediaWidgetService.listChildren(parent.get().getId(), new MediaWidgetQueryScope())
                .stream()
                .collect(Collectors.toMap(MediaWidgetFileDTO::getFileId, Function.identity()));        
    }
}
