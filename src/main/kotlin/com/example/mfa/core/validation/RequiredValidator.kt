package com.example.mfa.core.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class RequiredValidator: ConstraintValidator<Required, String> {
    override fun isValid(p0: String?, p1: ConstraintValidatorContext?): Boolean {
        return !p0.isNullOrEmpty()
    }
}