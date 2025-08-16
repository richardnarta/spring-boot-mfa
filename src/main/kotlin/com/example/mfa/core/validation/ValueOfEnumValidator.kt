package com.example.mfa.core.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class ValueOfEnumValidator : ConstraintValidator<ValueOfEnum, CharSequence> {
    private lateinit var acceptedValues: Set<String>

    override fun initialize(constraintAnnotation: ValueOfEnum) {
        acceptedValues = constraintAnnotation.enumClass.java.enumConstants
            .map { it.name }
            .toSet()
    }

    override fun isValid(value: CharSequence?, context: ConstraintValidatorContext): Boolean {
        return value == null || acceptedValues.contains(value.toString())
    }
}