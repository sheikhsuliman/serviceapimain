package com.siryus.swisscon.api.mediawidget;

import java.util.StringJoiner;

public class MediaWidgetQueryScope {
    private final String fileContentType;
    private final Integer createdByUserId;
    private final String name;
    private final String type;

    public MediaWidgetQueryScope() {
        fileContentType = null;
        createdByUserId = null;
        name = null;
        type = null;                
    }

    public MediaWidgetQueryScope(String fileContentType, Integer createdByUserId, String name, String type) {
        this.fileContentType = fileContentType;
        this.createdByUserId = createdByUserId;
        this.name = name;
        this.type = type;
    }

    public static MediaWidgetQueryScope all() {
        return new MediaWidgetQueryScope();
    }

    String getFileContentType() {
        return fileContentType;
    }

    Integer getCreatedByUserId() {
        return createdByUserId;
    }

    String getName() {
        return name;
    }

    String getType() {
        return type;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MediaWidgetQueryScope.class.getSimpleName() + "[", "]")
                .add("fileContentType='" + fileContentType + "'")
                .add("createdByUserId=" + createdByUserId)
                .add("name='" + name + "'")
                .add("type='" + type + "'")
                .toString();
    }
}
