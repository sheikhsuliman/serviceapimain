package com.siryus.swisscon.api.mediawidget;

import com.siryus.swisscon.api.util.TranslationUtil;
import com.siryus.swisscon.api.auth.user.UserService;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.file.file.FileService;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.entitytree.EntityTreeNodeDTO;
import com.siryus.swisscon.api.util.entitytree.EntityTreeService;
import com.siryus.swisscon.api.util.entitytree.EntityTreeServiceFactory;
import com.siryus.swisscon.api.util.security.SecurityHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SimpleMediaWidgetServiceTest {

    public static final int FILE_ENTITY_100 = 100;
    public static final int FILE_ENTITY_101 = 101;
    private final MediaWidgetFileDAO dao = mock(MediaWidgetFileDAO.class);
    private final UserService userService = mock(UserService.class);
    private final EntityTreeServiceFactory treeServiceFactory = mock(EntityTreeServiceFactory.class);
    private final EntityTreeService treeService = mock(EntityTreeService.class);
    private final FileService fileService = mock(FileService.class);
    private final SecurityHelper securityHelper = mock(SecurityHelper.class);

    @BeforeEach
    void doBeforeEach() {
        Mockito.reset( dao, userService, treeService, fileService );

        when(treeServiceFactory.createService(anyList())).thenReturn(treeService);
    }

    @Test
    void Given_thereAreSomeRootsAndCorrespondingFiles_When_listRoots_Then_returnListOfMediaWidgetFiles() {
        when(treeService.listRoots(any(ReferenceType.class), anyInt(), anyString(), anyString())).thenReturn(
                Arrays.asList(
                        new EntityTreeNodeDTO(ReferenceType.PROJECT, 1, 1, 0, true, true, MediaWidgetType.FILE.name(), FILE_ENTITY_100),
                        new EntityTreeNodeDTO(ReferenceType.PROJECT, 1, 2, 0, true, true, MediaWidgetType.FILE.name(), FILE_ENTITY_101)
                )
        );

        when(dao.filterFiles(any(MediaWidgetQueryScope.class), any(int[].class))).thenReturn(
                Arrays.asList(
                        buildFile(FILE_ENTITY_100, "pretty-picture-1.png" ),
                        buildFile(FILE_ENTITY_101, "pretty-picture-2.png" )
                )
        );

        SimpleMediaWidgetService service = new SimpleMediaWidgetService(dao, userService, treeServiceFactory, fileService, securityHelper, new TranslationUtil());
        MediaWidgetQueryScope queryScope = new MediaWidgetQueryScope(null, null, null, null);

        List<MediaWidgetFileDTO> roots = service.listRoots(ReferenceType.PROJECT, 1, queryScope);

        assertNotNull(roots);
        assertEquals(2, roots.size());

        assertEquals(1, (int)roots.get(0).getId());
        assertEquals(FILE_ENTITY_100, (int)roots.get(0).getFileId());

        assertEquals(2, (int)roots.get(1).getId());
        assertEquals(FILE_ENTITY_101, (int)roots.get(1).getFileId());

        ArgumentCaptor<MediaWidgetQueryScope> queryScopeArgumentCaptor = ArgumentCaptor.forClass(MediaWidgetQueryScope.class);
        ArgumentCaptor<int[]> fileIdsCaptor = ArgumentCaptor.forClass(int[].class);

        verify(dao).filterFiles(queryScopeArgumentCaptor.capture(), fileIdsCaptor.capture());

        assertEquals(queryScope, queryScopeArgumentCaptor.getValue());

        int[] fileIDs = fileIdsCaptor.getValue();
        assertEquals(2, fileIDs.length);

        assertEquals(FILE_ENTITY_100, fileIDs[0]);
        assertEquals(FILE_ENTITY_101, fileIDs[1]);
    }

    private File buildFile(Integer id, String fileName) {
        File result = new File();

        result.setId(id);
        result.setReferenceType(ReferenceType.PROJECT.name());
        result.setReferenceId(1);
        result.setCreatedDate(LocalDateTime.now());
        result.setLastModifiedDate(LocalDateTime.now());
        result.setCreatedBy(1);
        result.setFilename(fileName);
        result.setUrl("https://s3.com/files/" + fileName);
        result.setUrlSmall("https://s3.com/files/small/" + fileName );
        result.setUrlMedium("https://s3.com/files/medium/" + fileName );
        result.setMimeType("Mime/Type");
        result.setLength(1000L);
        result.setIsSystemFile(Boolean.FALSE);
        
        return result;

    }
}
