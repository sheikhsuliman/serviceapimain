package com.siryus.swisscon.api.mediawidget;

import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
class MediaWidgetFileDAO {

    private EntityManager em;

    @PersistenceContext
    void setEm(EntityManager em) {
        this.em = em;
    }

    List<File> filterFiles(MediaWidgetQueryScope queryScope, int[] fileIDs) {
        if (fileIDs.length == 0) {
            return Collections.EMPTY_LIST;
        }

        Query q = em.createQuery("SELECT file FROM File file WHERE file.id in :fileIDs " + buildWhereClause(queryScope) )
                .setParameter("fileIDs",Arrays.stream(fileIDs).boxed().collect(Collectors.toList()));

        setParameters(q, queryScope);

        return q.getResultList();
    }

    File createFolder(ReferenceType referenceType, Integer referenceId, String name, Boolean isSystem) {
        File newFile = new File();

        newFile.setReferenceType(referenceType.name());
        newFile.setReferenceId(referenceId);
        newFile.setFilename(name);
        newFile.setMimeType(MediaConstants.FOLDER_MIME_TYPE);
        newFile.setLength(0L);
        newFile.setIsSystemFile(isSystem);

        em.persist(newFile);

        return newFile;
    }

    File updateFileName(Integer folderId, String newName) {
        File file = em.find(File.class, folderId);

        if (file == null) {
            throw MediaWidgetException.fileNotFound(folderId);
        }

        file.setFilename(newName);

        em.persist(file);

        return file;
    }

    void deleteFolder(Integer fileId) {
        File folder = em.find(File.class, fileId);
        em.remove(folder);
    }


    private String buildWhereClause(MediaWidgetQueryScope queryScope) {
        List<String> conditions = new ArrayList<>();

        conditions.add(buildMediaTypeCondition(queryScope.getFileContentType()));
        conditions.add(buildCreatedByUseIdCondition(queryScope.getCreatedByUserId()));
        conditions.add(buildNameCondition(queryScope.getName()));
        conditions.add(buildTypeCondition(queryScope.getType()));

        conditions = conditions.stream().filter(Objects::nonNull).collect(Collectors.toList());

        return conditions.isEmpty() ? "" : " AND " + String.join(" AND ", conditions);
    }

    private String buildMediaTypeCondition(String fileMediaType) {
        if (fileMediaType == null) {
            return null;
        }
        switch (MediaWidgetFileContentType.valueOf(fileMediaType)) {
            case IMAGE: return "file.mimeType LIKE 'image/%'";
            case AUDIO: return "file.mimeType LIKE 'audio/%'";
            case VIDEO: return "file.mimeType LIKE 'video/%'";
            case OTHER: return "file.mimeType NOT LIKE 'image/%' AND file.mimeType NOT LIKE 'audio/%' AND file.mimeType NOT LIKE 'video/%'";
            default: return null;
        }
    }

    private String buildCreatedByUseIdCondition(Integer createdByUserId) {
        return createdByUserId == null ? null : "file.createdBy = :createdByUserId";
    }

    private String buildNameCondition(String name) {
        return name == null ? null : "file.filename LIKE CONCAT('%', :name, '%')" ;
    }

    private String buildTypeCondition(String type) {
        if (type == null) {
            return null;
        }
        switch(MediaWidgetType.valueOf(type)) {
            case FILE: return "file.mimeType is not '" + MediaConstants.FOLDER_MIME_TYPE + "'";
            case FOLDER: return "file.mimeType is '" + MediaConstants.FOLDER_MIME_TYPE + "'";
            case ALL:
            default: return null;
        }
    }

    private void setParameters(Query query, MediaWidgetQueryScope queryScope) {
        if (queryScope.getCreatedByUserId() != null) {
            query.setParameter("createdByUserId", queryScope.getCreatedByUserId());
        }
        if (queryScope.getName() != null) {
            query.setParameter("name", queryScope.getName());
        }
    }
}
