package gateways

import kotlinx.serialization.Serializable
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class PrisonApi (
    val prisonApi: String,
    val token: String,
) {
    private val limit = 100;

    @Throws(exceptionClasses = [WebClientException::class, ServerException::class])
    @Synchronized fun findAgenciesWithType(type: String) : List<String> {
        println("Attempting to find agencies with type: $type")
        var offset = 0
        var allAgencies = listOf<Agency>()
        while (true) {
            val pageOfResults = findAgenciesWithType(offset, this.limit)
            if (pageOfResults.isEmpty()) {
                break
            }
            allAgencies = listOf(allAgencies, pageOfResults).flatten()
            offset += limit

        }
        return allAgencies.filter { "INST".equals(it.agencyType) && it.active } .map { it.agencyId }
    }

    private fun findAgenciesWithType(offset: Int, limit: Int): List<Agency> {
        println("Attempting to find agencies of all types. Offset: $offset. Limit: $limit")

        val conn = URL("$prisonApi/api/agencies/").openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.setRequestProperty("Page-Limit", "$limit")
        conn.setRequestProperty("Page-Offset", "$offset")
        try {
            val lines = conn.inputStream.use {
                it.bufferedReader().readLines()
            }
            val combinedResult = lines.joinToString (" ")
            return Json {ignoreUnknownKeys = true} .decodeFromString(combinedResult)
        } catch (e: Exception) {
            e.printStackTrace()
            val errorString = getError(conn)
            println("Error response: ${conn.responseCode}: ${conn.responseMessage}")
            if (conn.responseCode >= 500) {
                throw ServerException()
            }
            throw WebClientException(conn.responseCode, errorString)
        } finally {
            conn.disconnect()
        }
    }

    private fun getError(conn: HttpURLConnection): String {
        if (conn.errorStream != null) {
            val errorInfo = conn.errorStream.use {
                it.bufferedReader().readLines()
            }
            val errorString = errorInfo.joinToString { it }
            errorInfo.forEach {
                println("Error Line: $it")
            }
            return errorString
        }
        return "No error"
    }

    @Serializable
    data class Agency(val agencyId: String, val agencyType: String, val active: Boolean)
}
