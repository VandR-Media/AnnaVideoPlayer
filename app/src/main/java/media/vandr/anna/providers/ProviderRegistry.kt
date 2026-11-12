package media.vandr.anna.providers

/** Keep providers independent of the local video library and its permissions. */
class ProviderRegistry(providers: List<VideoProvider>) {
    private val byId: Map<String, VideoProvider>

    init {
        require(providers.all { it.id.isNotBlank() }) { "Provider IDs must not be blank" }
        require(providers.map { it.id }.distinct().size == providers.size) {
            "Provider IDs must be unique"
        }
        byId = providers.associateBy { it.id }
    }

    fun all(): List<VideoProvider> = byId.values.toList()

    fun find(id: String): VideoProvider? = byId[id]
}

object OnlineProviders {
    val registry: ProviderRegistry by lazy {
        ProviderRegistry(listOf(ZonaProvider()))
    }
}
