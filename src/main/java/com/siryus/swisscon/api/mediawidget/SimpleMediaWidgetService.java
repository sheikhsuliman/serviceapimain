package com.siryus.swisscon.api.mediawidget;

import com.siryus.swisscon.api.util.TranslationUtil;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.auth.user.UserService;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.file.file.FileService;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.entitytree.EntityTreeNodeDTO;
import com.siryus.swisscon.api.util.entitytree.EntityTreeService;
import com.siryus.swisscon.api.util.entitytree.EntityTreeServiceFactory;
import com.siryus.swisscon.api.util.security.SecurityHelper;
import com.siryus.swisscon.api.util.validator.DTOValidator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SimpleMediaWidgetService implements MediaWidgetService {
    
    private final MediaWidgetFileDAO dao;
    private final UserService userService;
    private final EntityTreeService treeService;
    private final FileService fileService;
    private final SecurityHelper securityHelper;
    private final TranslationUtil transationUtil;
    
    @Inject
    public SimpleMediaWidgetService(
            MediaWidgetFileDAO dao,
            UserService userService,
            EntityTreeServiceFactory treeServiceFactory,
            FileService fileService,
            SecurityHelper securityHelper,
            TranslationUtil translationUtil) {
        this.dao = dao;
        this.userService = userService;
        this.treeService = treeServiceFactory.createService(Collections.singletonList(MediaWidgetType.FOLDER.name()));
        this.fileService = fileService;
        this.securityHelper = securityHelper;
        this.transationUtil = translationUtil;
    }

    @Override
    public Optional<MediaWidgetFileDTO> getRootFolderByName(ReferenceType referenceType, Integer referenceId, String name) {
        return listRoots(referenceType, referenceId, new MediaWidgetQueryScope(null, null, name, null)).stream()
                .filter(f -> f.getMimeType().equals(MediaConstants.FOLDER_MIME_TYPE))
                .findFirst();
    }

    @Override
    public List<MediaWidgetFileDTO> listRoots(ReferenceType referenceType, Integer referenceId, MediaWidgetQueryScope queryScope) {
        List<EntityTreeNodeDTO> roots = treeService.listRoots(referenceType, referenceId, MediaWidgetType.FILE.name(), MediaWidgetType.FOLDER.name());

        List<File> rootFiles = dao.filterFiles(queryScope, roots.stream().mapToInt(EntityTreeNodeDTO::getEntityId).toArray());

        return combine(rootFiles, roots);
    }
    
    @Override
    public List<MediaWidgetFileDTO> listChildren(Integer parentId, MediaWidgetQueryScope queryScope) {
        List<EntityTreeNodeDTO> nodes = treeService.listChildren(parentId);

        List<File> nodeFiles = dao.filterFiles(queryScope, nodes.stream().mapToInt(EntityTreeNodeDTO::getEntityId).toArray());

        return combine(nodeFiles, nodes);
    }

    @Transactional
    @Override
    public MediaWidgetFileDTO createFolder(ReferenceType referenceType, Integer referenceId, CreateFolderRequest request) {
        DTOValidator.validateAndThrow(request);

        if (request.parentNodeId == null ) {
            return internalCreateRootFolder(referenceType, referenceId, request.name, false);
        }
        else {
            return internalCreateFolder(referenceType, referenceId, request.parentNodeId, request.name, false);
        }
    }

    @Transactional
    @Override
    public MediaWidgetFileDTO createFile(ReferenceType referenceType, Integer referenceId, Integer parentNodeId, MultipartFile multipartFile) {
        if (parentNodeId == null) {
            return internalCreateRootFile(referenceType, referenceId, multipartFile, false);
        }
        else {
            return internalCreateFile(referenceType, referenceId, parentNodeId, multipartFile, false);
        }
    }

    @Transactional
    @Override
    public MediaWidgetFileDTO createSystemFolder(ReferenceType referenceType, Integer referenceId, Integer parentNodeId, String name) {
        if (parentNodeId == null) {
            return internalCreateRootFolder(referenceType, referenceId, name, true);
        }
        else {
            return internalCreateFolder(referenceType, referenceId, parentNodeId, name, true);
        }
    }

    @Transactional
    @Override
    public List<MediaWidgetFileDTO> createDefaultFolders(ReferenceType referenceType, Integer referenceId) {
        return MediaConstants.DEFAULT_MEDIA_FOLDER_KEYS.stream()
                .map(translationKey -> transationUtil.get(translationKey, userService.getCurrentUserLang().getId()))
                .map(folderName -> createFolder(referenceType, referenceId, new CreateFolderRequest(null, folderName)))
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public MediaWidgetFileDTO createSystemFile(ReferenceType referenceType, Integer referenceId, Integer parentNodeId, MultipartFile multipartFile) {
        if (parentNodeId == null) {
            return internalCreateRootFile(referenceType, referenceId, multipartFile, true);
        }
        else {
            return internalCreateFile(referenceType, referenceId, parentNodeId, multipartFile, true);
        }
    }

    @Transactional
    @Override
    public MediaWidgetFileDTO convertTemporaryFileToSystemFile(Integer temporaryFileId, ReferenceType ownerReferenceType, Integer ownerReferenceId, Integer parentFolderId) {
        // TODO: Validation
        File temporaryFile = fileService.findById(temporaryFileId);

        temporaryFile.setReferenceType(ownerReferenceType.name());
        temporaryFile.setReferenceId(ownerReferenceId);
        temporaryFile.setIsSystemFile(true);

        return internalCreateFile(parentFolderId, temporaryFile);
    }

    @Transactional
    @Override
    public MediaWidgetFileDTO updateFile(UpdateFileRequest request ) {
        DTOValidator.validateAndThrow(request);

        EntityTreeNodeDTO node = treeService.getNode(request.nodeId);

        File original = fileService.findById(node.getEntityId());
        original.setFilename(request.name);
        File file = fileService.update(original);

        return combine(file, node);
    }

    @Transactional
    @Override
    public void deleteFolder(Integer folderNodeId) {
        EntityTreeNodeDTO node = treeService.getNode(folderNodeId);

        if (!node.isEmpty()) {
            throw MediaWidgetException.canNotDeleteNonEmptyFolder();
        }
        
        fileService.deletePermanently(node.getEntityId());
        treeService.deleteNode(folderNodeId);
    }

    @Transactional
    @Override
    public void deleteFile(Integer fileNodeId) {
        EntityTreeNodeDTO node = treeService.getNode(fileNodeId);
        fileService.disable(node.getEntityId());
        treeService.deleteNode(fileNodeId);
    }
    
    @Transactional
    @Override
    public void disableSystemFile(Integer fileNodeId) {
        EntityTreeNodeDTO node = treeService.getNode(fileNodeId);
        fileService.disableSystemFile(node.getEntityId());
        treeService.deleteNode(fileNodeId);
    }    

    @Override
    public void deleteAllOwnedBy(ReferenceType referenceType, Integer referenceId) {
        treeService.deleteAllOwnedBy(referenceType, referenceId);
        fileService.deleteAllOwnedBy(referenceType, referenceId);
    }

    private File uploadFile(ReferenceType referenceType, Integer referenceId, MultipartFile multipartFile, Boolean isSystem, String filename) {
        File tempFile = File.builder()
                .referenceType(referenceType.toString())
                .referenceId(referenceId)
                .isSystemFile(isSystem)
                .build();

        return  fileService.createAndPersistFile(tempFile, multipartFile, filename);
    }

    private List<MediaWidgetFileDTO> combine(List<File> files, List<EntityTreeNodeDTO> nodes) {
        Map<Integer, EntityTreeNodeDTO> nodesMap = nodes.stream().collect(Collectors.toMap(EntityTreeNodeDTO::getEntityId, Function.identity()));

        return files.stream().map( f -> new MediaWidgetFileDTO(
                nodesMap.get(f.getId()), f, buildCreatedBy(f)
        )).collect(Collectors.toList());
    }

    private MediaWidgetFileDTO combine(File file, EntityTreeNodeDTO node) {
        return new MediaWidgetFileDTO(
            node,
            file,
            buildCreatedBy(file)
        );
    }

    private MediaWidgetFileDTO.CreatedBy buildCreatedBy(File file) {
        User user = userService.findById(file.getCreatedBy());

        if (user == null) {
            return new MediaWidgetFileDTO.CreatedBy(file.getCreatedBy());
        }

        String userName = user.getGivenName() + " " + user.getSurName();
        if (user.getGivenName() == null) {
            userName = user.getSurName();
        }
        else if (user.getSurName() == null) {
            userName = user.getGivenName();
        }

        String userPictureURL = user.getPicture() == null ? null : user.getPicture().getUrlSmall();

        return new MediaWidgetFileDTO.CreatedBy(user.getId(), userName, userPictureURL);
    }

    @Override
    public void checkUploadPermissions(Integer userId, ReferenceType referenceType, Integer referenceId) {
        // TODO: add some logic for who can attach files to a work log 
        
        if (null == userService.findById(userId)) {
            throw MediaWidgetException.userNotFound(userId);
        }

        if (referenceType == ReferenceType.MAIN_TASK) {
            securityHelper.validateUserIsPartOfMainTaskProject(userId, referenceId);
        } else {
            throw MediaWidgetException.notImplemented();
        }
    }

    public Optional<MediaWidgetFileDTO> getSystemFolderByName(ReferenceType referenceType, Integer referenceId, String systemFolderName) {
        Optional<MediaWidgetFileDTO> systemFolder = getRootFolderByName(referenceType, referenceId, MediaConstants.SYSTEM_FOLDER);
        if (systemFolder.isEmpty()) {
            return systemFolder;
        }
        List<MediaWidgetFileDTO> children = listChildren(systemFolder.get().getId(), new MediaWidgetQueryScope(null, null, systemFolderName, MediaWidgetType.FOLDER.name()));
        if (null == children || children.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(children.get(0));
    }
    
    @Override
    public MediaWidgetFileDTO findByFileId(ReferenceType ownerReferenceType, Integer ownerReferenceId, Integer fileId) {
        File file = fileService.findById(fileId);

        return new MediaWidgetFileDTO(
                treeService.listRoots(ownerReferenceType, ownerReferenceId, MediaWidgetType.FILE.name(), MediaWidgetType.FOLDER.name())
                        .stream().map( r -> treeService.listNodesByEntity(r.getId(), MediaWidgetType.FILE.name(), fileId )).flatMap(List::stream).findFirst()
                        .orElseThrow(() -> MediaWidgetException.fileNotFound(fileId)),
                file,
                buildCreatedBy(file)
        );
    }
    
    @Transactional
    @Override
    public void createSystemFolders(ReferenceType referenceType, Integer referenceId) {        
        MediaWidgetFileDTO rootFolder;
        
        switch(referenceType) {
            case MAIN_TASK:    
                rootFolder = createSystemFolder(referenceType, referenceId, null, MediaConstants.SYSTEM_FOLDER);                
                createSystemFolder(referenceType, referenceId, rootFolder.getId(), MediaConstants.DESCRIPTION_FOLDER);
                createSystemFolder(referenceType, referenceId, rootFolder.getId(), MediaConstants.COMMENTS_FOLDER);
                createSystemFolder(referenceType, referenceId, rootFolder.getId(), MediaConstants.START_TASK_FOLDER);
                createSystemFolder(referenceType, referenceId, rootFolder.getId(), MediaConstants.COMPLETE_TASK_FOLDER);
                createSystemFolder(referenceType, referenceId, rootFolder.getId(), MediaConstants.REJECT_TASK_FOLDER);
                
                break;
            case CONTRACT:
                rootFolder = createSystemFolder(referenceType, referenceId, null, MediaConstants.SYSTEM_FOLDER);
                createSystemFolder(referenceType, referenceId, rootFolder.getId(), MediaConstants.COMMENTS_FOLDER);                
                
                break;
            default:
                throw MediaWidgetException.notImplemented();
        }
    }
    
    /**
     * Determines whether a non-root file name is unique at its level in the tree.
     * 
     * Uniqueness is established based on (name, parentNodeId)
     * 
     * @param name file/folder name
     * @param parentNodeId parent node id
     */
    private void validateParameters(String name, Integer parentNodeId) {
        if (null == name || name.isBlank()) {
            throw MediaWidgetException.fileNameIsMissing();
        }
        
        if (null == parentNodeId) {
            throw MediaWidgetException.parentNodeIdIsMissing();
        }
    }
    
    /**
     * Determines whether a root file/folder name is unique.
     *
     * Uniqueness is established based on (name, referenceType, referenceId)
     *
     * @param name file or folder name
     * @param referenceType owner reference type
     * @param referenceId owner reference id
     *
     */
    private void validateParameters(String name, ReferenceType referenceType, Integer referenceId) {
        if (null == name || name.isBlank()) {
            throw MediaWidgetException.fileNameIsMissing();
        }

        if (referenceType == null || referenceId == null) {
            throw MediaWidgetException.missingReferenceTypeOrId();
        }

    }

    private String getFilenameForRoot(String filename, ReferenceType referenceType, Integer referenceId, MediaWidgetType type) {
        List<EntityTreeNodeDTO> roots = treeService.listRoots(referenceType,
                referenceId,
                MediaWidgetType.FILE.name(),
                type.name());
        return FilenameUtil.evaluateFilename(filename, getFilenames(roots));
    }

    private String getFileNameForChildren(String filename, Integer parentNodeId, ReferenceType referenceType, MediaWidgetType type) {
        List<EntityTreeNodeDTO> siblings = treeService.listChildren(parentNodeId).stream()
                .filter(n -> n.getEntityType().equals(type.name()))
                .collect(Collectors.toList());
        return FilenameUtil.evaluateFilename(filename, getFilenames(siblings));
    }

    private List<String> getFilenames(List<EntityTreeNodeDTO> nodes) {
        return nodesToFiles(nodes)
                .stream().map(File::getFilename)
                .collect(Collectors.toList());
    }

    private List<File> nodesToFiles(List<EntityTreeNodeDTO> nodes) {
        return nodes.stream()
                .map(EntityTreeNodeDTO::getEntityId)
                .map(fileService::findById)
                .filter(f->f.getDisabled() == null)
            .collect(Collectors.toList());
    }
        
    private MediaWidgetFileDTO internalCreateRootFolder(ReferenceType referenceType, Integer referenceId, String name, Boolean isSystem) {
        validateParameters(name, referenceType, referenceId);

        String filename = getFilenameForRoot(name, referenceType, referenceId, MediaWidgetType.FOLDER);

        File newFolder = dao.createFolder(referenceType, referenceId, filename, isSystem);
        EntityTreeNodeDTO root = treeService.createRoot(referenceType, referenceId, MediaWidgetType.FOLDER.name(), newFolder.getId());
        return combine(newFolder, root);        
    }    
    
    private MediaWidgetFileDTO internalCreateFolder(ReferenceType referenceType, Integer referenceId, Integer parentNodeId, String name, Boolean isSystem) {
        validateParameters(name, parentNodeId);

        String filename = getFileNameForChildren(name, parentNodeId, referenceType, MediaWidgetType.FOLDER);

        File newFolder = dao.createFolder(referenceType, referenceId, filename, isSystem);
        
        // Should not create non-system folder within system parent and system location within non-system parent
        EntityTreeNodeDTO parentNode = treeService.getNode(parentNodeId);
        File parentFolder = fileService.findById(parentNode.getEntityId());
        if (!parentFolder.getIsSystemFile().equals(isSystem)) {
            throw MediaWidgetException.canNotCreateFolderAtSpecifiedLocation();
        }
        
        EntityTreeNodeDTO node = treeService.addChild(parentNodeId, MediaWidgetType.FOLDER.name(), newFolder.getId());
        return combine(newFolder, node);        
    }

    private MediaWidgetFileDTO internalCreateRootFile(ReferenceType referenceType, Integer referenceId, MultipartFile multipartFile, Boolean isSystem) {
        validateParameters(multipartFile.getOriginalFilename(), referenceType, referenceId);

        String filename = getFilenameForRoot(multipartFile.getOriginalFilename(), referenceType, referenceId, MediaWidgetType.FILE);

        File newFile = uploadFile(referenceType, referenceId, multipartFile, isSystem, filename);

        EntityTreeNodeDTO node = treeService.createRoot(referenceType, referenceId, MediaWidgetType.FILE.name(), newFile.getId());

        return combine(newFile, node);        
    }    
    
    private MediaWidgetFileDTO internalCreateFile(ReferenceType referenceType, Integer referenceId, Integer parentNodeId, MultipartFile multipartFile, Boolean isSystem) {
        validateParameters(multipartFile.getOriginalFilename(), parentNodeId);

        String filename = getFileNameForChildren(multipartFile.getOriginalFilename(), parentNodeId, referenceType, MediaWidgetType.FILE);

        return internalCreateFile(parentNodeId, uploadFile(referenceType, referenceId, multipartFile, isSystem, filename));
    }

    private MediaWidgetFileDTO internalCreateFile(Integer parentNodeId, File file) {

        // Check that a system file is only ever created within a system folder location (or a root location)
        EntityTreeNodeDTO parentNode = treeService.getNode(parentNodeId);
        File parentFolder = fileService.findById(parentNode.getEntityId());
        if (!parentFolder.getIsSystemFile().equals(file.getIsSystemFile())) {
            throw MediaWidgetException.canNotCreateFileAtSpecifiedLocation();
        }
        
        EntityTreeNodeDTO node = treeService.addChild(parentNodeId, MediaWidgetType.FILE.name(), file.getId());

        return combine(file, node);
    }

}
