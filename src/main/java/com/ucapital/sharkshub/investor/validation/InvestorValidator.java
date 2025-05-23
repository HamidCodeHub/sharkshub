package com.ucapital.sharkshub.investor.validation;

import com.ucapital.sharkshub.investor.dto.ContactsDto;
import com.ucapital.sharkshub.investor.dto.InvDescriptionsDto;
import com.ucapital.sharkshub.investor.dto.InvestorDto;
import com.ucapital.sharkshub.investor.exception.InvestorValidationException;
import com.ucapital.sharkshub.investor.repository.InvestorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


@Component
public class InvestorValidator {

    private static final Logger logger = LoggerFactory.getLogger(InvestorValidator.class);

    private static final int NAME_MAX_LENGTH = 200;
    private static final int WEBSITE_MAX_LENGTH = 500;
    private static final int DESCRIPTION_MAX_LENGTH = 5000;

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?://)?([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}(/.*)?$");

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

    private final InvestorRepository investorRepository;

    public InvestorValidator(InvestorRepository investorRepository) {
        this.investorRepository = investorRepository;
    }


    public void validateInvestor(InvestorDto investorDto, int index, boolean checkDuplicates)
            throws InvestorValidationException {

        if (investorDto == null) {
            throw new InvestorValidationException("Investor data cannot be null");
        }

        List<InvestorValidationException.ValidationError> errors = new ArrayList<>();

        validateRequiredFields(investorDto, errors);

        validateStringLengths(investorDto, errors);

        validateEmails(investorDto, errors);

        validateWebsite(investorDto, errors);

        validateNestedObjects(investorDto, errors);

        if (!errors.isEmpty()) {
            String message = String.format("Validation failed for investor at index %d", index);
            throw new InvestorValidationException(message, index, investorDto.getName(), errors);
        }
        if (checkDuplicates && StringUtils.hasText(investorDto.getName())) {
            validateUniqueName(investorDto, index);
        }
    }

    private void validateRequiredFields(InvestorDto investorDto,
                                        List<InvestorValidationException.ValidationError> errors) {

        if (!StringUtils.hasText(investorDto.getName())) {
            errors.add(InvestorValidationException.ValidationError.builder()
                    .fieldName("name")
                    .errorMessage("Investor name is required")
                    .build());
        }

        if (!StringUtils.hasText(investorDto.getStatus())) {
            errors.add(InvestorValidationException.ValidationError.builder()
                    .fieldName("status")
                    .errorMessage("Status is required")
                    .build());
        }

        if (!StringUtils.hasText(investorDto.getType())) {
            errors.add(InvestorValidationException.ValidationError.builder()
                    .fieldName("type")
                    .errorMessage("Type is required")
                    .build());
        }
    }

    private void validateStringLengths(InvestorDto investorDto,
                                       List<InvestorValidationException.ValidationError> errors) {

        if (StringUtils.hasText(investorDto.getName()) && investorDto.getName().length() > NAME_MAX_LENGTH) {
            errors.add(InvestorValidationException.ValidationError.builder()
                    .fieldName("name")
                    .errorMessage(String.format("Name must not exceed %d characters", NAME_MAX_LENGTH))
                    .rejectedValue(investorDto.getName())
                    .build());
        }

        if (StringUtils.hasText(investorDto.getWebsite()) && investorDto.getWebsite().length() > WEBSITE_MAX_LENGTH) {
            errors.add(InvestorValidationException.ValidationError.builder()
                    .fieldName("website")
                    .errorMessage(String.format("Website URL must not exceed %d characters", WEBSITE_MAX_LENGTH))
                    .rejectedValue(investorDto.getWebsite())
                    .build());
        }

        if (investorDto.getDescriptions() != null) {
            InvDescriptionsDto desc = investorDto.getDescriptions();

            validateDescriptionLength(desc.getIt(), "descriptions.it", errors);
            validateDescriptionLength(desc.getEn(), "descriptions.en", errors);
            validateDescriptionLength(desc.getFr(), "descriptions.fr", errors);
            validateDescriptionLength(desc.getDe(), "descriptions.de", errors);
            validateDescriptionLength(desc.getEs(), "descriptions.es", errors);
            validateDescriptionLength(desc.getRu(), "descriptions.ru", errors);
            validateDescriptionLength(desc.getCh(), "descriptions.ch", errors);
        }
    }


