package mavenplugin;

import java.util.Comparator;

public class Meta implements Comparable<Meta> {
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
    public int compareTo(Meta o) {
        return Comparator.comparingInt(Meta::getHash)
                .compare(o, this);
    }

    @Override
    public String toString() {
        return "Meta{" +
                "hash=" + hash +
                '}';
    }
}
