package darkyenuscommand;

/**
 *
 */
public final class MatchResult<T> {
    public final boolean isDefinite;
    public final T[] results;

    public MatchResult(T[] results) {
        this.isDefinite = false;
        this.results = results;
    }

    public MatchResult(T result) {
        this.isDefinite = true;
        //noinspection unchecked
        this.results = (T[])new Object[]{result};
    }

    public T result() {
        return results[0];
    }
}
