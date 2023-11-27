package com.siryus.swisscon.api.file.file;

import java.io.File;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;


@Getter
@Setter
@Builder
public class FileData {

    private long contentLength;

    private String contentType;

    private File in;

    private String path;

    private File tmpFile;

    private String filename;

    private String url;

    private String urlSmall;

    private String urlMedium;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("pathFragment", this.getPath())
                .append("contentLength", this.getContentLength())
                .append("contentType", this.getContentType())
                .toString();
    }
}