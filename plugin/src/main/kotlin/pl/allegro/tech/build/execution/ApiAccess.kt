package pl.allegro.tech.build.execution

import java.net.URI

internal data class ApiAccess(val tagsEndpoint: URI, val credential: HeaderCredential)