    private void validateDescriptionLength(String description, String fieldName,
                                           List<InvestorValidationException.ValidationError> errors) {

        if (StringUtils.hasText(description) && description.length() > DESCRIPTION_MAX_LENGTH) {
            errors.add(InvestorValidationException.ValidationError.builder()
                    .fieldName(fieldName)
                    .errorMessage(String.format("Description must not exceed %d characters", DESCRIPTION_MAX_LENGTH))
                    .rejectedValue(description.substring(0, 50) + "...")
                    .build());
        }
    }


    private void validateEmails(InvestorDto investorDto,
                                List<InvestorValidationException.ValidationError> errors) {

        if (StringUtils.hasText(investorDto.getCreatorEmail())
                && !isValidEmail(investorDto.getCreatorEmail())) {
            errors.add(InvestorValidationException.ValidationError.builder()
                    .fieldName("creatorEmail")
                    .errorMessage("Invalid email format")
                    .rejectedValue(investorDto.getCreatorEmail())
                    .build());
        }

        if (StringUtils.hasText(investorDto.getAdminEmail())
                && !isValidEmail(investorDto.getAdminEmail())) {
            errors.add(InvestorValidationException.ValidationError.builder()
                    .fieldName("adminEmail")
                    .errorMessage("Invalid email format")
                    .rejectedValue(investorDto.getAdminEmail())
                    .build());
        }

        if (investorDto.getHqLocation() != null && StringUtils.hasText(investorDto.getHqLocation().getEmail())
                && !isValidEmail(investorDto.getHqLocation().getEmail())) {
            errors.add(InvestorValidationException.ValidationError.builder()
                    .fieldName("hqLocation.email")
                    .errorMessage("Invalid email format")
                    .rejectedValue(investorDto.getHqLocation().getEmail())
                    .build());
        }

        // Validate contact emails if provided
        if (investorDto.getContacts() != null) {
            int contactIndex = 0;
            for (ContactsDto contact : investorDto.getContacts()) {
                if (StringUtils.hasText(contact.getEmail()) && !isValidEmail(contact.getEmail())) {
                    errors.add(InvestorValidationException.ValidationError.builder()
                            .fieldName("contacts[" + contactIndex + "].email")
                            .errorMessage("Invalid email format")
                            .rejectedValue(contact.getEmail())
                            .build());
                }
                contactIndex++;
            }
        }
    }


    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }


    private void validateWebsite(InvestorDto investorDto,
                                 List<InvestorValidationException.ValidationError> errors) {

        if (StringUtils.hasText(investorDto.getWebsite())
                && !URL_PATTERN.matcher(investorDto.getWebsite()).matches()) {
            errors.add(InvestorValidationException.ValidationError.builder()
                    .fieldName("website")
                    .errorMessage("Invalid website URL format")
                    .rejectedValue(investorDto.getWebsite())
                    .build());
        }
    }


    private void validateUniqueName(InvestorDto investorDto, int index)
            throws InvestorValidationException {

        if (investorRepository.existsByName(investorDto.getName())) {
            throw InvestorValidationException.duplicateName(investorDto.getName(), index);
        }
    }

    private void validateNestedObjects(InvestorDto investorDto,
                                       List<InvestorValidationException.ValidationError> errors) {

        if (investorDto.getContacts() != null) {
            int contactIndex = 0;
            for (ContactsDto contact : investorDto.getContacts()) {
                if (!StringUtils.hasText(contact.getFirstName())) {
                    errors.add(InvestorValidationException.ValidationError.builder()
                            .fieldName("contacts[" + contactIndex + "].firstName")
                            .errorMessage("Contact first name is required")
                            .build());
                }

                if (!StringUtils.hasText(contact.getLastName())) {
                    errors.add(InvestorValidationException.ValidationError.builder()
                            .fieldName("contacts[" + contactIndex + "].lastName")
                            .errorMessage("Contact last name is required")
                            .build());
                }

                contactIndex++;
            }
        }
    }
}