package com.example.mfa

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MfaAuthenticationApplication

fun main(args: Array<String>) {
	runApplication<MfaAuthenticationApplication>(*args)
}
