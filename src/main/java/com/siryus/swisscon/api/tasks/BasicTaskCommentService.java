package com.siryus.swisscon.api.tasks;

import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.commonsweb.util.LecwUtils;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.auth.user.UserRepository;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.file.file.FileRepository;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.mediawidget.MediaWidgetFileDTO;
import com.siryus.swisscon.api.mediawidget.MediaWidgetQueryScope;
import com.siryus.swisscon.api.mediawidget.MediaWidgetService;
import com.siryus.swisscon.api.tasks.dto.CommentDTO;
import com.siryus.swisscon.api.tasks.dto.CreateCommentDTO;
import com.siryus.swisscon.api.tasks.entity.CommentEntity;
import com.siryus.swisscon.api.tasks.entity.MainTaskEntity;
import com.siryus.swisscon.api.tasks.entity.SubTaskEntity;
import com.siryus.swisscon.api.tasks.exceptions.TaskExceptions;
import com.siryus.swisscon.api.tasks.repos.CommentRepository;
import com.siryus.swisscon.api.tasks.repos.MainTaskRepository;
import com.siryus.swisscon.api.tasks.repos.SubTaskRepository;
import com.siryus.swisscon.api.util.security.SecurityHelper;
import com.siryus.swisscon.soa.EventsEmitter;
import com.siryus.swisscon.soa.notification.contract.NotificationEvent;
import com.siryus.swisscon.soa.notification.contract.NotificationType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.siryus.swisscon.api.mediawidget.MediaConstants.COMMENTS_FOLDER;

@Service
public class BasicTaskCommentService implements TaskCommentService {

    private final MainTaskRepository mainTaskRepository;
    private final SubTaskRepository subTaskRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final MediaWidgetService mediaWidgetService;
    private final FileRepository fileRepository;
    private final SecurityHelper securityHelper;
    private final EventsEmitter eventsEmitter;

    public BasicTaskCommentService(
            MainTaskRepository mainTaskRepository,
            SubTaskRepository subTaskRepository,
            CommentRepository commentRepository,
            UserRepository userRepository, MediaWidgetService mediaWidgetService,
            FileRepository fileRepository,
            SecurityHelper securityHelper,
            EventsEmitter eventsEmitter) {
        this.mainTaskRepository = mainTaskRepository;
        this.subTaskRepository = subTaskRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.mediaWidgetService = mediaWidgetService;
        this.fileRepository = fileRepository;
        this.securityHelper = securityHelper;
        this.eventsEmitter = eventsEmitter;
    }

    @Override
    public List<CommentDTO> getMainTaskComments(Integer mainTaskId) {
        // Check that the task exists
        MainTaskEntity task = mainTaskRepository.findById(mainTaskId)
                .orElseThrow(() -> TaskExceptions.mainTaskNotFound(mainTaskId));

        UserDto user = LecwUtils.currentUser();
        Integer userId = Integer.parseInt(user.getId());

        securityHelper.validateUserIsPartOfProject(userId, task.getProjectId());

        SubTaskEntity sue =  subTaskRepository.getDefaultSubTask(task.getId())
                .orElseThrow(() -> TaskExceptions.defaultSubTaskNotFound(mainTaskId));

        return internalGetComments(sue.getId(), mainTaskId);
    }

    @Override
    public List<CommentDTO> getSubTaskComments(Integer subTaskId) {
        UserDto user = LecwUtils.currentUser();
        Integer userId = Integer.parseInt(user.getId());

        SubTaskEntity subTask = subTaskRepository.findById(subTaskId)
                .orElseThrow(() -> TaskExceptions.subTaskNotFound(subTaskId));

        securityHelper.validateUserIsPartOfProject(userId, subTask.getMainTask().getProjectId());

        return internalGetComments(subTaskId, subTask.getMainTask().getId());
    }

