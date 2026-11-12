package media.vandr.anna.providers

/**
 * Zona is planned, not connected.
 *
 * No public, authorized provider API or playback integration has been confirmed.
 * This placeholder deliberately makes no network requests and exposes no fake titles.
 */
class ZonaProvider : VideoProvider {
    override val id: String = "zona"
    override val displayName: String = "Zona"
    override val availability: ProviderAvailability = ProviderAvailability.NOT_CONFIGURED

    private fun unavailable(): ProviderResult.Unavailable = ProviderResult.Unavailable(
        "Подключение Zona пока не настроено: официальный API и условия интеграции не подтверждены."
    )

    override suspend fun search(query: String): ProviderResult<List<CatalogVideo>> = unavailable()
    override suspend fun details(itemId: String): ProviderResult<CatalogVideo> = unavailable()
    override suspend fun playback(itemId: String): ProviderResult<StreamSource> = unavailable()
}
