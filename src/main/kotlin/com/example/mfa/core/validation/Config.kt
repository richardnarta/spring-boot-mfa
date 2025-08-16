package com.example.mfa.core.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [RequireAtLeastOneValidator::class])
annotation class RequireAtLeastOne(
    val fields: Array<String>,
    val message: String = "At least one of the fields must be provided.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValueOfEnumValidator::class])
annotation class ValueOfEnum(
    val enumClass: KClass<out Enum<*>>,
    val message: String = "Value is not valid.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)