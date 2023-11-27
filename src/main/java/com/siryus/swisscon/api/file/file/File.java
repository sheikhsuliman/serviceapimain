package com.siryus.swisscon.api.file.file;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.general.favorite.Favoritable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "file")
public class File implements Favoritable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDateTime disabled;

    @CreatedDate
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "created", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "last_modified", nullable = false)
    private LocalDateTime lastModifiedDate;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Integer createdBy;

    @LastModifiedBy
    @Column(name = "last_modified_by")
    private Integer lastModifiedBy;

    private String filename;

    private String url;

    @Column(name = "url_medium")
    private String urlMedium;

    @Column(name = "url_small")
    private String urlSmall;

    @Column(name = "mime_type")
    private String mimeType;

    private Long length;

    @Column(name = "reference_id")
    private Integer referenceId;

    @Column(name = "reference_type")
    private String referenceType;
    
    @Column(name = "system")    
    private Boolean isSystemFile;
}
