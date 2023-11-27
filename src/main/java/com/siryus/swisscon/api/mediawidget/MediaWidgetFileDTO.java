package com.siryus.swisscon.api.mediawidget;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.entitytree.EntityTreeNodeDTO;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MediaWidgetFileDTO {
    private final Integer    id;
    private final Integer parentNodeId;

    private final ReferenceType referenceType;
    private final Integer          referenceId;

    private final boolean   leaf;
    private final boolean   empty;

    private final LocalDateTime createdDate;
    private final LocalDateTime lastModifiedDate;

    private final CreatedBy createdBy;

    private final Integer fileId;

    private final String filename;

    private final String url;

    private final String urlMedium;

    private final String urlSmall;

    private final String mimeType;

    private final Long length;
    
    private final Boolean isSystemFile;

    @JsonCreator
    public MediaWidgetFileDTO(
            @JsonProperty("id") Integer id,
            @JsonProperty("parentNodeId") Integer parentNodeId,
            @JsonProperty("referenceType") ReferenceType referenceType,
            @JsonProperty("referenceId") Integer referenceId,
            @JsonProperty("leaf") boolean leaf,
            @JsonProperty("empty") boolean empty,
            @JsonProperty("createdDate") LocalDateTime createdDate,
            @JsonProperty("lastModifiedDate") LocalDateTime lastModifiedDate,
            @JsonProperty("createdBy") CreatedBy createdBy,
            @JsonProperty("fileId") Integer fileId,
            @JsonProperty("filename") String filename,
            @JsonProperty("url") String url,
            @JsonProperty("urlMedium") String urlMedium,
            @JsonProperty("urlSmall") String urlSmall,
            @JsonProperty("mimeType") String mimeType,
            @JsonProperty("length") Long length,
            @JsonProperty("isSystemFile") Boolean isSystemFile
    ) {
        this.id = id;
        this.parentNodeId = parentNodeId;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.leaf = leaf;
        this.empty = empty;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
        this.createdBy = createdBy;
        this.fileId = fileId;
        this.filename = filename;
        this.url = url;
        this.urlMedium = urlMedium;
        this.urlSmall = urlSmall;
        this.mimeType = mimeType;
        this.length = length;
        this.isSystemFile = isSystemFile;
    }

    MediaWidgetFileDTO(
            EntityTreeNodeDTO node,
            File file,
            CreatedBy createdBy
    ) {
        this(
                node.getId(),
                node.getParentNodeId(),
                node.getOwnerReferenceType(),
                node.getOwnerReferenceId(),
                node.isLeaf(),
                node.isEmpty(),
                file.getCreatedDate(),
                file.getLastModifiedDate(),
                createdBy,
                file.getId(),
                file.getFilename(),
                file.getUrl(),
                file.getUrlMedium(),
                file.getUrlSmall(),
                file.getMimeType(),
                file.getLength(),
                file.getIsSystemFile()
        );
    }

    public static MediaWidgetFileDTO from(File file,
                                          User user) {
        return new MediaWidgetFileDTO(
                0,
                0,
                ReferenceType.valueOf(file.getReferenceType()),
                file.getReferenceId(),
                true,
                true,
                file.getCreatedDate(),
                file.getLastModifiedDate(),
                new CreatedBy(user.getId(), user.getName(), user.getIdCardUrl()),
                file.getId(),
                file.getFilename(),
                file.getUrl(),
                file.getUrlMedium(),
                file.getUrlSmall(),
                file.getMimeType(),
                file.getLength(),
                file.getIsSystemFile()
        );
    }

    public Integer getId() {
        return id;
    }

    public Integer getParentNodeId() {
        return parentNodeId;
    }

    public ReferenceType getReferenceType() {
        return referenceType;
    }

    public Integer getReferenceId() {
        return referenceId;
    }

    public boolean isEmpty() {
        return empty;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public Integer getFileId() {
        return fileId;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public CreatedBy getCreatedBy() {
        return createdBy;
    }

    public String getFilename() {
        return filename;
    }

    public String getUrl() {
        return url;
    }

    public String getUrlMedium() {
        return urlMedium;
    }

    public String getUrlSmall() {
        return urlSmall;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Long getLength() {
        return length;
    }
    
    public Boolean getIsSystemFile() {
        return isSystemFile;
    }    

    static class CreatedBy {
        private final Integer id;
        private final String name;
        private final String imageUrl;



        CreatedBy(Integer id) {
            this(id, null, null);
        }

        @JsonCreator
        CreatedBy(
                @JsonProperty("id") Integer id,
                @JsonProperty("name") String name,
                @JsonProperty("imageUrl") String imageUrl
        ) {
            this.id = id;
            this.name = name;
            this.imageUrl = imageUrl;
        }

        public Integer getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getImageUrl() {
            return imageUrl;
        }
    }
}
