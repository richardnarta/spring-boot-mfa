package com.example.mfa.core.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.springframework.beans.BeanWrapperImpl

class RequireAtLeastOneValidator : ConstraintValidator<RequireAtLeastOne, Any> {

    private lateinit var fieldNames: Array<String>

    override fun initialize(constraintAnnotation: RequireAtLeastOne) {
        this.fieldNames = constraintAnnotation.fields
    }

    override fun isValid(value: Any, context: ConstraintValidatorContext): Boolean {
        // Use Spring's BeanWrapper to easily access the properties of the object
        val beanWrapper = BeanWrapperImpl(value)

        // Iterate through the field names provided in the annotation
        for (fieldName in fieldNames) {
            // Get the value of the field
            val fieldValue = beanWrapper.getPropertyValue(fieldName)

            // Check if the value is a non-blank string
            if (fieldValue is String && fieldValue.isNotBlank()) {
                return true // Found at least one valid field, so the validation passes
            }
            // You could add checks for other types here if needed (e.g., not null for non-string types)
        }

        // If the loop completes without finding a valid field, the validation fails
        return false
    }
}