package com.siryus.swisscon.api.base.helpers;

import com.siryus.swisscon.api.auth.user.CompanyInviteDTO;
import com.siryus.swisscon.api.company.company.CompanyDetailsDTO;
import com.siryus.swisscon.api.company.bankaccount.BankAccountDTO;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.siryus.swisscon.api.base.AbstractMvcTestBase.endPoint;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

public class Companies {
    public void removeUserFromCompany(RequestSpecification spec, Integer companyId, Integer userId) {
        given()
                .spec(spec)
                .queryParam("user", userId)
                .post(endPoint("/companies/" + companyId + "/remove-user"))
                .then()
                .assertThat()
                .statusCode(Matchers.equalTo(HttpStatus.OK.value()));
    }

    public void removeUserFromCompany(RequestSpecification spec, Integer companyId, Integer userId, Consumer<ValidatableResponse> responseValidator) {
        responseValidator.accept(given()
                .spec(spec)
                .queryParam("user", userId)
                .post(endPoint("/companies/" + companyId + "/remove-user"))
                .then());
    }

    public CompanyDetailsDTO getDetailsTeam(RequestSpecification spec, Integer companyId) {
        return getDetailsTeam(
                spec, companyId,
                r -> r.statusCode(HttpStatus.OK.value()).extract().as(CompanyDetailsDTO.class)
        );
    }

    public CompanyDetailsDTO getDetailsTeam(RequestSpecification spec, Integer companyId, Function<ValidatableResponse, CompanyDetailsDTO> responseValidator) {
        return responseValidator.apply(given()
                .spec(spec)
                .pathParam("id", companyId)
                .get(endPoint("/companies/{id}/details-team"))
                .then());
    }


    @Deprecated //TODO remove after SI-177
    public void inviteCompanyDeprecated(RequestSpecification spec, CompanyInviteDTO companyInviteDTO, HttpStatus httpStatus) {
        given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .body(companyInviteDTO)
                .post(endPoint("/companies/invite-company"))
                .then()
                .assertThat()
                .statusCode(equalTo(httpStatus.value()));
    }

    public Integer inviteCompany(RequestSpecification spec, CompanyInviteDTO companyInviteDTO) {
        return inviteCompany(spec, companyInviteDTO, r -> r.assertThat().statusCode(HttpStatus.OK.value()).extract().as(Integer.class));
    }

    public Integer inviteCompany(RequestSpecification spec, CompanyInviteDTO companyInviteDTO, Function<ValidatableResponse, Integer> responseValidator) {
        return responseValidator.apply(given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .body(companyInviteDTO)
                .post(endPoint("/companies/invite-company-and-user"))
                .then());
    }

    public List<BankAccountDTO> getBankAccounts(RequestSpecification spec, Integer companyId) {
        return getBankAccounts(
                spec, companyId,
                r -> r.statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract()
                        .body()
                        .jsonPath()
                        .getList(".", BankAccountDTO.class)
        );
    }

    public List<BankAccountDTO> getBankAccounts(RequestSpecification spec, Integer companyId, Function<ValidatableResponse, List<BankAccountDTO>> responseValidator) {
        return responseValidator.apply(given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .pathParam("companyId", companyId)
                .get(endPoint("/companies/{companyId}/bank-accounts"))
                .then());
    }

    public BankAccountDTO addBankAccount(RequestSpecification spec, Integer companyId, BankAccountDTO bankAccountDTO) {
        return addBankAccount(
                spec, companyId, bankAccountDTO,
                r -> r.statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract().as(BankAccountDTO.class)
        );
    }

    public BankAccountDTO addBankAccount(RequestSpecification spec, Integer companyId, BankAccountDTO bankAccountDTO, Function<ValidatableResponse, BankAccountDTO> responseValidator) {
        return responseValidator.apply(given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .pathParam("companyId", companyId)
                .body(bankAccountDTO)
                .post(endPoint("/companies/{companyId}/add-bank-account"))
                .then());
    }

    public BankAccountDTO editBankAccount(RequestSpecification spec, Integer companyId, BankAccountDTO bankAccountDTO) {
        return editBankAccount(
                spec, companyId, bankAccountDTO,
                r -> r.statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract().as(BankAccountDTO.class)
        );
    }

    public BankAccountDTO editBankAccount(RequestSpecification spec, Integer companyId, BankAccountDTO bankAccountDTO, Function<ValidatableResponse, BankAccountDTO> responseValidator) {
        return responseValidator.apply(given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .pathParam("companyId", companyId)
                .body(bankAccountDTO)
                .post(endPoint("/companies/{companyId}/edit-bank-account"))
                .then());
    }

    public void deleteBankAccount(RequestSpecification spec, Integer companyId, Integer bankAccountId) {
        deleteBankAccount(
                spec, companyId, bankAccountId,
                r -> r.statusCode(Matchers.equalTo(HttpStatus.OK.value())));
    }

    public void deleteBankAccount(RequestSpecification spec, Integer companyId, Integer bankAccountId, Consumer<ValidatableResponse> responseValidator) {
        responseValidator.accept(given()
                .spec(spec)
                .pathParam("companyId", companyId)
                .pathParam("bankAccountId", bankAccountId)
                .post(endPoint("/companies/{companyId}/delete-bank-account/{bankAccountId}"))
                .then());
    }
}
