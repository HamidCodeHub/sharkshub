package com.ucapital.sharkshub;

import com.ucapital.sharkshub.investor.dto.InvestorDto;
import com.ucapital.sharkshub.investor.exception.InvestorValidationException;
import com.ucapital.sharkshub.investor.repository.InvestorRepository;
import com.ucapital.sharkshub.investor.validation.InvestorValidator;
import com.ucapital.sharkshub.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvestorValidator Tests")
class InvestorValidatorTest {

    @Mock
    private InvestorRepository investorRepository;

    @InjectMocks
    private InvestorValidator investorValidator;

    private InvestorDto validInvestorDto;

    @BeforeEach
    void setUp() {
        validInvestorDto = TestDataBuilder.createValidInvestorDto();
    }

    @Nested
    @DisplayName("Basic Validation Tests")
    class BasicValidationTests {

        @Test
        @DisplayName("Should pass validation for valid investor")
        void validateInvestor_WithValidData_ShouldPass() {
            // Given
            when(investorRepository.existsByName(anyString())).thenReturn(false);

            // When & Then
            assertThatCode(() -> investorValidator.validateInvestor(validInvestorDto, 0, true))
                    .doesNotThrowAnyException();

            verify(investorRepository).existsByName(validInvestorDto.getName());
        }

        @Test
        @DisplayName("Should throw exception when investor DTO is null")
        void validateInvestor_WithNullDto_ShouldThrowException() {
            // When & Then
            assertThatThrownBy(() -> investorValidator.validateInvestor(null, 0, true))
                    .isInstanceOf(InvestorValidationException.class);

            verifyNoInteractions(investorRepository);
        }

        @Test
        @DisplayName("Should pass validation when bulk mode is false")
        void validateInvestor_WithBulkModeFalse_ShouldSkipUniquenessCheck() {
            // When & Then
            assertThatCode(() -> investorValidator.validateInvestor(validInvestorDto, 0, false))
                    .doesNotThrowAnyException();

            // Should not check uniqueness when not in bulk mode
            verifyNoInteractions(investorRepository);
        }
    }

    @Nested
    @DisplayName("Name Validation Tests")
    class NameValidationTests {

        @Test
        @DisplayName("Should throw exception when name is null")
        void validateInvestor_WithNullName_ShouldThrowException() {
            // Given
            validInvestorDto.setName(null);

            // When & Then
            assertThatThrownBy(() -> investorValidator.validateInvestor(validInvestorDto, 0, true))
                    .isInstanceOf(InvestorValidationException.class);

            verifyNoInteractions(investorRepository);
        }

        @Test
        @DisplayName("Should throw exception when name is empty")
        void validateInvestor_WithEmptyName_ShouldThrowException() {
            // Given
            validInvestorDto.setName("");

            // When & Then
            assertThatThrownBy(() -> investorValidator.validateInvestor(validInvestorDto, 0, true))
                    .isInstanceOf(InvestorValidationException.class);

            verifyNoInteractions(investorRepository);
        }

        @Test
        @DisplayName("Should throw exception when name is only whitespace")
        void validateInvestor_WithWhitespaceName_ShouldThrowException() {
            // Given
            validInvestorDto.setName("   ");

            // When & Then
            assertThatThrownBy(() -> investorValidator.validateInvestor(validInvestorDto, 0, true))
                    .isInstanceOf(InvestorValidationException.class);

            verifyNoInteractions(investorRepository);
        }

        @Test
        @DisplayName("Should accept valid name")
        void validateInvestor_WithValidName_ShouldPass() {
            // Given
            validInvestorDto.setName("Valid Investor Name");
            when(investorRepository.existsByName(anyString())).thenReturn(false);

            // When & Then
            assertThatCode(() -> investorValidator.validateInvestor(validInvestorDto, 0, true))
                    .doesNotThrowAnyException();

            verify(investorRepository).existsByName(validInvestorDto.getName());
        }

        @Test
        @DisplayName("Should accept name with special characters")
        void validateInvestor_WithSpecialCharacters_ShouldPass() {
            // Given
            validInvestorDto.setName("Test & Co. - Venture Partners, LLC");
            when(investorRepository.existsByName(anyString())).thenReturn(false);

            // When & Then
            assertThatCode(() -> investorValidator.validateInvestor(validInvestorDto, 0, true))
                    .doesNotThrowAnyException();

            verify(investorRepository).existsByName(validInvestorDto.getName());
        }
    }

