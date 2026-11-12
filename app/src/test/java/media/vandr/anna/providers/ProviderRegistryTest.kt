package media.vandr.anna.providers

import org.junit.Assert.*
import org.junit.Test

class ProviderRegistryTest {
    private val zona = ZonaProvider()

    @Test fun registeredProviderCanBeFound() {
        val registry = ProviderRegistry(listOf(zona))
        assertSame(zona, registry.find("zona"))
        assertNull(registry.find("unknown"))
        assertEquals(1, registry.all().size)
    }

    @Test fun registryRejectsDuplicateIds() {
        try {
            ProviderRegistry(listOf(zona, ZonaProvider()))
            fail("Duplicate provider IDs must be rejected")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }

    @Test fun zonaIsExplicitlyNotConnected() {
        assertEquals("zona", zona.id)
        assertEquals(ProviderAvailability.NOT_CONFIGURED, zona.availability)
    }

    @Test fun registeredProvidersCannotHaveEmptyIds() {
        val empty = object : VideoProvider by zona {
            override val id: String = ""
        }
        try {
            ProviderRegistry(listOf(empty))
            fail("An empty provider ID must be rejected")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }
}
