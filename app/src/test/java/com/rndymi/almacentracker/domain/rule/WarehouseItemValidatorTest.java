package com.rndymi.almacentracker.domain.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public final class WarehouseItemValidatorTest {

    private WarehouseItemValidator validator;

    @Before
    public void setUp() {
        validator = new WarehouseItemValidator();
    }

    @Test
    public void completeValuesAreValid() {
        WarehouseItemValidator.ValidationResult result =
                validator.validateRequiredFields(
                        "MR",
                        "1050",
                        "A1"
                );

        assertTrue(result.isValid());
    }

    @Test
    public void nullEmptyAndWhitespaceAreMissing() {
        WarehouseItemValidator.ValidationResult result =
                validator.validateRequiredFields(
                        null,
                        "",
                        " \t "
                );

        assertFalse(result.isValid());
        assertTrue(result.isMissing(
                WarehouseItemValidator.RequiredField.CATEGORY
        ));
        assertTrue(result.isMissing(
                WarehouseItemValidator.RequiredField.CODE
        ));
        assertTrue(result.isMissing(
                WarehouseItemValidator.RequiredField.SITE
        ));
    }

    @Test
    public void missingFieldsAreReportedIndependently() {
        WarehouseItemValidator.ValidationResult result =
                validator.validateRequiredFields(
                        "MR",
                        "",
                        "A1"
                );

        assertFalse(result.isMissing(
                WarehouseItemValidator.RequiredField.CATEGORY
        ));
        assertTrue(result.isMissing(
                WarehouseItemValidator.RequiredField.CODE
        ));
        assertFalse(result.isMissing(
                WarehouseItemValidator.RequiredField.SITE
        ));
    }
}
