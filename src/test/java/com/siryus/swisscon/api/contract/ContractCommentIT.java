package com.siryus.swisscon.api.contract;

import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.TestHelper.ExtendedTestProject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.siryus.swisscon.api.base.TestBuilder.testCreateContractRequest;
import static com.siryus.swisscon.api.base.TestBuilder.testCreateContractCommentRequest;
import com.siryus.swisscon.api.contract.dto.ContractCommentDTO;
        
import com.siryus.swisscon.api.contract.dto.ContractDTO;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.file.file.FileRepository;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

public class ContractCommentIT extends AbstractMvcTestBase {
    private ExtendedTestProject testProject;
    private ContractDTO testContract;

    private static final String TEST_CONTRACT_NAME = "NEW_TEST_CONTRACT";
    private static final Integer INVALID_CONTRACT_ID = -42;
    private static final Integer INVALID_COMMENT_ID = -1;
    private static final Integer INVALID_FILE_ID = -1;
    private static final String TEST_COMMENT_TEXT = "Test comment";
    
    private final FileRepository fileRepository;

    @Autowired
    public ContractCommentIT(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @BeforeAll
    void initTest() {
        testProject = testHelper.createExtendedProject();

        testHelper.assignCustomerToProject(testProject.ownerCompany.asOwner, testProject.projectId,
                testProject.ownerCompany.companyId);
        
        testContract = testHelper.createContract(
                testProject.ownerCompany.asOwner, 
                testCreateContractRequest(TEST_CONTRACT_NAME, testProject.projectId)
        );
    }
    
    @Test
    void Given_validContractId_When_emptyCommentIsAdded_Then_Fail() {
        testHelper.createContractComment(
            testProject.ownerCompany.asOwner,
            testCreateContractCommentRequest(testContract.getId(), null, null),
            r -> r.statusCode(HttpStatus.BAD_REQUEST.value())
        );
        
        testHelper.createContractComment(
            testProject.ownerCompany.asOwner,
            testCreateContractCommentRequest(testContract.getId(), "", null),
            r -> r.statusCode(HttpStatus.BAD_REQUEST.value())
        );        
    }
    
    @Test
    void Given_validContractId_When_commentWithInvalidFileIdIsAdded_Then_Fail() {
        testHelper.createContractComment(
            testProject.ownerCompany.asOwner,
            testCreateContractCommentRequest(testContract.getId(), TEST_COMMENT_TEXT, INVALID_FILE_ID),
            r -> r.statusCode(HttpStatus.NOT_FOUND.value())
        );        
    }

    @Test
    void Given_validContractId_When_validCommentIsAdded_Then_Success() {
        File uploadedFile = testHelper.fileUploadTemporary(testProject.ownerCompany.asOwner);
        
        testHelper.createContractComment(
            testProject.ownerCompany.asOwner,
            testCreateContractCommentRequest(
                testContract.getId(), 
                TEST_COMMENT_TEXT, 
                uploadedFile.getId()
            )
        );
        
        List<ContractCommentDTO> comments = Arrays.asList(testHelper.listContractComments(
            testProject.ownerCompany.asOwner,
            testContract.getId()
        ));
        
        assertFalse(comments.isEmpty());
        assertEquals(1, comments.size());
        assertEquals(TEST_COMMENT_TEXT, comments.get(0).getComment());
        assertEquals(testProject.ownerCompany.ownerId, comments.get(0).getUser().getId());
        assertNotNull(comments.get(0).getAttachment());
        assertEquals(uploadedFile.getId(), comments.get(0).getAttachment().getFileId());
    }
    
    @Test
    void Given_validContractId_When_onlyTextCommentsIsAdded_Then_Success() {
        testHelper.createContractComment(
            testProject.ownerCompany.asOwner,
            testCreateContractCommentRequest(
                testContract.getId(), 
                TEST_COMMENT_TEXT, 
                null
            )
        );
    }    
    
    @Test
    void Given_validContractId_When_onlyFileCommentsIsAdded_Then_Success() {
        testHelper.createContractComment(
            testProject.ownerCompany.asOwner,
            testCreateContractCommentRequest(
                testContract.getId(), 
                null, 
                testHelper.fileUploadTemporary(testProject.ownerCompany.asOwner).getId()
            )
        );
    }     
    
    @Test
    void Given_invalidContractId_When_validCommentIsAdded_Then_Fail() {
        testHelper.createContractComment(
            testProject.ownerCompany.asOwner,
            testCreateContractCommentRequest(
                INVALID_CONTRACT_ID, 
                TEST_COMMENT_TEXT, 
                testHelper.fileUploadTemporary(testProject.ownerCompany.asOwner).getId()
            ),
            r -> r.statusCode(HttpStatus.FORBIDDEN.value())
        );         
    }    

    @Test
    void Given_validContractId_When_thereAreNoComments_Then_emptyListIsReturned() {
        ContractDTO emptyContract = testHelper.createContract(
                testProject.ownerCompany.asOwner,
                testCreateContractRequest("EMPTY_CONTRACT", testProject.projectId)
        );
        List<ContractCommentDTO> comments = Arrays.asList(testHelper.listContractComments(
                testProject.ownerCompany.asOwner,
                emptyContract.getId()
        ));
        
        assertTrue(comments.isEmpty());
    }
    
    @Test
    void Given_invalidContractId_When_listCommentsRequested_Then_Fail() {
        testHelper.listContractComments(
            testProject.ownerCompany.asOwner,
            INVALID_CONTRACT_ID,
            r -> { r.statusCode(HttpStatus.FORBIDDEN.value()); return null; }            
        );        
    }
    
    @Test
    void Given_invalidCommentId_When_removeCommentRequested_Then_Fail() {
        testHelper.removeContractComment(
            testProject.ownerCompany.asOwner,
            INVALID_COMMENT_ID,
            r -> r.statusCode(HttpStatus.FORBIDDEN.value())
        );
    }
    
    @Test
    void Given_validCommentId_When_removeCommentRequested_Then_Success() {
        Integer fileId = testHelper.fileUploadTemporary(testProject.ownerCompany.asOwner).getId();
        
        testHelper.createContractComment(
            testProject.ownerCompany.asOwner,
            testCreateContractCommentRequest(
                testContract.getId(), 
                TEST_COMMENT_TEXT, 
                fileId
            )
        );
        
        assertTrue(fileRepository.existsById(fileId));
    
        ContractCommentDTO[] comments = testHelper.listContractComments(
            testProject.ownerCompany.asOwner,
            testContract.getId());        
        
        testHelper.removeContractComment(
            testProject.ownerCompany.asOwner,
            comments[0].getId());
        
        assertNotNull(fileRepository.findById(fileId).get().getDisabled());
    }        
}
