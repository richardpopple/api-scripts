package gateways

import kotlinx.serialization.Serializable
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class NomisUserRoleApi (
    val nomisUserRoleApi: String,
    val token: String,
) {
    private val pageSize = 100;


    @Throws(exceptionClasses = [WebClientException::class, ServerException::class])
    @Synchronized fun giveDpsRole(role: String, username: String): User {
        println("Adding dps role: $role to user: $username")
        val conn = URL("$nomisUserRoleApi/users/$username/roles/$role").openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $token")
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
            } else if (conn.responseCode == 409) {
                println("Username: $username already has the dps role: $role")
                return User(username)
            }
            throw WebClientException(conn.responseCode, errorString)
        } finally {
            conn.disconnect()
        }
    }

    @Throws(exceptionClasses = [WebClientException::class, ServerException::class])
    @Synchronized fun withDpsRole(role: String) : Set<String> {
        println("Attempting to find users with DPS role: $role")

        val firstPage = withDpsRolePaged(role, 0, this.pageSize)
        val totalPages = firstPage.totalPages
        return (0 until totalPages).flatMap { withDpsRolePaged(role, it, this.pageSize).content }.map { it.username }.toSet()
    }


    @Throws(exceptionClasses = [WebClientException::class, ServerException::class])
    @Synchronized fun withRoleAndCaseload(role: String, caseload: String) : Set<String> {
        println("Attempting to find users with role: $role in caseload: $caseload")

        val firstPage = withRoleAndCaseloadPaged(role, caseload, 0, this.pageSize)
        val totalPages = firstPage.totalPages
        return (0 until totalPages).flatMap { withRoleAndCaseloadPaged(role, caseload, it, this.pageSize).content }.map { it.username }.toSet()
    }

    private fun withDpsRolePaged(role: String, page: Int, size: Int): Page {
        println("Attempting to find users with DPS role: $role. Page: $page and Size: $size")
        val results = withUrl("$nomisUserRoleApi/users?accessRoles=$role&status=ACTIVE&size=$size&page=$page")
        return results
    }

    private fun withRoleAndCaseloadPaged(role: String, caseload: String, page: Int, size: Int): Page {
        println("Attempting to find users with role: $role in caseload: $caseload. Page: $page and Size: $size")
        val results = withUrl("$nomisUserRoleApi/users?nomisRole=$role&caseload=$caseload&status=ACTIVE&size=$size&page=$page")
        return results
    }

    private fun withUrl(url: String): Page {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $token")
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
    data class Page(val content: List<User>, val totalPages: Int, val last: Boolean)

    @Serializable
    data class User(val username: String)
}
