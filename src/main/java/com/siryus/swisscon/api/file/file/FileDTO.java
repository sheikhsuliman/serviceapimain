package com.siryus.swisscon.api.file.file;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Builder(toBuilder = true)
public class FileDTO {
    private final Integer id;
    private final String url;
    private final String urlMedium;
    private final String urlSmall;

    @JsonCreator
    public FileDTO(
            @JsonProperty("id") Integer id,
            @JsonProperty("url") String url,
            @JsonProperty("urlMedium") String urlMedium,
            @JsonProperty("urlSmall") String urlSmall
    ) {
        this.id = id;
        this.url = url;
        this.urlMedium = urlMedium;
        this.urlSmall = urlSmall;
    }

    public static FileDTO fromFile(File file) {
        if(file != null) {
            return FileDTO.builder()
                .id(file.getId())
                .url(file.getUrl())
                .urlMedium(file.getUrlMedium())
                .urlSmall(file.getUrlSmall())
            .build();
        }
        return null;
    }
}
