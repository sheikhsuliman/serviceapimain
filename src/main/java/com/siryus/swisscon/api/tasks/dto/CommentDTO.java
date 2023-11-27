/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siryus.swisscon.api.tasks.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.auth.user.AuthorDTO;
import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.mediawidget.MediaWidgetFileDTO;
import com.siryus.swisscon.api.tasks.entity.CommentEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
@Getter
public class CommentDTO {
    private final Integer id;

    private final AuthorDTO user;

    private final LocalDateTime date;

    private final LocalDateTime disabled;

    private final MediaWidgetFileDTO attachment;
    
    private final String comment;

    @JsonCreator
    CommentDTO(
            @JsonProperty("id") Integer id,
            @JsonProperty("user") AuthorDTO user,
            @JsonProperty("date") LocalDateTime date,
            @JsonProperty("disabled") LocalDateTime disabled,
            @JsonProperty("attachment") MediaWidgetFileDTO attachment,
            @JsonProperty("comment") String comment
    ) {
        this.id = id;
        this.user = user;
        this.date = date;
        this.disabled = disabled;
        this.attachment = attachment;
        this.comment = comment;
    }

    public static CommentDTO from(CommentEntity ce, User user, MediaWidgetFileDTO attachment) {
        return new CommentDTO(
                ce.getId(),
                AuthorDTO.from(user),
                ce.getCreatedDate(),
                ce.getDisabled(),
                attachment,
                ce.getText()
        );
    }

    public static CommentDTO from(CommentEntity ce, TeamUserDTO user, MediaWidgetFileDTO attachment) {
        return new CommentDTO(
                ce.getId(),
                AuthorDTO.from(user),
                ce.getCreatedDate(),
                ce.getDisabled(),
                attachment,
                ce.getText()
        );
    }
}
