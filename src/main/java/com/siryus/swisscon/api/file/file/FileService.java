package com.siryus.swisscon.api.file.file;

import com.siryus.swisscon.api.file.FileExceptions;
import com.siryus.swisscon.api.general.reference.ReferenceService;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.validator.Reference;
import com.siryus.swisscon.soa.EventsEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service("fileService")
@Validated
public class FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);
    private static final String FILE_CLEANUP_JOB = "TEMPORARY FILE CLEANUP";

    private final FileS3PersistenceService persistenceService;
    private final ReferenceService referenceService;
    private final FileRepository fileRepository;
    private final EventsEmitter eventsEmitter;

    @Autowired
    public FileService(FileS3PersistenceService persistenceService, ReferenceService referenceService, FileRepository fileRepository, EventsEmitter eventsEmitter) {
        this.persistenceService = persistenceService;
        this.referenceService = referenceService;
        this.fileRepository = fileRepository;
        this.eventsEmitter = eventsEmitter;
    }

    @Transactional
    public File createAndPersistFile(File tempFile, MultipartFile requestFile) {
        return createAndPersistFile(tempFile, requestFile, null);
    }

    @Transactional
    public File createAndPersistFile(File tempFile, MultipartFile requestFile, String filename) {
        // Create record in db and retrieve id
        File fileWithIdButNoProperties = create(tempFile);

        try {
            // Store the file in S3 and set properties
            File fileWithProperties = persistFileAndSetProperties(fileWithIdButNoProperties, requestFile, filename);

            // Store the updated properties in the database
            return fileRepository.save(fileWithProperties);
        } catch (Exception e) {
            throw FileExceptions.failedToPersistFile(tempFile.getFilename());
        }
    }

    @Transactional
    public File renameFile(@Reference(ReferenceType.FILE) Integer fileId, @Valid RenameFileRequest renameFileRequest) {
        File file = findById(fileId);
        if (!file.getReferenceType().equals(ReferenceType.TEMPORARY.name())) {
            throw FileExceptions.canNotRenameNonTemporaryFile(fileId);
        }
        file.setFilename(renameFileRequest.getNewFileName());

        eventsEmitter.emitCacheUpdate(ReferenceType.FILE, fileId);
        return file;
    }


    public boolean isNewPicture(File oldPicture, File newPicture) {
        return oldPicture == null || !oldPicture.getId().equals(newPicture.getId());
    }

    public File create(File resource) {
        referenceService.validateForeignKey(resource.getReferenceType(), resource.getReferenceId());
        return fileRepository.save(resource);
    }

    public File update(File file) {
        if (file.getIsSystemFile()) {
            throw FileExceptions.canNotUpdateSystemFile(file.getFilename());
        }
        
        return fileRepository.save(file);
    }

    @Transactional
    public void updateFileReferences(List<Integer> ids, Integer referenceId, ReferenceType referenceType) {
        Set<Integer> idSet = Optional.ofNullable(ids).map(HashSet::new).orElse(new HashSet<>());
        fileRepository.updateFileReferences(idSet, referenceId, referenceType.name());
    }
    
    @Transactional
    public void cleanupTemporaryFiles() {
        LOGGER.info(FILE_CLEANUP_JOB + ": Starting...");

        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        List<File> tempFiles = fileRepository.findTemporaryFilesOlderThan(oneDayAgo);
        tempFiles.forEach(f -> {
            LOGGER.info(FILE_CLEANUP_JOB + ": Delete File id=" + f.getId() + " , url=" + f.getUrl());
            deletePermanently(f.getId());
        });

        LOGGER.info(FILE_CLEANUP_JOB + (tempFiles.isEmpty() ? ": No Files to Cleanup today" : ": Finished"));
    }

    public File findById(Integer id) {
        return fileRepository.findById(id).orElse(null);
    }

    public File getFile(Integer id) {
        return fileRepository.findById(id).orElseThrow(() -> FileExceptions.fileNotFound(id));
    }

    /**
     * Erases the file from DB and S3
     */
    @Transactional
    public void deletePermanently(Integer id) {
        fileRepository.deleteById(id);
        String basePath = getBasePath(id);
        persistenceService.deleteByPrefix(basePath);
    }

    /**
     * Disables the file in the DB and keeps it on S3
     */
    @Transactional
    public void disable(File file) {
        if (file.getIsSystemFile()) {
            throw FileExceptions.canNotDeleteSystemFile(file.getFilename());
        }
        fileRepository.disable(file.getId());
    }
    
    /**
     * Disables the file in the DB and keeps it on S3
     */
    @Transactional
    public void disableSystemFile(Integer id) {
        if (id != null) {
            fileRepository.disable(id);
        }
    }    

    /**
     * Disables the file in the DB and keeps it on S3
     */
    @Transactional
    public void disable(Integer id) {
        File file = findById(id);
        disable(file);
    }

    private File persistFileAndSetProperties(File resource, MultipartFile multipartFile, String filename) {
        // Persist file
        assert multipartFile != null;
        FileData fileData = this.persistenceService.saveFile(multipartFile, getBasePath(resource.getId()) + multipartFile.getOriginalFilename());

        // Set properties
        resource.setFilename(Optional.ofNullable(filename).orElse(multipartFile.getOriginalFilename()));
        resource.setMimeType(multipartFile.getContentType());
        resource.setLength(multipartFile.getSize());
        resource.setUrl(fileData.getUrl());
        resource.setUrlMedium(fileData.getUrlMedium());
        resource.setUrlSmall(fileData.getUrlSmall());
        return resource;
    }

    public String getBasePath(Integer id) {
        return File.class.getSimpleName() +
                '/' + id + '/';
    }

    public void deleteAllOwnedBy(ReferenceType referenceType, Integer referenceId) {
        fileRepository.getFileResourcesIds(referenceType.name(), referenceId).forEach(
                fileRepository::disable
        );
    }

    public void validateFileOwner(Integer ownerId, Integer fileId) {
        File file = getFile(fileId);

        if (! file.getLastModifiedBy().equals(ownerId)) {
            throw FileExceptions.fileIsNotOwnBy(fileId, ownerId);
        }
    }

}