    private List<CommentDTO> internalGetComments(Integer subTaskId, Integer mainTaskId) {

        List<CommentEntity> comments = commentRepository.findBySubTaskId(subTaskId);
        if (null == comments || comments.isEmpty()) {
            return Collections.emptyList();
        }

        // Batch retrieve attachments
        HashMap<Integer, MediaWidgetFileDTO> files = new HashMap<>();
        Optional<MediaWidgetFileDTO> parent = mediaWidgetService.getSystemFolderByName(ReferenceType.MAIN_TASK, mainTaskId, COMMENTS_FOLDER);
        if (parent.isPresent()) {
            for (MediaWidgetFileDTO f : mediaWidgetService.listChildren(parent.get().getId(), new MediaWidgetQueryScope())) {
                files.put(f.getFileId(), f);
            }
        }

        HashMap<Integer, User> users = new HashMap<>();
        // Batch retrieve users
        for(User u : userRepository.findAllById(
                comments.stream().map(CommentEntity::getCreatedBy).distinct().collect(Collectors.toList())
        )) {
            users.put(u.getId(), u);
        }

        for(CommentEntity ce : comments) {
            // Check that attachment references are intact
            if (ce.getAttachmentId() != null && !files.containsKey(ce.getAttachmentId())) {
                files.put(ce.getAttachmentId(), MediaWidgetFileDTO.from(
                        fileRepository.findById(ce.getAttachmentId())
                        .orElseThrow(() -> TaskExceptions.attachmentNotFound(ce.getAttachmentId())),
                        users.get(ce.getCreatedBy())
                ));
            }
        }

        // Batch retrieve users
        for(User u : userRepository.findAllById(users.keySet())) {
            users.put(u.getId(), u);
        }


        // Put everything together
        return comments.stream()
                .map(c -> CommentDTO.from(c,
                        users.get(c.getCreatedBy()),
                        c.getAttachmentId() != null ? files.get(c.getAttachmentId()) : null))
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CommentDTO addCommentToMainTask(Integer mainTaskId, CreateCommentDTO comment) {
        // Check that the task exists
        MainTaskEntity task = mainTaskRepository.findById(mainTaskId)
                .orElseThrow(() -> TaskExceptions.mainTaskNotFound(mainTaskId) );

        SubTaskEntity sue = subTaskRepository.getDefaultSubTask(task.getId())
                .orElseThrow(() -> TaskExceptions.defaultSubTaskNotFound(mainTaskId) );

        final CommentDTO commentDTO = addCommentToSubTaskInternal(sue, comment);

        eventsEmitter.emitNotification(NotificationEvent.builder()
                .notificationType(NotificationType.MAIN_TASK_COMMENTED)
                .senderId(securityHelper.currentUserId())
                .projectId(task.getProjectId())
                .referenceId(mainTaskId)
                .subjectId(commentDTO.getId())
                .build());
        return commentDTO;
    }

    @Transactional
    @Override
    public CommentDTO addCommentToSubTask(Integer subTaskId, CreateCommentDTO comment) {
        SubTaskEntity subTask = subTaskRepository
                .findById(subTaskId)
                .orElseThrow(() -> TaskExceptions.subTaskNotFound(subTaskId));

        final CommentDTO commentDTO = addCommentToSubTaskInternal(subTask, comment);

        eventsEmitter.emitNotification(NotificationEvent.builder()
                .notificationType(NotificationType.SUB_TASK_COMMENTED)
                .senderId(securityHelper.currentUserId())
                .projectId(subTask.getMainTask().getProjectId())
                .referenceId(subTaskId)
                .subjectId(commentDTO.getId())
                .build());
        return commentDTO;
    }

    private CommentDTO addCommentToSubTaskInternal(SubTaskEntity subTask, CreateCommentDTO comment) {
        UserDto currentUser = LecwUtils.currentUser();
        Integer userId = Integer.parseInt(currentUser.getId());

        if (null == comment.getAttachment() && (null == comment.getComment() || comment.getComment().isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one of 'attachment' or 'text' need to be present");
        }

        // Check whether the user is part of the project team (according to David all users from a project can comment)
        securityHelper.validateUserIsPartOfProject(userId, subTask.getMainTask().getProjectId());

        validateAttachment(comment, userId);

        var attachmentDTO = materializeAttachment(subTask.getId(), comment);

        return CommentDTO.from(
                materializeComment(subTask.getId(), comment, userId),
                securityHelper.user(userId),
                attachmentDTO
        );
    }

    @Override
    public CommentDTO getComment(Integer commentId) {
        return toDTO(getValidComment(commentId));
    }

    @Override
    @Transactional
    public CommentDTO updateComment(Integer commentId, CreateCommentDTO comment) {
        var commentEntity = getValidComment(commentId);

        commentEntity.setText(comment.getComment());

        MediaWidgetFileDTO attachmentDTO = null;

        if(! Objects.equals(commentEntity.getAttachmentId(), comment.getAttachment())) {
            if(commentEntity.getAttachmentId() != null) {
                disableAttachment(commentEntity.getSubTaskId(), commentEntity.getAttachmentId());
                commentEntity.setAttachmentId(null);
            }
            if(comment.getAttachment() != null) {
                attachmentDTO = materializeAttachment(commentEntity.getSubTaskId(), comment);
                commentEntity.setAttachmentId(comment.getAttachment());
            }
        }

        eventsEmitter.emitCacheUpdate(ReferenceType.SUB_TASK_COMMENT, commentEntity.getId());

        return CommentDTO.from(
                commentEntity,
                securityHelper.user(commentEntity.getCreatedBy()),
                attachmentDTO
        );
    }

    @Override
    @Transactional
    public void archiveComment(Integer commentId) {
        var commentEntity = getValidComment(commentId);

        if(commentEntity.getAttachmentId()!=null) {
            disableAttachment(commentEntity.getSubTaskId(), commentEntity.getAttachmentId());
        }

        commentEntity.setDisabled(LocalDateTime.now());
    }

    private CommentEntity getValidComment(Integer commentId) {
        return  commentRepository.findById(commentId)
                .orElseThrow(() -> TaskExceptions.commentNotFound(commentId));
    }

    private CommentDTO toDTO(CommentEntity commentEntity) {
        var subTask = subTaskRepository
                .findById(commentEntity.getSubTaskId())
                .orElseThrow(() -> TaskExceptions.subTaskNotFound(commentEntity.getSubTaskId()));

        return CommentDTO.from(
                commentEntity,
                securityHelper.user(commentEntity.getCreatedBy()),
                Optional.ofNullable(commentEntity.getAttachmentId())
                        .map(id -> mediaWidgetService.findByFileId(ReferenceType.MAIN_TASK, subTask.getMainTask().getId(), id))
                        .orElse(null)
        );
    }

    private CommentEntity materializeComment(Integer subTaskId, CreateCommentDTO comment, Integer userId) {
        CommentEntity ce = new CommentEntity();
        ce.setCreatedBy(userId);
        ce.setCreatedDate(LocalDateTime.now(ZoneOffset.UTC));
        ce.setSubTaskId(subTaskId);
        ce.setText(comment.getComment());
        ce.setAttachmentId(comment.getAttachment());

        return commentRepository.save(ce);
    }

    private void validateAttachment(CreateCommentDTO comment, Integer userId) {
        if (null != comment.getAttachment()) {
            File attachedFile = fileRepository.findById(comment.getAttachment())
                    .orElseThrow(() -> TaskExceptions.attachmentNotFound(comment.getAttachment()));

            // Check that the author of the file is the user making the comment and that the file was not deleted
            if (!attachedFile.getCreatedBy().equals(userId) || attachedFile.getDisabled() != null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "File of id " + comment.getAttachment() + " was created by " + attachedFile.getCreatedBy() + " and not by " + userId);
            }

            if (!attachedFile.getReferenceType().equals(ReferenceType.TEMPORARY.name())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "File of id " + comment.getAttachment() + " can not be used for upload");
            }
        }
    }

