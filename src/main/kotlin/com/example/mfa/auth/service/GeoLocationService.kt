package com.example.mfa.auth.service

import com.maxmind.geoip2.DatabaseReader
import com.maxmind.geoip2.exception.AddressNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.InetAddress

@Service
class GeoLocationService {

    private val logger = LoggerFactory.getLogger(GeoLocationService::class.java)
    private var databaseReader: DatabaseReader? = null

    init {
        try {
            // Load the GeoLite2 database from the resources folder
            val dbStream = javaClass.classLoader.getResourceAsStream("GeoLite2-City.mmdb")
            if (dbStream != null) {
                databaseReader = DatabaseReader.Builder(dbStream).build()
                logger.info("GeoLite2 database loaded successfully.")
            } else {
                logger.error("Could not find GeoLite2-City.mmdb in resources.")
            }
        } catch (e: Exception) {
            logger.error("Failed to load GeoLite2 database", e)
        }
    }

    fun getLocation(ipAddress: String): String {
        if (databaseReader == null || ipAddress == "127.0.0.1" || ipAddress == "0:0:0:0:0:0:0:1") {
            return "Local Address"
        }

        return try {
            val ip = InetAddress.getByName(ipAddress)
            val response = databaseReader!!.city(ip)

            val city = response.city.name ?: "Unknown City"
            val country = response.country.name ?: "Unknown Country"

            "$city, $country"
        } catch (_: AddressNotFoundException) {
            "Location Not Found"
        } catch (_: Exception) {
            "Error Retrieving Location"
        }
    }
}