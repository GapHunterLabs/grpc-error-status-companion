package dev.gaphunter.grpcerrorstatuscompanion.detect

/**
 * Text signals this plugin uses to recognize a gRPC `StreamObserver`
 * receiver and a properly-converted `Status`-based error argument --
 * deliberately broad substring matches (not resolved types), same
 * "match a known name, don't resolve a symbol" discipline as
 * `SqlSignalNames`/`CorrelationSignals` elsewhere in this catalog.
 */
object GrpcSignals {

    /** Variable-name fragments that mark a receiver as a likely `StreamObserver`. */
    private val OBSERVER_NAME_FRAGMENTS = listOf("observer", "responseobserver", "streamobserver")

    /** Argument-text signals that the error was properly converted through gRPC's `Status` type. */
    private val STATUS_CONVERTED_FRAGMENTS = listOf(
        "asruntimeexception",
        "asexception",
        "statusruntimeexception",
        "statusexception",
    )

    fun looksLikeStreamObserver(receiverName: String): Boolean {
        val lower = receiverName.lowercase()
        return OBSERVER_NAME_FRAGMENTS.any { lower.contains(it) }
    }

    fun argumentLooksStatusConverted(argumentText: String): Boolean {
        val lower = argumentText.lowercase()
        return STATUS_CONVERTED_FRAGMENTS.any { lower.contains(it) }
    }
}