    private void disableAttachment(Integer subTaskId, Integer fileId) {
        var subTask = subTaskRepository
                .findById(subTaskId)
                .orElseThrow(() -> TaskExceptions.subTaskNotFound(subTaskId));
        var fileDTO = mediaWidgetService.findByFileId(ReferenceType.MAIN_TASK,subTask.getMainTask().getId(), fileId);

        mediaWidgetService.disableSystemFile(fileDTO.getId());
    }
    private MediaWidgetFileDTO materializeAttachment(Integer subTaskId, CreateCommentDTO comment) {
        if (comment.getAttachment() == null) {
            return null;
        }
        var subTask = subTaskRepository
                .findById(subTaskId)
                .orElseThrow(() -> TaskExceptions.subTaskNotFound(subTaskId));
        return mediaWidgetService
                .convertTemporaryFileToSystemFile(
                        comment.getAttachment(),
                        ReferenceType.MAIN_TASK,
                        subTask.getMainTask().getId(),
                        getSystemCommentsFolder(subTask.getMainTask().getId()).getId()
                );
    }

    private MediaWidgetFileDTO getSystemCommentsFolder(Integer mainTaskId) {
            return mediaWidgetService.getSystemFolderByName(
                    ReferenceType.MAIN_TASK,
                    mainTaskId,
                    COMMENTS_FOLDER)
                    .orElseThrow(() -> TaskExceptions.systemFolderForTaskNotFound(COMMENTS_FOLDER, mainTaskId));
        }
}
