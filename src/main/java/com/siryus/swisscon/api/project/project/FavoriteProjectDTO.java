/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siryus.swisscon.api.project.project;

import com.siryus.swisscon.api.file.file.FileDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the information needed when displaying favourite projects
 * 
 * It is used in these calls:
 * - /dashboard/init
 * 
 * @author hng
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteProjectDTO {
    private Integer id;
    private String name;
    private String city;
    private FileDTO imageUrl;
    
    /**
     * Builds a FavouriteProjectsDTO object by extracting some fields from Project
     * 
     * Note: the image size which is selected for 'imageUrl' is always large because 
     * resizing will be done by frontend.
     * 
     * @param project the project to convert from
     * @return 
     */
    public static FavoriteProjectDTO from(Project project) {        
        return FavoriteProjectDTO.builder()
                                    .id(project.getId())
                                    .name(project.getName())
                                    .city(project.getCity())
                                    .imageUrl(FileDTO.fromFile(project.getDefaultImage()))
                                    .build();
    }
}
