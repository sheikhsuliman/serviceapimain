package com.siryus.swisscon.api.base;

import com.google.common.collect.ImmutableSet;
import com.siryus.commons.rabbitmq.EventsManager;
import com.siryus.commons.rabbitmq.RabbitMessage;
import com.siryus.swisscon.api.file.file.FileService;
import com.siryus.swisscon.api.util.TranslationUtil;
import com.siryus.swisscon.api.auth.role.Role;
import com.siryus.swisscon.api.auth.signup.SignupDTO;
import com.siryus.swisscon.api.auth.signup.SignupResponseDTO;
import com.siryus.swisscon.api.auth.user.TeamUserAddDTO;
import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.catalog.CatalogVariationTestRepository;
import com.siryus.swisscon.api.catalog.Constants;
import com.siryus.swisscon.api.catalog.csvreader.CatalogItemLine;
import com.siryus.swisscon.api.catalog.csvreader.CatalogItemLineWriter;
import com.siryus.swisscon.api.catalog.dto.CatalogImportReportDTO;
import com.siryus.swisscon.api.catalog.repos.CatalogNodeEntity;
import com.siryus.swisscon.api.catalog.repos.CatalogNodeRepository;
import com.siryus.swisscon.api.catalog.repos.CatalogVariationEntity;
import com.siryus.swisscon.api.company.bankaccount.BankAccountDTO;
import com.siryus.swisscon.api.company.company.CompanyDetailsDTO;
import com.siryus.swisscon.api.contract.dto.ContractAddTasksRequest;
import com.siryus.swisscon.api.contract.dto.ContractEventLogDTO;
import com.siryus.swisscon.api.contract.dto.ContractState;
import com.siryus.swisscon.api.contract.dto.ContractTaskDTO;
import com.siryus.swisscon.api.contract.dto.ContractUpdateTaskRequest;
import com.siryus.swisscon.api.exceptions.LocalizedResponseStatusException;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.location.location.Location;
import com.siryus.swisscon.api.location.location.LocationDetailsDTO;
import com.siryus.swisscon.api.location.location.LocationUpdateDTO;
import com.siryus.swisscon.api.mediawidget.MediaConstants;
import com.siryus.swisscon.api.mediawidget.MediaWidgetFileDTO;
import com.siryus.swisscon.api.tasks.dto.TaskStatus;
import com.siryus.swisscon.api.tasks.entity.MainTaskEntity;
import com.siryus.swisscon.api.tasks.repos.MainTaskRepository;
import com.siryus.swisscon.api.taskworklog.dto.WorkLogEventType;
import com.siryus.swisscon.api.taskworklog.entity.TaskWorklogEntity;
import com.siryus.swisscon.api.taskworklog.repos.TaskWorkLogRepository;
import com.siryus.swisscon.api.util.EmailPhoneUtils;
import com.siryus.swisscon.api.util.error.TestErrorResponse;
import com.siryus.swisscon.soa.notification.contract.NotificationEvent;
import com.siryus.swisscon.soa.notification.contract.NotificationType;
import io.restassured.http.Headers;
import io.restassured.response.ValidatableResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Component
@SuppressWarnings("Weaker Access")
public class TestAssert {

    private static final String EXPECTED_CSV_CONTENT_TYPE = "application/csv;charset=ISO-8859-1";
    private static final String CONTENT_TYPE = "Content-Type";
    private final CatalogNodeRepository nodeRepository;
    private final CatalogVariationTestRepository variationTestRepository;
    private final MainTaskRepository mainTaskRepository;
    private final TaskWorkLogRepository taskWorkLogRepository;
    private final TranslationUtil translationUtil;
    private final FileService fileService;
    private final MockEventsManager eventsManager;

