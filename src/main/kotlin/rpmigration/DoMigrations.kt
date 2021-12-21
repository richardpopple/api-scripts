package rpmigration

import output.PerOffenderFileOutput
import output.SynchronisedSummaryFileOutput
import spreadsheetaccess.RowInformation
import spreadsheetaccess.SpreadsheetReader

data class MigrationRequestEvent (
    val startRow: Int,
    val numberOfRows: Int,
)

class Migrations (
    private val spreadsheetReader: SpreadsheetReader,
    private val resultStream: SynchronisedSummaryFileOutput,
    private val progressStream: PerOffenderFileOutput,
    private val migrateOffenderCommand: MigrateOffender,
) {
    fun handle(command: MigrationRequestEvent) {
        println("Reading from row ${command.startRow} - ${command.numberOfRows} rows")

        val offenderInformation = spreadsheetReader.readRows(command.startRow, command.numberOfRows)
        val failedItems = offenderInformation.map { migrateOffender(it) }.filter { !it }.count()

        println("Read ${offenderInformation.size} items - $failedItems failed")
    }

    private fun migrateOffender(migrationInfo: RowInformation): Boolean {
        val progressStreamRef = progressStream.migrationStarted(migrationInfo.offenderNo)
        val migrationData = MigrateOffenderRequestEvent(migrationInfo.prisonId, migrationInfo.offenderNo, migrationInfo.targetHospitalCode, progressStreamRef)
        try {
            migrateOffenderCommand.handle(migrationData)
        } catch (th: Throwable) {
            resultStream.logMigrationResult(migrationInfo.offenderNo, false)
            progressStream.migrationFailed(progressStreamRef, th)
            return false
        }
        resultStream.logMigrationResult(migrationInfo.offenderNo, true)
        progressStream.migrationSucceeded(progressStreamRef)
        return true
    }
}