package com.siryus.swisscon.api.mediawidget;

import com.naturalprogrammer.spring.lemon.commonsweb.util.LecwUtils;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.util.List;

@RestController("mediaWidgetController")
@Api(
        tags = "MediaWidget",
        produces = "application/json"
)
@RequestMapping("/api/rest/media")
public class MediaWidgetController {

    private final MediaWidgetService service;

    @Inject
    MediaWidgetController(MediaWidgetService service) {
        this.service = service;
    }


    @GetMapping(value = "{referenceType}/{referenceId}")
    public List<MediaWidgetFileDTO> list(
            @PathVariable(name = "referenceType") ReferenceType referenceType,
            @PathVariable(name = "referenceId") Integer referenceId,
            @RequestParam(name = "parentNodeId", required = false) Integer parentNodeId,
            @RequestParam(name = "fileMediaType", required = false) String fileMediaType,
            @RequestParam(name = "createdBy", required = false) Integer createdBy,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "type", required = false) String type
            )
    {
        MediaWidgetQueryScope queryScope = new MediaWidgetQueryScope(fileMediaType, createdBy, name, type);
        List<MediaWidgetFileDTO> result;

        if (parentNodeId != null) {
            result = service.listChildren(parentNodeId, queryScope);
        }
        else {
            result = service.listRoots(referenceType, referenceId, queryScope);
        }

        return result;
    }

    @PostMapping(value = "{referenceType}/{referenceId}/folder")
    @PreAuthorize("hasPermission(#referenceId, #referenceType.name(), 'MEDIA_CREATE')")
    public MediaWidgetFileDTO createFolder(
            @PathVariable(name = "referenceType") ReferenceType referenceType,
            @PathVariable(name = "referenceId") Integer referenceId,
            @RequestBody CreateFolderRequest request
    ) {
        return service.createFolder(referenceType, referenceId, request);
    }

    @PostMapping(value = "{referenceType}/{referenceId}/file", headers = ("content-type=multipart/*"))
    @PreAuthorize("hasPermission(#referenceId, #referenceType.name(), 'MEDIA_CREATE')")
    public MediaWidgetFileDTO createFile(
            @PathVariable(name = "referenceType") ReferenceType referenceType,
            @PathVariable(name = "referenceId") Integer referenceId,
            @RequestParam(name = "parentNodeId", required = false) Integer parentNodeId,
            @RequestParam(name = "file") MultipartFile multipartFile
    ) {
        return service.createFile(referenceType, referenceId, parentNodeId, multipartFile);
    }

    /**
     * @deprecated  for creating temporary files use /files (POST), We don't upload system files directly
     */
    @Deprecated(forRemoval = true)
    @PostMapping(value = "{referenceType}/{referenceId}/system-file", headers = ("content-type=multipart/*"))
    @PreAuthorize("hasPermission(#referenceId, #referenceType.name(), 'MEDIA_CREATE')")
    @ApiOperation(value = "for uploading temporary files use /files (POST), We don't upload system files directly")
    public MediaWidgetFileDTO createSystemFile(
            @PathVariable(name = "referenceType") ReferenceType referenceType,
            @PathVariable(name = "referenceId") Integer referenceId,
            @RequestParam(name = "folder") String systemFolderName,
            @RequestParam(name = "file") MultipartFile multipartFile
    ) {
        if (multipartFile.isEmpty()) {
            throw  MediaWidgetException.failToGetFileFromRequest();
        }

        Integer userId = Integer.parseInt(LecwUtils.currentUser().getId());
        
        service.checkUploadPermissions(userId, referenceType, referenceId);
        
        MediaWidgetFileDTO systemFolder = service.getSystemFolderByName(referenceType, referenceId, systemFolderName)
                .orElseThrow(() -> MediaWidgetException.systemFolderNotFound(systemFolderName));
        
        // System files are always created as temporary and updated on owning entity creation
        return service.createSystemFile(ReferenceType.TEMPORARY, null, systemFolder.getId(), multipartFile);
    }    

    @PostMapping(value = "/folder/update")
    @PreAuthorize("hasPermission(#request.nodeId, 'com.siryus.swisscon.api.util.entitytree.nestedsets.EntityTreeNode', 'MEDIA_UPDATE')")    
    public MediaWidgetFileDTO updateFolder(@RequestBody UpdateFileRequest request) {
        return service.updateFile(request);
    }

    @PostMapping(value = "/file/update")
    @PreAuthorize("hasPermission(#request.nodeId, 'com.siryus.swisscon.api.util.entitytree.nestedsets.EntityTreeNode', 'MEDIA_UPDATE')")        
    public MediaWidgetFileDTO updateFile(@RequestBody UpdateFileRequest request) {
        return service.updateFile(request);
    }

    @PostMapping(value = "/folder/delete")
    @PreAuthorize("hasPermission(#request.nodeId, 'com.siryus.swisscon.api.util.entitytree.nestedsets.EntityTreeNode', 'MEDIA_ARCHIVE')")    
    public ResponseEntity<String> deleteFolder(@RequestBody DeleteFileRequest request) {
        service.deleteFolder(request.nodeId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/file/delete")
    @PreAuthorize("hasPermission(#request.nodeId, 'com.siryus.swisscon.api.util.entitytree.nestedsets.EntityTreeNode', 'MEDIA_ARCHIVE')")        
    public ResponseEntity<String> deleteFile(@RequestBody DeleteFileRequest request) {
        service.deleteFile(request.nodeId);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
