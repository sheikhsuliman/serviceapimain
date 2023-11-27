package com.siryus.swisscon.api.company.bankaccount;

import com.siryus.swisscon.api.company.CompanyExceptions;
import com.siryus.swisscon.api.util.CustomStringUtils;
import com.siryus.swisscon.api.util.validator.DTOValidator;
import org.apache.commons.validator.routines.IBANValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service("bankAccountService")
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;

    @Autowired
    public BankAccountService(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }

    public List<BankAccountDTO> getBankAccounts(Integer companyId) {
        return bankAccountRepository
                .findAllByCompanyId(companyId)
                .stream()
                .map(BankAccountDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional()
    public BankAccountDTO addBankAccount(Integer companyId, BankAccountDTO bankAccountDTO) {
        DTOValidator.validateAndThrow(bankAccountDTO);
        BankAccountDTO validBankAccountDTO = bankAccountDTO.toBuilder()
                .iban(validateAndNormalize(bankAccountDTO.getIban())).build();

        validateNewBankAccountCannotHaveId(validBankAccountDTO.getId());

        return saveBankAccount(companyId, validBankAccountDTO);
    }

    @Transactional()
    public BankAccountDTO editBankAccount(Integer companyId, BankAccountDTO bankAccountDTO) {
        DTOValidator.validateAndThrow(bankAccountDTO);
        BankAccountDTO validBankAccountDTO = bankAccountDTO.toBuilder()
                .iban(validateAndNormalize(bankAccountDTO.getIban())).build();

        validateBankAccountBelongsToCompany(companyId, validBankAccountDTO.getId());

        return saveBankAccount(companyId, validBankAccountDTO);
    }

    @Transactional()
    public void deleteBankAccount(Integer companyId, Integer bankAccountId) {
        BankAccountEntity bankAccountEntity = bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> CompanyExceptions.bankAccountNotFound(bankAccountId));

        validateBankAccountBelongsToCompany(companyId, bankAccountEntity.getId());

        bankAccountRepository.deleteById(bankAccountEntity.getId());
    }

    private BankAccountDTO saveBankAccount(Integer companyId, BankAccountDTO bankAccountDTO) {
        BankAccountEntity unsavedBankAccountEntity = BankAccountEntity.from(companyId, bankAccountDTO);
        BankAccountEntity savedBankAccountEntity = bankAccountRepository.save(unsavedBankAccountEntity);
        return BankAccountDTO.from(savedBankAccountEntity);
    }

    private void validateNewBankAccountCannotHaveId(Integer bankAccountId) {
        if (bankAccountId != null) {
            throw CompanyExceptions.newBankAccountCannotHaveId(bankAccountId);
        }
    }

    private void validateBankAccountBelongsToCompany(Integer companyId, Integer bankAccountId) {
        BankAccountEntity bankAccountEntity = bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> CompanyExceptions.bankAccountNotFound(bankAccountId));
        if (!bankAccountEntity.getCompanyId().equals(companyId)) {
            throw CompanyExceptions.bankAccountDoesNotBelongToCompany(companyId, bankAccountId);
        }
    }

    private String validateAndNormalize(String iban) {
        String normalizedIBAN = CustomStringUtils.removeAllSpaces(iban).toUpperCase();

        if (!IBANValidator.getInstance().isValid(normalizedIBAN)) {
            throw CompanyExceptions.ibanNotValid(iban);
        }
        return normalizedIBAN;
    }

}
