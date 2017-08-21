package pl.allegro.tech.build.axion.release.domain.scm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class TagsOnCommit {

    private final String commitId;

    private final List<String> tags = new ArrayList<>();

    private final boolean isHead;

    public TagsOnCommit(String commitId, List<String> tags, boolean isHead) {
        this.commitId = commitId;
        this.tags.addAll(tags);
        this.isHead = isHead;
    }

    public static TagsOnCommit empty() {
        return new TagsOnCommit(null, Collections.<String>emptyList(), false);
    }

    public boolean hasOnlyMatching(Pattern pattern) {
        for (String tag : tags) {
            if (!pattern.matcher(tag).matches()) {
                return false;
            }
        }
        return !tags.isEmpty() && true;
    }

    public boolean hasAnyMatching(Pattern pattern) {
        for (String tag : tags) {
            if (pattern.matcher(tag).matches()) {
                return true;
            }
        }
        return false;
    }

    public String getCommitId() {
        return commitId;
    }

    public List<String> getTags() {
        return tags;
    }

    public boolean isHead() {
        return isHead;
    }
}
