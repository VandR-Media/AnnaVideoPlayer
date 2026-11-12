package media.vandr.anna.providers

/**
 * Contract for optional online catalogs.
 *
 * Providers must not download APKs, extract protected playback URLs or bypass access controls.
 * Actual playback remains in Annavue's existing Media3 player only for authorized streams.
 */
interface VideoProvider {
    val id: String
    val displayName: String
    val availability: ProviderAvailability

    suspend fun search(query: String): ProviderResult<List<CatalogVideo>>
    suspend fun details(itemId: String): ProviderResult<CatalogVideo>
    suspend fun playback(itemId: String): ProviderResult<StreamSource>
}

enum class ProviderAvailability {
    AVAILABLE,
    NOT_CONFIGURED
}

data class CatalogVideo(
    val id: String,
    val title: String,
    val description: String? = null,
    val posterHttpsUrl: String? = null,
    val year: Int? = null
)

data class StreamSource(
    val uri: String,
    val title: String,
    val mimeType: String? = null
)

sealed interface ProviderResult<out T> {
    data class Success<T>(val value: T) : ProviderResult<T>
    data class Unavailable(val reason: String) : ProviderResult<Nothing>
    data class Failure(val message: String) : ProviderResult<Nothing>
}