    @Autowired
    public TestAssert(CatalogNodeRepository nodeRepository, CatalogVariationTestRepository variationTestRepository, MainTaskRepository mainTaskRepository, TaskWorkLogRepository taskWorkLogRepository, FileService fileService, EventsManager eventsManager) {
        this.nodeRepository = nodeRepository;
        this.variationTestRepository = variationTestRepository;
        this.mainTaskRepository = mainTaskRepository;
        this.taskWorkLogRepository = taskWorkLogRepository;
        this.fileService = fileService;
        this.eventsManager = (MockEventsManager) eventsManager;
        this.translationUtil = new TranslationUtil();
    }

    public static void assertTeamUserDTOequals(TeamUserDTO expected, TeamUserDTO actual) {
        Integer pictureIdExpected = expected.getPicture() != null ? expected.getPicture().getId() : null;
        Integer pictureIdActual = actual.getPicture() != null ? actual.getPicture().getId() : null;

        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getRoleId(), actual.getRoleId());

        expected.getRoleIds().forEach(r-> assertThat(actual.getRoleIds(), hasItem(r)));
        assertEquals(expected.getEmail(), actual.getEmail());
        assertEquals(expected.getMobile(), actual.getMobile());
        //TODO doesn't work atm
        //assertEquals(expected.getIsAdmin(), actual.getIsAdmin());
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getCompanyId(), actual.getCompanyId());
        assertEquals(expected.getIsUnverified(), actual.getIsUnverified());
        assertEquals(pictureIdExpected, pictureIdActual);
    }

    public static void assertTeamUserDTOequals(TeamUserAddDTO expected, Role expectedRole, TeamUserDTO actual) {
        if (expected.getCountryCode() != null) {
            String expectedMobile = EmailPhoneUtils
                    .toFullPhoneNumber(expected.getCountryCode(), expected.getEmailOrPhone());
            assertEquals(expectedMobile, actual.getMobile());
        } else {
            assertEquals(expected.getEmailOrPhone(), actual.getEmail());
        }

        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertThat(actual.getRoleIds(), hasItem(expectedRole.getId()));
        assertEquals(expectedRole.isAdmin(), actual.getIsAdmin());
        assertTrue(actual.getIsUnverified());
    }

    public static void assertTeamUserDTOequals(User expected, Role expectedRole, TeamUserDTO dto) {
        String expectedMobile = EmailPhoneUtils
                .toFullPhoneNumber(expected.getMobileCountryCode(),
                        expected.getMobile());

        assertEquals(expected.getId(), dto.getId());
        assertEquals(expected.getGivenName(), dto.getFirstName());
        assertEquals(expected.getSurName(), dto.getLastName());
        assertEquals(expected.getEmail(), dto.getEmail());
        assertEquals(expectedMobile, dto.getMobile());
        assertThat(dto.getRoleIds(), hasItem(expectedRole.getId()));
        assertEquals(Role.isAdmin(expectedRole), dto.getIsAdmin());

        if (expected.getPicture() != null) {
            assertNotNull(dto.getPicture());
            assertEquals(expected.getPicture().getId(), dto.getPicture().getId());
            assertEquals(expected.getPicture().getUrl(), dto.getPicture().getUrl());
            assertEquals(expected.getPicture().getUrlSmall(), dto.getPicture().getUrlSmall());
            assertEquals(expected.getPicture().getUrlMedium(), dto.getPicture().getUrlMedium());
        }
    }

    public static void assertAttachment(File file, ReferenceType referenceType, Integer referenceId) {
        assertTrue(file.getIsSystemFile());
        assertEquals(referenceType.toString(), file.getReferenceType());
        assertEquals(referenceId, file.getReferenceId());
    }

    public static void assertBankAccountDTOequals(BankAccountDTO expected, BankAccountDTO actual) {
        assertEquals(expected.getBankName(), actual.getBankName());
        assertEquals(expected.getBic(), actual.getBic());
        assertEquals(expected.getCurrencyId(), actual.getCurrencyId());
        assertEquals(expected.getBeneficiaryName(), actual.getBeneficiaryName());
        assertEquals(expected.getIban(), actual.getIban());
    }

    public static void assertCatalogImportReportDTO(CatalogImportReportDTO actual, Integer lines, Integer editedNodes, Integer editedLeafNodes, Integer addedNodes, Integer addedLeafNodes) {
        assertNotNull(actual);
        String expectedReport = String
                .join(", ", Arrays.asList(lines.toString(),
                        editedNodes.toString(),
                        editedLeafNodes.toString(),
                        addedNodes.toString(),
                        addedLeafNodes.toString()));
        String actualReport = String
                .join(", ", Arrays.asList(actual.getLines().toString(),
                        actual.getEditedNodes().toString(),
                        actual.getEditedLeafNodes().toString(),
                        actual.getAddedNodes().toString(),
                        actual.getAddedLeafNodes().toString()));

        assertEquals(expectedReport, actualReport);
    }

    public static void assertDetailsTeam(SignupDTO signupDTO, SignupResponseDTO signupResponseDTO, CompanyDetailsDTO detailsTeam) {
        assertEquals(signupDTO.getCompany().getName(), detailsTeam.getName());
        assertEquals(signupDTO.getCompany().getCountryId(), detailsTeam.getCountryId());
        assertTrue(detailsTeam.getTeam().stream().anyMatch(u->u.getFirstName().equals(signupDTO.getUser().getFirstName())));
        assertTrue(detailsTeam.getTeam().stream().anyMatch(u->u.getLastName().equals(signupDTO.getUser().getLastName())));
    }

    public static void assertContractEventLogDTO(Integer contractId, ContractState contractState, ContractState fromState, ContractState toState, Integer projectId, ContractEventLogDTO dto) {
        assertEquals(contractId, dto.getContractId());
        assertEquals(contractState, dto.getContractState());
        assertTrue(dto.getEvent().validFromState(fromState));
        assertEquals(toState, dto.getEvent().getToState(fromState));
        assertEquals(projectId, dto.getProjectId());
    }

    public static void templateContainsCaseInsensitive(String htmlContent, String ...strings) {
        for(String str : strings) {
            assertThat(htmlContent.toLowerCase(), containsString(str.toLowerCase()));
        }
        assertFalse(htmlContent.contains("{"));
        assertFalse(htmlContent.contains("}"));
    }

    public void assertContainsNewEmptyDefaultMediaFolders(List<MediaWidgetFileDTO> dtos, ReferenceType type, Integer referenceId) {
        MediaConstants.DEFAULT_MEDIA_FOLDER_KEYS
                .stream()
                .map(key-> translationUtil.get(key, TestHelper.TEST_LANGUAGE))
                .forEach(folderName -> {
            assertContainsMediaFolder(dtos, folderName, type, referenceId, true, true);
        });
    }

    public static void assertTeamContainsUser(List<TeamUserDTO> team, Integer userId) {
        assertTrue(team.stream().anyMatch(t -> t.getId().equals(userId)));
    }

    public static void assertAvailableUsersContainUser(List<TeamUserDTO> team, Integer userId, boolean inProject) {
        TeamUserDTO teamUserDTO = team.stream().filter(d -> d.getId().equals(userId)).findFirst().orElseThrow();
        assertEquals(inProject, teamUserDTO.getInProject());
    }

    public static void assertContainsMediaFolder(List<MediaWidgetFileDTO> dtos, String folderName, ReferenceType type, Integer id, boolean isLeaf, boolean isEmpty) {
        MediaWidgetFileDTO folder = dtos.stream()
                .filter(dto -> folderName.equals(dto.getFilename()))
                .findFirst()
                .orElseThrow();
        assertEquals(type, folder.getReferenceType());
        assertEquals(id, folder.getReferenceId());
        assertEquals(isLeaf, folder.isLeaf());
        assertEquals(isEmpty, folder.isEmpty());
        assertEquals(MediaConstants.FOLDER_MIME_TYPE, folder.getMimeType());
    }

    public void assertPersistedGlobalCatalogNode(String snp, String parentSnp, String name) {
        CatalogNodeEntity node = nodeRepository.findLatestWithSnp(snp).orElseThrow();

        assertEquals(snp, node.getSnp());
        assertEquals(parentSnp, node.getParentSnp());
        assertEquals(name, node.getName());
        assertEquals(Constants.GLOBAL_CATALOG_COMPANY_ID, node.getCompanyId());
    }

    public void assertGlobalCatalogVariation(String snp, String taskName, String variationName, Integer variationNumber, String unitSymbol) {
        assertCompanyCatalogVariation(Constants.GLOBAL_CATALOG_COMPANY_ID, snp, taskName, variationName, variationNumber, unitSymbol, true, null, null);
    }

    public void assertCompanyCatalogVariation(Integer companyId, String snp, String taskName, String variationName, Integer variationNumber, String unitSymbol, boolean isActive, String price, String checkList) {
        CatalogNodeEntity node = nodeRepository.findLatestWithSnp(snp).orElseThrow();
        CatalogVariationEntity variation = variationTestRepository
                .findLatestBySnpAndVariationNumber(snp, variationNumber, companyId)
                .orElseThrow();

        assertEquals(snp, variation.getSnp());
        assertEquals(taskName, variation.getTaskName());
        assertEquals(variationName, variation.getTaskVariation());
        assertEquals(variationNumber, variation.getVariationNumber());
        assertEquals(unitSymbol, variation.getUnit().getSymbol());
        assertEquals(node.getId(), variation.getCatalogNodeId());
        assertEquals(companyId, variation.getCompanyId());
        assertEquals(isActive, variation.isActive());
        assertEquals(checkList, variation.getCheckList());
        assertEquals(price != null ? new BigDecimal(price) : null, variation.getPrice());
    }

    public static void assertContainsVariation(String[] lines, String snpWithVariationNumber) {
        boolean variationExists = Arrays.stream(lines)
                .map(line -> line.split(String.valueOf(Constants.CSV_SEPARATOR)))
                .map(fields -> CatalogItemLine.toLine(Arrays.asList(fields)))
                .anyMatch(itemLine -> itemLine.getSnpAndVariationNumber().equals(snpWithVariationNumber));
        assertTrue(variationExists, snpWithVariationNumber + " should exist in export");
    }

    public static void assertHeaders(ValidatableResponse r, String scope) {
        Headers headers = r.assertThat()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .headers();

        assertEquals(CatalogItemLineWriter.HEADER_VALUE + scope + CatalogItemLineWriter.CSV_FILENAME_SUFFIX,
                headers.get(CatalogItemLineWriter.HEADER_KEY).getValue());
        assertEquals(EXPECTED_CSV_CONTENT_TYPE,
                headers.get(CONTENT_TYPE).getValue());
    }

    public static void assertContainsLine(List<String> fieldsList, String[] lines) {
        String[] fields = fieldsList.toArray(new String[]{});

        for (String line : lines) {
            String[] splitLine = line.split(String.valueOf(Constants.CSV_SEPARATOR));
            String expectedSnp = CatalogItemLine.toLine(fieldsList).getSnpAndVariationNumber();
            String currentSnp = CatalogItemLine.toLine(Arrays.asList(splitLine)).getSnpAndVariationNumber();
            if (expectedSnp.equals(currentSnp)) {
                assertArrayEquals(fields, splitLine);
                return;
            }
        }
        fail();
    }

    public static void assertContainsLocationWithName(List<LocationDetailsDTO> locations, String name) {
        assertTrue(locations.stream().anyMatch(l -> l.getName().equals(name)));
    }
    
    public void assertLocationUpdateIsCorrect(LocationDetailsDTO result, LocationUpdateDTO updateRequest) {
        assertEquals(updateRequest.getId(), result.getId());
        
        checkLocationMainFields(result, updateRequest);
        checkLocationUnits(result, updateRequest);
        checkLocationFiles(result, updateRequest);
    }
    
    private void checkLocationMainFields(LocationDetailsDTO result, LocationUpdateDTO updateRequest) {
        if (updateRequest.getDescription() != null) {
            assertEquals(updateRequest.getDescription(), result.getDescription());
        }
                
        if (updateRequest.getName() != null) {
            assertEquals(updateRequest.getName(), result.getName());
        }        
    }
 
    private void checkLocationUnits(LocationDetailsDTO result, LocationUpdateDTO updateRequest) {
        if (updateRequest.getHeight() != null) {
            assertEquals(updateRequest.getHeight(), result.getHeight());
        }        

        if (updateRequest.getLength() != null) {
            assertEquals(updateRequest.getLength(), result.getLength());
        }        
        
        if (updateRequest.getSurface() != null) {
            assertEquals(updateRequest.getSurface(), result.getSurface());
        }
        
        if (updateRequest.getVolume() != null) {        
            assertEquals(updateRequest.getVolume(), result.getVolume());
        }

        if (updateRequest.getWidth() != null) {
            assertEquals(updateRequest.getWidth(), result.getWidth());
        }        

        
        if (updateRequest.getSurfaceUnit() != null) {
            assertEquals(updateRequest.getSurfaceUnit(), result.getSurfaceUnit().getSymbol());
        }
        
        if (updateRequest.getUnit() != null) {
            assertEquals(updateRequest.getUnit(), result.getUnit().getSymbol());
        }
        
        if (updateRequest.getVolumeUnit() != null) {
            assertEquals(updateRequest.getVolumeUnit(), result.getVolumeUnit().getSymbol());
        }        
    }
    
    private void checkLocationFiles(LocationDetailsDTO result, LocationUpdateDTO updateRequest) {
        if (updateRequest.getImageFileId() != null) {
            assertEquals(updateRequest.getImageFileId(), result.getImage().getId());
            assertFileReference(updateRequest.getImageFileId(), result.getId(), ReferenceType.LOCATION);
        }
        
        if (updateRequest.getLayoutFileId() != null) {
            assertEquals(updateRequest.getLayoutFileId(), result.getLayout().getId());
            assertFileReference(updateRequest.getLayoutFileId(), result.getId(), ReferenceType.LOCATION);
        }
    }

    public void assertFileReference(Integer fileId, Integer referenceId, ReferenceType referenceType) {
        File file = fileService.findById(fileId);
        assertEquals(referenceId, file.getReferenceId());
        assertEquals(referenceType.name(), file.getReferenceType());
    }
    
    public static void assertLocationDTOOrder(List<LocationDetailsDTO> locations) {
        for (int i = 0; i < locations.size(); i++) {
            assertEquals(i, locations.get(i).getOrder());
        }
    }

    public static void assertLocationOrder(List<Location> locations) {
        for (int i = 0; i < locations.size(); i++) {
            assertEquals(i, locations.get(i).getOrder());
        }
    }

    public void assertContractAddTask(ContractAddTasksRequest request, Integer locationId, List<ContractTaskDTO> dtos) {
        assertEquals(request.getTaskIds().size(), dtos.size());
        dtos.forEach(dto -> {
            assertEquals(request.getContractId(), dto.getContractId());
            assertTrue(request.getTaskIds().contains(dto.getTask().getId()));
            assertAddedMainTaskToContract(dto.getTask().getId(), locationId, dto);
        });
    }

    public void assertListContractTask(Integer expectedTaskId, Integer locationId, List<ContractTaskDTO> dtos) {
        ContractTaskDTO contractTaskDTO = dtos.stream()
                .filter(dto -> dto.getTask().getId().equals(expectedTaskId))
                .findFirst().orElseThrow();
        assertAddedMainTaskToContract(expectedTaskId, locationId, contractTaskDTO);
    }

    public void assertAddedMainTaskToContract(Integer taskId, Integer locationId, ContractTaskDTO dto) {
        MainTaskEntity mainTaskEntity = mainTaskRepository.findById(taskId).orElseThrow();
        assertEquals(mainTaskEntity.getId(), dto.getTask().getId());
        assertEquals(mainTaskEntity.getDescription(), dto.getTask().getDescription());
        boolean pathContainsLocation = dto.getTask().getLocationPath().stream().anyMatch(l -> l.getId().equals(locationId));
        assertTrue(pathContainsLocation);
        assertEquals(TaskStatus.DRAFT, mainTaskEntity.getStatus());

        TaskWorklogEntity taskWorkLogEntity = taskWorkLogRepository
                .getLastMainTaskWorkLog(mainTaskEntity.getId(),
                        ImmutableSet.of(WorkLogEventType.ADDED_TO_CONTRACT.name()))
                .orElseThrow();
        assertEquals(dto.getTask().getId(), taskWorkLogEntity.getMainTask().getId());
        assertEquals(WorkLogEventType.ADDED_TO_CONTRACT, taskWorkLogEntity.getEvent());
    }

    public void assertContractUpdateTask(ContractUpdateTaskRequest updateTaskRequest, String expectedTotalPrice, ContractTaskDTO contractTaskDTO) {
        assertEquals(updateTaskRequest.getContractTaskId(), contractTaskDTO.getContractTaskId());
        assertEquals(updateTaskRequest.getAmount(), contractTaskDTO.getAmount());
        assertEquals(updateTaskRequest.getPricePerUnit(), contractTaskDTO.getPricePerUnit());
        assertEquals(updateTaskRequest.getUnitId(), contractTaskDTO.getUnit().getId());
        assertEquals(expectedTotalPrice != null ? new BigDecimal(expectedTotalPrice) : null, contractTaskDTO.getPrice());
    }

    public static void assertErrorContains(HttpStatus httpStatus, ValidatableResponse response, String ...reasonParts ) {
        TestErrorResponse error = response.assertThat().statusCode(httpStatus.value()).extract().as(TestErrorResponse.class);
        for(String reasonPart : reasonParts) {
            assertThat(error.getReason(), containsString(reasonPart));
        }
    }

    public static void assertError(HttpStatus httpStatus, String reason, ValidatableResponse response) {
        assertError(httpStatus, null, reason, response);
    }

    public static void assertError(HttpStatus httpStatus, Integer errorCode, ValidatableResponse response) {
        assertError(httpStatus, errorCode, null, response);
    }

    public static void assertError(HttpStatus httpStatus, Integer errorCode, String reason, ValidatableResponse response) {
        TestErrorResponse error = response.assertThat().statusCode(httpStatus.value()).extract().as(TestErrorResponse.class);
        assertEquals(errorCode, error.getErrorCode());
        if(errorCode != null) {
            assertEquals(errorCode, error.getErrorCode());
        }
        if(reason != null) {
            assertEquals(reason, error.getReason());
        }
    }

    public static void assertErrorFromExecution(HttpStatus httpStatus, Integer errorCode, Runnable runnable) {
        LocalizedResponseStatusException exception = assertThrows(LocalizedResponseStatusException.class, runnable::run);
        assertEquals(httpStatus, exception.getStatus());
        assertEquals(errorCode, exception.getLocalizedReason().getErrorCode());
    }

    public void assertNotificationFired(Integer companyId, Integer projectId, Integer senderId, Integer subjectId, NotificationType notificationType, Integer referenceId) {
        final RabbitMessage currentEvent = eventsManager.getCurrentEvent();
        assertNotNull(currentEvent);
        assertTrue(currentEvent instanceof NotificationEvent);
        NotificationEvent event = (NotificationEvent) currentEvent;

        assertEquals(companyId, event.getCompanyId());
        assertEquals(projectId, event.getProjectId());
        assertEquals(senderId, event.getSenderId());
        assertEquals(subjectId, event.getSubjectId());
        assertEquals(notificationType, event.getNotificationType());
        assertEquals(referenceId, event.getReferenceId());
    }

}
