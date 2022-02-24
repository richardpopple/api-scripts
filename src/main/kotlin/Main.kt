import com.opencsv.CSVReaderBuilder
import java.io.FileReader
import gateways.NomisUserRoleApi


fun main() {

    // BEFORE RUNNING THIS YOU WILL NEED:
    // 1) A TOKEN WITH PERMISSION: ROLE_MAINTAIN_ACCESS_ROLES_ADMIN - see richardpopple-test client in dev.
    // 2) BE CONNECTED TO THE VPN
    // 3) COMMENT IN THE CODE TO CHOOSE THE ENVIRONMENT VIA CONFIG
    // 4) SET THE FLAG TO DETERMINE IF ONLY WORKING ON A SINGLE PRISON.
    // 5) PROVIDE A CSV WITH USERNAMES IN.
    val usernamesFileName = "/Users/richard.popple/Desktop/Usernames.csv"
//    val config = Dev()
     val config = PreProd()
    // val config = Prod()
    val token = ""
    val onlyFirstPrison = false // TODO change as required

    // Now do the migration.
    val usernames = mutableSetOf<String>()
    val csvReader = CSVReaderBuilder(FileReader(usernamesFileName)).build()
    var line: Array<String>? = csvReader.readNext()
    while (line != null) {
        usernames.add(line[0])
        line = csvReader.readNext()
    }

    val nomisUserRoleApi = NomisUserRoleApi(config.nomisUserRoleApi, token)
    val dpsRole = "TRANSFER_RESTRICTED_PATIENT"

    val usernamesAlreadyDpsNewRole = nomisUserRoleApi.withDpsRole(dpsRole)
    println("total users already with dps role: ${usernamesAlreadyDpsNewRole.size}")
    println("users already with dps role: ${usernamesAlreadyDpsNewRole}")

    // Remove all users who already have the role.
    usernames.removeAll(usernamesAlreadyDpsNewRole)
    println("total users to migrate: ${usernames.size}")
    println("users to migrate: ${usernames}")

    for (username: String in usernames) {
        nomisUserRoleApi.giveDpsRole(dpsRole, username)
        if (onlyFirstPrison) {
            break;
        }
    }
}

class Dev {
    val nomisUserRoleApi = "https://nomis-user-dev.aks-dev-1.studio-hosting.service.justice.gov.uk"
}

class PreProd {
    val nomisUserRoleApi = "https://nomis-user-pp.aks-live-1.studio-hosting.service.justice.gov.uk"
}

class Prod {
    val nomisUserRoleApi = "https://nomis-user.aks-live-1.studio-hosting.service.justice.gov.uk"
}