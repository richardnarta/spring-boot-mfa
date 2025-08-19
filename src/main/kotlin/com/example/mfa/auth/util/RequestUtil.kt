package com.example.mfa.auth.util

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import ua_parser.Parser

@Component
object RequestUtil {
    private val uaParser = Parser()

    fun extractIpAddress(request: HttpServletRequest): String {
        val xffHeader = request.getHeader("X-Forwarded-For")
        if (xffHeader != null && xffHeader.isNotEmpty()) {
            return xffHeader.split(",")[0].trim()
        }
        return request.remoteAddr
    }

    fun parseUserAgent(userAgentString: String?): String {
        if (userAgentString == null) return "Unknown Device"
        val client = uaParser.parse(userAgentString)
        val os = client.os.family
        val browser = client.device.family
        return "$os - $browser"
    }
}