package com.siryus.swisscon.api.contract.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.auth.user.AuthorDTO;
import com.siryus.swisscon.api.contract.repos.ContractCommentEntity;
import com.siryus.swisscon.api.mediawidget.MediaWidgetFileDTO;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ContractCommentDTO {
    private final Integer id; 
    
    private final LocalDateTime date;
    private final AuthorDTO user;
    private final String comment;
    private final MediaWidgetFileDTO attachment;
    
    @JsonCreator
    ContractCommentDTO(
            @JsonProperty("id") Integer id,
            @JsonProperty("user") AuthorDTO user,
            @JsonProperty("date") LocalDateTime date,
            @JsonProperty("attachment") MediaWidgetFileDTO attachment,
            @JsonProperty("comment") String comment
    ) {
        this.id = id;
        this.user = user;
        this.date = date;
        this.attachment = attachment;
        this.comment = comment;
    }    
    
    public static ContractCommentDTO from(ContractCommentEntity comment, AuthorDTO author, MediaWidgetFileDTO attachment) {
        return new ContractCommentDTO(comment.getId(), author, comment.getCreatedDate(), attachment, comment.getText());
    }           
}
