/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siryus.swisscon.api.tasks.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name ="comment")
public class CommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @CreatedBy
    @Column(name = "created_by")
    private Integer createdBy;

    @CreatedDate
    @Column(name = "created")
    private LocalDateTime createdDate;

    @LastModifiedBy
    @Column(name = "last_modified_by")
    private Integer lastModifiedBy;

    @LastModifiedDate
    @Column(name = "last_modified")
    private LocalDateTime lastModifiedDate;

    @Column(name = "disabled")
    private LocalDateTime disabled;

    @Column(name = "sub_task_id")
    private Integer subTaskId;
    
    @Column(name = "text")
    private String text;
    
    @Column(name = "attachment_id")
    private Integer attachmentId;    
}
