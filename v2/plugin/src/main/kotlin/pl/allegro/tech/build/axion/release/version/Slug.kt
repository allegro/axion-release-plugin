package pl.allegro.tech.build.axion.release.version

import com.github.slugify.Slugify

internal val slugifier: Slugify = Slugify.builder().build()

internal fun slugify(s: String): String = slugifier.slugify(s).replace('_', '-')
