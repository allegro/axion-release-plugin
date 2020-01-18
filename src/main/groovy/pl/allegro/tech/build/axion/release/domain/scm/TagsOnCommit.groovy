package pl.allegro.tech.build.axion.release.domain.scm

import java.util.regex.Pattern

class TagsOnCommit {

    final String commitId

    final List<String> tags = new ArrayList<>()

    TagsOnCommit(String commitId, List<String> tags) {
        this.commitId = commitId
        this.tags.addAll(tags)
    }

    static TagsOnCommit empty() {
        return new TagsOnCommit(null, [])
    }

    boolean hasOnlyMatching(Pattern pattern) {
        return !tags.isEmpty() && tags.every({ it ==~ pattern })
    }
}
