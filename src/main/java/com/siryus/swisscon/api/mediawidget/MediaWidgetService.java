package com.siryus.swisscon.api.mediawidget;

import com.siryus.swisscon.api.general.reference.ReferenceType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface MediaWidgetService {
    Optional<MediaWidgetFileDTO> getRootFolderByName(ReferenceType referenceType, Integer referenceId, String name);
    
    List<MediaWidgetFileDTO> listRoots(ReferenceType referenceType, Integer referenceId, MediaWidgetQueryScope queryScope);

    List<MediaWidgetFileDTO> listChildren(Integer parentId, MediaWidgetQueryScope queryScope);

    MediaWidgetFileDTO createFolder(ReferenceType referenceType, Integer referenceId, CreateFolderRequest request);

    MediaWidgetFileDTO createSystemFolder(ReferenceType referenceType, Integer referenceId, Integer parentNodeId, String name);

    List<MediaWidgetFileDTO> createDefaultFolders(ReferenceType referenceType, Integer referenceId);

    MediaWidgetFileDTO createFile(ReferenceType referenceType, Integer referenceId, Integer parentNodeId, MultipartFile file);
    
    MediaWidgetFileDTO createSystemFile(ReferenceType referenceType, Integer referenceId, Integer parentNodeId, MultipartFile file);


    void deleteFolder(Integer folderNodeId);

    void deleteFile(Integer fileNodeId);
    
    void disableSystemFile(Integer fileNodeId);

    MediaWidgetFileDTO updateFile(UpdateFileRequest request);

    Optional<MediaWidgetFileDTO> getSystemFolderByName(ReferenceType referenceType, Integer referenceId, String systemFolderName);
    
    void checkUploadPermissions(Integer userId, ReferenceType referenceType, Integer referenceId);    

    MediaWidgetFileDTO findByFileId(ReferenceType ownerReferenceType, Integer ownerReferenceId, Integer fileId);

    MediaWidgetFileDTO convertTemporaryFileToSystemFile(Integer temporaryFileId, ReferenceType ownerReferenceType, Integer ownerReferenceId, Integer parentFolderId );

    void deleteAllOwnedBy(ReferenceType location, Integer id);

    void createSystemFolders(ReferenceType referenceType, Integer referenceId);
}
