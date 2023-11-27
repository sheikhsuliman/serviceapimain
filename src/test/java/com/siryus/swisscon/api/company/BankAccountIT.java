package com.siryus.swisscon.api.company;

import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.FailFastExtension;
import com.siryus.swisscon.api.base.TestAssert;
import com.siryus.swisscon.api.base.TestBuilder;
import com.siryus.swisscon.api.company.bankaccount.BankAccountDTO;
import com.siryus.swisscon.api.util.error.TestErrorResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;

import java.util.List;

import static com.siryus.swisscon.api.base.TestBuilder.testSignupDTO;
import static com.siryus.swisscon.api.base.TestHelper.COMPANY_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_EMAIL;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_FIRST_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_LAST_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(FailFastExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BankAccountIT extends AbstractMvcTestBase {

    private static Integer companyId;
    private static Integer bankAccountOneId;
    private static Integer bankAccountTwoId;
    private static RequestSpecification asCompanyOwner;

    @BeforeAll()
    public void init() {
        companyId = testHelper
                .signUp(testSignupDTO(COMPANY_NAME, PROJECT_OWNER_FIRST_NAME, PROJECT_OWNER_LAST_NAME))
                .getCompanyId();
        asCompanyOwner = testHelper.login(PROJECT_OWNER_EMAIL);
    }

    @Test
    @Order(0)
    public void Given_companyWithNoBankAccounts_When_getBankAccounts_Then_returnEmptyList() {
        List<BankAccountDTO> bankAccounts = testHelper.getBankAccounts(asCompanyOwner, companyId);
        assertTrue(bankAccounts.isEmpty());
    }

    @Test
    @Order(1)
    public void Given_invalidIBAN_When_addBankAccount_Then_Throw() {
        testHelper.addBankAccount(asCompanyOwner, companyId,
                TestBuilder.testBankAccountDTO(dto -> dto.toBuilder().iban(dto.getIban() + "x").build()),
                r -> {
                    TestErrorResponse errorResponse = r.assertThat().statusCode(HttpStatus.BAD_REQUEST.value())
                            .extract().as(TestErrorResponse.class);
                    assertEquals(CompanyExceptions.IBAN_NUMBER_NOT_VALID.getErrorCode(), errorResponse.getErrorCode());
                    return null;
                });
    }

    @Test
    @Order(2)
    public void Given_invalidCurrency_When_addBankAccount_Then_Throw() {
        testHelper.addBankAccount(asCompanyOwner, companyId,
                TestBuilder.testBankAccountDTO(dto -> dto.toBuilder().currencyId("NOT_EXISTING_CURRENCY").build()),
                r -> {
                    r.assertThat().statusCode(HttpStatus.BAD_REQUEST.value());
                    return null;
                });
    }

    @Test
    @Order(3)
    public void Given_validIBANWithSpacesAndLowerCase_When_addBankAccount_Then_Success() {
        BankAccountDTO bankAccountDTOReq = TestBuilder.testBankAccountDTO(dto -> {
            String ibanWithSpaces = dto.getIban()
                    .toLowerCase()
                    .replaceAll("..", "$0 ");
            return dto.toBuilder().iban(ibanWithSpaces).build();
        });

        BankAccountDTO bankAccountDTO = testHelper.addBankAccount(asCompanyOwner, companyId, bankAccountDTOReq);
        assertNotNull(bankAccountDTO.getId());
        bankAccountOneId = bankAccountDTO.getId();


        TestAssert.assertBankAccountDTOequals(TestBuilder.testBankAccountDTO(), bankAccountDTO);

        // add another bank account
        bankAccountTwoId = testHelper
                .addBankAccount(asCompanyOwner, companyId, TestBuilder.testBankAccountDTO()).getId();

    }

    @Test
    @Order(4)
    public void Given_TwoStoredBankAccounts_When_getBankAccounts_Then_returnListWithTwoEntries() {
        List<BankAccountDTO> bankAccounts = testHelper.getBankAccounts(asCompanyOwner, companyId);
        assertEquals(2, bankAccounts.size());
    }

    @Test
    @Order(5)
    public void Given_modifiedBankAccountWithWrongIban_When_editBankAccount_Then_Throw() {
        testHelper.addBankAccount(asCompanyOwner, companyId,
                TestBuilder.testBankAccountDTO(dto -> dto.toBuilder().iban("DE89370400440532013000_x").id(bankAccountOneId).build()),
                r -> {
                    TestErrorResponse errorResponse = r.assertThat().statusCode(HttpStatus.BAD_REQUEST.value())
                            .extract().as(TestErrorResponse.class);
                    assertEquals(CompanyExceptions.IBAN_NUMBER_NOT_VALID.getErrorCode(), errorResponse.getErrorCode());
                    return null;
                });
    }

    @Test
    @Order(6)
    public void Given_modifiedBankAccount_When_editBankAccount_Then_Success() {
        BankAccountDTO bankAccountDTOReq = TestBuilder.testBankAccountDTO(dto -> dto.toBuilder()
                .bankName("edited bank name")
                .bic("edited bic")
                .beneficiaryName("edited beneficiary")
                .currencyId("EUR")
                .iban("DE89370400440532013000")
                .id(bankAccountOneId).build());

        BankAccountDTO bankAccountDTO = testHelper.editBankAccount(asCompanyOwner, companyId, bankAccountDTOReq);
        assertEquals(bankAccountOneId, bankAccountDTO.getId());
        TestAssert.assertBankAccountDTOequals(bankAccountDTOReq, bankAccountDTO);
    }

    @Test
    @Order(7)
    public void Given_inExistingBankAccountId_When_deleteBankAccount_Then_Throw() {
        testHelper.deleteBankAccount(asCompanyOwner, companyId, 325,
                r -> r.assertThat().statusCode(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    @Order(8)
    public void Given_bankAccountTwoId_When_deleteBankAccount_Then_Success() {
        testHelper.deleteBankAccount(asCompanyOwner, companyId, bankAccountTwoId);
    }

    @Test
    @Order(9)
    public void Given_OneBankAccountRemoved_When_getBankAccount_Then_returnListWithOneEntry() {
        List<BankAccountDTO> bankAccounts = testHelper.getBankAccounts(asCompanyOwner, companyId);
        assertEquals(1, bankAccounts.size());
        assertEquals(bankAccountOneId, bankAccounts.get(0).getId());
    }

}
