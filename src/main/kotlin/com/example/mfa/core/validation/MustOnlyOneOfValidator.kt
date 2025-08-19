package com.example.mfa.core.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.springframework.beans.BeanWrapperImpl

class MustOnlyOneOfValidator : ConstraintValidator<MustOnlyOneOf, Any> {

    private lateinit var fieldNames: Array<String>

    override fun initialize(constraintAnnotation: MustOnlyOneOf) {
        this.fieldNames = constraintAnnotation.fields
    }

    override fun isValid(value: Any, context: ConstraintValidatorContext): Boolean {
        val beanWrapper = BeanWrapperImpl(value)

        // Count how many of the specified fields are not null or blank.
        val providedFieldsCount = fieldNames.count { fieldName ->
            // We consider a field "provided" if it's not null and, if it's a string, not blank.
            when (val fieldValue = beanWrapper.getPropertyValue(fieldName)) {
                is String -> fieldValue.isNotBlank()
                null -> false
                else -> true
            }
        }

        // The validation is successful if and only if exactly one field was provided.
        return providedFieldsCount == 1
    }
}