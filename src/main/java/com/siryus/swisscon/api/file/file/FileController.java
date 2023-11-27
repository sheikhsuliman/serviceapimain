package com.siryus.swisscon.api.file.file;

import com.siryus.swisscon.api.file.FileExceptions;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@RestController("fileController")
@Api(
        tags = "Files",
        produces = "application/json, application/hal+json, application/vnd.api+json"
)
@RequestMapping("/api/rest/files")
public class FileController {

    private final FileService fileService;

    private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);

    public static final String MULTIFORM_FILE_IDENTIFIER = "file";

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @ApiOperation(value = "Create a file, uploading a file at the same time",
            notes = "The uploaded file and object correspond to the `file` and `resource` parts of the request respectively .")
    @PostMapping(headers = ("content-type=multipart/*"))
    @ResponseStatus(HttpStatus.CREATED)
    public File createFile(
            MultipartHttpServletRequest request, HttpServletResponse response) {

        MultipartFile multipartFile = validateAndGetFile(request);

        // valueOf proofs not null and if the reference type exists
        ReferenceType referenceType;
        try {
            referenceType = ReferenceType.valueOf(request.getParameter("referenceType"));
        } catch (Exception e) {
            throw FileExceptions.referenceTypeIncorrect(request.getParameter("referenceType"));
        }

        // check if referenceId is numeric
        String referenceIdStr = request.getParameter("referenceId");
        if(StringUtils.isNotBlank(referenceIdStr) && !StringUtils.isNumeric(referenceIdStr)) {
            throw FileExceptions.referenceIdNotNumeric(request.getParameter("referenceId"));
        }

        // check if non temporary files have a reference id
        if (!ReferenceType.TEMPORARY.equals(referenceType) && StringUtils.isBlank(referenceIdStr)) {
            throw FileExceptions.nonTemporaryFileNeedsReferenceId();
        }

        Integer referenceId = StringUtils.isNotBlank(referenceIdStr) ? Integer.valueOf(referenceIdStr): null;

        // Set properties from controller method
        File tempFile = File.builder()
                .referenceType(referenceType.toString())
                .referenceId(referenceId)
                .isSystemFile(Boolean.FALSE)
                .build();
        Long startTime = System.currentTimeMillis();

        File result = fileService.createAndPersistFile(tempFile, multipartFile);
        
        // This parameter, if present, is used to link total execution time to S3 upload time
        String batchId = request.getParameter("batchId");
                
        if (batchId != null) {
            LOGGER.error("createFile execution results (batchId, time in ms): " + batchId + " " +(System.currentTimeMillis() - startTime));
        }
        
        return result;
    }

    // TODO check in the UI if this is needed
    // TODO Also probable Security issues here
    @GetMapping(value="{id}")
    @ApiOperation(
            value = "Find by id",
            notes = "Find a resource by it's identifier"
    )
    public ResponseEntity<File> getById(@ApiParam(name = "id",required = true) @PathVariable Integer id) {
        File model = fileService.findById(id);
        if (model == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } return new ResponseEntity<>(model, HttpStatus.OK);
    }

    @ApiOperation("Rename file with given file ID")
    @PostMapping("{fileId}/rename")
    public File renameFile(@PathVariable Integer fileId, @RequestBody RenameFileRequest renameFileRequest ) {
        return fileService.renameFile(fileId, renameFileRequest);
    }

    public static MultipartFile validateAndGetFile(MultipartHttpServletRequest request) {
        return Optional .ofNullable(request.getFile(FileController.MULTIFORM_FILE_IDENTIFIER))
                .orElseThrow(FileExceptions::noFileInForm);
    }
}
