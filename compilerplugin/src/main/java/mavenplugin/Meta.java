package mavenplugin;

import java.util.Objects;

public class Meta {
    private static final Meta EMPTY = new Meta(0);
    private final int hash;

    public Meta(int hash) {
        this.hash = hash;
    }

    public static Meta empty() {
        return EMPTY;
    }

    public int getHash() {
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Meta meta = (Meta) o;
        return hash == meta.hash;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }

    @Override
    public String toString() {
        return "Meta{" +
                "hash=" + hash +
                '}';
    }
}
