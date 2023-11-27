package com.siryus.swisscon.api.mediawidget;

import java.util.Arrays;
import java.util.List;

public class MediaConstants {

    private static final String PLANS_FOLDER = "media.default_folder.plans";
    private static final String PHOTOS_AND_VIDEOS_FOLDER = "media.default_folder.photos_videos";
    private static final String VOICE_MESSAGES_FOLDER = "media.default_folder.voice_messages";
    
    public static final String DESCRIPTION_FOLDER = "description";
    public static final String COMMENTS_FOLDER = "comments";
    public static final String START_TASK_FOLDER = "start-task";
    public static final String COMPLETE_TASK_FOLDER = "complete-task";
    public static final String REJECT_TASK_FOLDER = "reject-task";
    public static final String SYSTEM_FOLDER = "system";    

    public static final List<String> DEFAULT_MEDIA_FOLDER_KEYS = Arrays.asList(PLANS_FOLDER,
            PHOTOS_AND_VIDEOS_FOLDER,
            VOICE_MESSAGES_FOLDER);

    public static final String FOLDER_MIME_TYPE = "application/x-folder";
}
