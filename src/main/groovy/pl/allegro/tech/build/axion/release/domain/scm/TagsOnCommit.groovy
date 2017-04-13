package pl.allegro.tech.build.axion.release.domain.scm

import java.util.regex.Pattern

class TagsOnCommit {

    final String commitId

    final List<String> tags = new ArrayList<>()

    final boolean isHead

    TagsOnCommit(String commitId, List<String> tags, boolean isHead) {
        this.commitId = commitId
        this.tags.addAll(tags)
        this.isHead = isHead
    }

    static TagsOnCommit empty() {
        return new TagsOnCommit(null, [], false)
    }

    boolean hasOnlyMatching(Pattern pattern) {
        return !tags.isEmpty() && tags.every({ it ==~ pattern })
    }
}