    @Nested
    @DisplayName("Uniqueness Validation Tests")
    class UniquenessValidationTests {

        @Test
        @DisplayName("Should throw exception when name already exists during bulk insert")
        void validateInvestor_WithDuplicateName_ShouldThrowException() {
            // Given
            when(investorRepository.existsByName(validInvestorDto.getName())).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> investorValidator.validateInvestor(validInvestorDto, 0, true))
                    .isInstanceOf(InvestorValidationException.class);

            verify(investorRepository).existsByName(validInvestorDto.getName());
        }

        @Test
        @DisplayName("Should pass when name does not exist")
        void validateInvestor_WithUniqueName_ShouldPass() {
            // Given
            when(investorRepository.existsByName(validInvestorDto.getName())).thenReturn(false);

            // When & Then
            assertThatCode(() -> investorValidator.validateInvestor(validInvestorDto, 0, true))
                    .doesNotThrowAnyException();

            verify(investorRepository).existsByName(validInvestorDto.getName());
        }

        @Test
        @DisplayName("Should skip uniqueness check when not in bulk mode")
        void validateInvestor_InNonBulkMode_ShouldSkipUniquenessCheck() {
            // When & Then
            assertThatCode(() -> investorValidator.validateInvestor(validInvestorDto, 0, false))
                    .doesNotThrowAnyException();

            verifyNoInteractions(investorRepository);
        }
    }

    @Nested
    @DisplayName("Required Fields Validation Tests")
    class RequiredFieldsValidationTests {

        @Test
        @DisplayName("Should throw exception when status is null")
        void validateInvestor_WithNullStatus_ShouldThrowException() {
            // Given
            validInvestorDto.setStatus(null);

            // When & Then
            assertThatThrownBy(() -> investorValidator.validateInvestor(validInvestorDto, 0, true))
                    .isInstanceOf(InvestorValidationException.class);

            verifyNoInteractions(investorRepository);
        }

        @Test
        @DisplayName("Should throw exception when status is empty")
        void validateInvestor_WithEmptyStatus_ShouldThrowException() {
            // Given
            validInvestorDto.setStatus("");

            // When & Then
            assertThatThrownBy(() -> investorValidator.validateInvestor(validInvestorDto, 0, true))
                    .isInstanceOf(InvestorValidationException.class);

            verifyNoInteractions(investorRepository);
        }

        @Test
        @DisplayName("Should throw exception when type is null")
        void validateInvestor_WithNullType_ShouldThrowException() {
            // Given
            validInvestorDto.setType(null);

            // When & Then
            assertThatThrownBy(() -> investorValidator.validateInvestor(validInvestorDto, 0, true))
                    .isInstanceOf(InvestorValidationException.class);

            verifyNoInteractions(investorRepository);
        }

        @Test
        @DisplayName("Should throw exception when type is empty")
        void validateInvestor_WithEmptyType_ShouldThrowException() {
            // Given
            validInvestorDto.setType("");

            // When & Then
            assertThatThrownBy(() -> investorValidator.validateInvestor(validInvestorDto, 0, true))
                    .isInstanceOf(InvestorValidationException.class);

            verifyNoInteractions(investorRepository);
        }

        @Test
        @DisplayName("Should accept valid required fields")
        void validateInvestor_WithValidRequiredFields_ShouldPass() {
            // Given
            validInvestorDto.setName("Valid Name");
            validInvestorDto.setStatus("ACTIVE");
            validInvestorDto.setType("VC");
            when(investorRepository.existsByName(anyString())).thenReturn(false);

            // When & Then
            assertThatCode(() -> investorValidator.validateInvestor(validInvestorDto, 0, true))
                    .doesNotThrowAnyException();

            verify(investorRepository).existsByName(validInvestorDto.getName());
        }
    }

    @Nested
    @DisplayName("Email Validation Tests")
    class EmailValidationTests {

        @Test
        @DisplayName("Should accept valid email format")
        void validateInvestor_WithValidEmail_ShouldPass() {
            // Given
            validInvestorDto.setCreatorEmail("valid@example.com");
            validInvestorDto.setAdminEmail("admin@example.com");
            when(investorRepository.existsByName(anyString())).thenReturn(false);

            // When & Then
            assertThatCode(() -> investorValidator.validateInvestor(validInvestorDto, 0, true))
                    .doesNotThrowAnyException();

            verify(investorRepository).existsByName(validInvestorDto.getName());
        }

        @Test
        @DisplayName("Should allow null emails")
        void validateInvestor_WithNullEmails_ShouldPass() {
            // Given
            validInvestorDto.setCreatorEmail(null);
            validInvestorDto.setAdminEmail(null);
            when(investorRepository.existsByName(anyString())).thenReturn(false);

            // When & Then
            assertThatCode(() -> investorValidator.validateInvestor(validInvestorDto, 0, true))
                    .doesNotThrowAnyException();

            verify(investorRepository).existsByName(validInvestorDto.getName());
        }

        @Test
        @DisplayName("Should throw exception for obviously invalid email")
        void validateInvestor_WithInvalidEmail_ShouldThrowException() {
            // Given
            validInvestorDto.setCreatorEmail("clearly-not-an-email");

            // When & Then
            assertThatThrownBy(() -> investorValidator.validateInvestor(validInvestorDto, 0, true))
                    .isInstanceOf(InvestorValidationException.class);

            verifyNoInteractions(investorRepository);
        }
    }

    @Nested
    @DisplayName("Website Validation Tests")
    class WebsiteValidationTests {

        @Test
        @DisplayName("Should accept valid website URLs")
        void validateInvestor_WithValidWebsite_ShouldPass() {
            // Given
            validInvestorDto.setWebsite("https://example.com");
            when(investorRepository.existsByName(anyString())).thenReturn(false);

            // When & Then
            assertThatCode(() -> investorValidator.validateInvestor(validInvestorDto, 0, true))
                    .doesNotThrowAnyException();

            verify(investorRepository).existsByName(validInvestorDto.getName());
        }

        @Test
        @DisplayName("Should allow null website")
        void validateInvestor_WithNullWebsite_ShouldPass() {
            // Given
            validInvestorDto.setWebsite(null);
            when(investorRepository.existsByName(anyString())).thenReturn(false);

            // When & Then
            assertThatCode(() -> investorValidator.validateInvestor(validInvestorDto, 0, true))
                    .doesNotThrowAnyException();

            verify(investorRepository).existsByName(validInvestorDto.getName());
        }

        @Test
        @DisplayName("Should throw exception for obviously invalid URL")
        void validateInvestor_WithInvalidWebsite_ShouldThrowException() {
            // Given
            validInvestorDto.setWebsite("not-a-valid-url");

            // When & Then
            assertThatThrownBy(() -> investorValidator.validateInvestor(validInvestorDto, 0, true))
                    .isInstanceOf(InvestorValidationException.class);

            verifyNoInteractions(investorRepository);
        }
    }

    @Nested
    @DisplayName("Numeric Fields Validation Tests")
    class NumericFieldsValidationTests {

        @Test
        @DisplayName("Should accept valid completeness score")
        void validateInvestor_WithValidCompletenessScore_ShouldPass() {
            // Given
            validInvestorDto.setCompletenessScore(85);
            when(investorRepository.existsByName(anyString())).thenReturn(false);

            // When & Then
            assertThatCode(() -> investorValidator.validateInvestor(validInvestorDto, 0, true))
                    .doesNotThrowAnyException();

            verify(investorRepository).existsByName(validInvestorDto.getName());
        }

        @Test
        @DisplayName("Should allow null completeness score")
        void validateInvestor_WithNullCompletenessScore_ShouldPass() {
            // Given
            validInvestorDto.setCompletenessScore(null);
            when(investorRepository.existsByName(anyString())).thenReturn(false);

            // When & Then
            assertThatCode(() -> investorValidator.validateInvestor(validInvestorDto, 0, true))
                    .doesNotThrowAnyException();

            verify(investorRepository).existsByName(validInvestorDto.getName());
        }

        @Test
        @DisplayName("Should accept valid impressions")
        void validateInvestor_WithValidImpressions_ShouldPass() {
            // Given
            validInvestorDto.setImpressions(1000);
            when(investorRepository.existsByName(anyString())).thenReturn(false);

            // When & Then
            assertThatCode(() -> investorValidator.validateInvestor(validInvestorDto, 0, true))
                    .doesNotThrowAnyException();

            verify(investorRepository).existsByName(validInvestorDto.getName());
        }

        @Test
        @DisplayName("Should allow null impressions")
        void validateInvestor_WithNullImpressions_ShouldPass() {
            // Given
            validInvestorDto.setImpressions(null);
            when(investorRepository.existsByName(anyString())).thenReturn(false);

            // When & Then
            assertThatCode(() -> investorValidator.validateInvestor(validInvestorDto, 0, true))
                    .doesNotThrowAnyException();

            verify(investorRepository).existsByName(validInvestorDto.getName());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very long valid name")
        void validateInvestor_WithLongValidName_ShouldPass() {
            // Given
            String longButValidName = "Very Long Investment Company Name With Many Words That Could Be Realistic";
            validInvestorDto.setName(longButValidName);
            when(investorRepository.existsByName(anyString())).thenReturn(false);

            // When & Then
            assertThatCode(() -> investorValidator.validateInvestor(validInvestorDto, 0, true))
                    .doesNotThrowAnyException();

            verify(investorRepository).existsByName(longButValidName);
        }

        @Test
        @DisplayName("Should handle name with numbers")
        void validateInvestor_WithNameContainingNumbers_ShouldPass() {
            // Given
            validInvestorDto.setName("ABC Capital 123");
            when(investorRepository.existsByName(anyString())).thenReturn(false);

            // When & Then
            assertThatCode(() -> investorValidator.validateInvestor(validInvestorDto, 0, true))
                    .doesNotThrowAnyException();

            verify(investorRepository).existsByName(validInvestorDto.getName());
        }

        @Test
        @DisplayName("Should handle international characters in name")
        void validateInvestor_WithInternationalCharacters_ShouldPass() {
            // Given
            validInvestorDto.setName("Société Générale Ventures");
            when(investorRepository.existsByName(anyString())).thenReturn(false);

            // When & Then
            assertThatCode(() -> investorValidator.validateInvestor(validInvestorDto, 0, true))
                    .doesNotThrowAnyException();

            verify(investorRepository).existsByName(validInvestorDto.getName());
        }

        @Test
        @DisplayName("Should handle case sensitivity in uniqueness check")
        void validateInvestor_WithDifferentCase_ShouldCallRepositoryWithExactCase() {
            // Given
            String mixedCaseName = "TeSt InVeStOr";
            validInvestorDto.setName(mixedCaseName);
            when(investorRepository.existsByName(mixedCaseName)).thenReturn(false);

            // When & Then
            assertThatCode(() -> investorValidator.validateInvestor(validInvestorDto, 0, true))
                    .doesNotThrowAnyException();

            verify(investorRepository).existsByName(mixedCaseName);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should validate complete investor successfully")
        void validateInvestor_WithCompleteValidData_ShouldPass() {
            // Given
            InvestorDto completeInvestor = TestDataBuilder.createCompleteInvestorDto();
            when(investorRepository.existsByName(anyString())).thenReturn(false);

            // When & Then
            assertThatCode(() -> investorValidator.validateInvestor(completeInvestor, 0, true))
                    .doesNotThrowAnyException();

            verify(investorRepository).existsByName(completeInvestor.getName());
        }

        @Test
        @DisplayName("Should validate minimal investor successfully")
        void validateInvestor_WithMinimalValidData_ShouldPass() {
            // Given
            InvestorDto minimalInvestor = TestDataBuilder.InvestorDtoBuilder
                    .anInvestorDto()
                    .withName("Minimal Investor")
                    .withStatus("ACTIVE")
                    .withType("VC")
                    .build();
            when(investorRepository.existsByName(anyString())).thenReturn(false);

            // When & Then
            assertThatCode(() -> investorValidator.validateInvestor(minimalInvestor, 0, true))
                    .doesNotThrowAnyException();

            verify(investorRepository).existsByName(minimalInvestor.getName());
        }

        @Test
        @DisplayName("Should handle validation with different index values")
        void validateInvestor_WithDifferentIndexes_ShouldWorkCorrectly() {
            // Given
            when(investorRepository.existsByName(anyString())).thenReturn(false);

            // When & Then
            for (int i = 0; i < 5; i++) {
                final int index = i;
                assertThatCode(() -> investorValidator.validateInvestor(validInvestorDto, index, true))
                        .doesNotThrowAnyException();
            }

            verify(investorRepository, times(5)).existsByName(validInvestorDto.getName());
        }
    }
}