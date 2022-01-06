import gateways.NomisUserRoleApi
import gateways.PrisonApi

fun main() {
    // BEFORE RUNNING THIS YOU WILL NEED:
    // 1) A TOKEN WITH PERMISSION: ROLE_MAINTAIN_ACCESS_ROLES_ADMIN - see richardpopple-test client in dev.
    // 2) BE CONNECTED TO THE VPN
    // 3) COMMENT IN THE CODE TO CHOOSE THE ENVIRONMENT VIA CONFIG
    // 4) SET THE FLAG TO DETERMINE IF ONLY WORKING ON A SINGLE PRISON.
    val config = Dev()
    // val config = PreProd()
    // val config = Prod()
    val token = "TO ADD"
    val onlyFirstPrison = true // TODO change as required

    val prisonApi = PrisonApi(config.prisonApi, token)
    val nomisUserRoleApi = NomisUserRoleApi(config.nomisUserRoleApi, token)
    val nomisRole = "302"
    val dpsRole = "PRISON_RECEPTION"

    val prisonIds = prisonApi.findAgenciesWithType("INST")
    println("total prisons to migrate: ${prisonIds.size}")
    println("prisons to migrate: $prisonIds. Total: ${prisonIds.size}")

    val usernamesAlreadyDpsNewRole = nomisUserRoleApi.withDpsRole(dpsRole)
    println("total users already with dps role: ${usernamesAlreadyDpsNewRole.size}")
    println("users already with dps role: ${usernamesAlreadyDpsNewRole}")

    var processedUsernames = setOf<String>()
    for (prisonId in prisonIds) {
        println("Finding users with nomis role: $nomisRole for prison $prisonId")
        val usernamesWithRole302InPrison = nomisUserRoleApi.withRoleAndCaseload("302", prisonId)
        println("total users with nomis role $nomisRole in prison: $prisonId. ${usernamesWithRole302InPrison.size}")
        println("users with nomis role $nomisRole in prison: $prisonId. $usernamesWithRole302InPrison")

        val usersNotYetProcessed = usernamesWithRole302InPrison.minus(processedUsernames)
        println("users not yet processed with nomis role $nomisRole in prison: $prisonId. ${usersNotYetProcessed}")
        println("total users not yet processed with nomis role $nomisRole in prison: $prisonId. ${usersNotYetProcessed.size}")

        val usersNotYetProcessedWithoutDpsRole = usersNotYetProcessed.minus(usernamesAlreadyDpsNewRole)
        println("users not yet processed with nomis role $nomisRole but not dps role in prison: $prisonId. ${usersNotYetProcessedWithoutDpsRole}")
        println("total users not yet processed with nomis role $nomisRole but not dps role in prison: $prisonId. ${usersNotYetProcessedWithoutDpsRole.size}")

        for (username: String in usersNotYetProcessedWithoutDpsRole) {
            nomisUserRoleApi.giveDpsRole(dpsRole, username)
        }

        processedUsernames = processedUsernames.union(usersNotYetProcessedWithoutDpsRole)
        println("TOTAL SO FAR: ${processedUsernames.size}")

        if (onlyFirstPrison) {
            break;
        }
    }
}

class Dev {
    val prisonApi = "https://api-dev.prison.service.justice.gov.uk"
    val nomisUserRoleApi = "https://nomis-user-dev.aks-dev-1.studio-hosting.service.justice.gov.uk"
}

class PreProd {
    val prisonApi = "https://api-preprod.prison.service.justice.gov.uk"
    val nomisUserRoleApi = "https://nomis-user-pp.aks-live-1.studio-hosting.service.justice.gov.uk"
}

class Prod {
    val prisonApi = "https://api.prison.service.justice.gov.uk"
    val nomisUserRoleApi = "https://nomis-user.aks-live-1.studio-hosting.service.justice.gov.uk"
}