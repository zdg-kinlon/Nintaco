package nintaco.gui.hexeditor;

import java.io.Serializable;

public class SearchQuery implements Serializable {

    private static final long serialVersionUID = 0;
    private final Type type;
    private final SearchText findWhat;
    private final SearchText replaceWith;
    private final Data data;
    private final Direction direction;
    private final Scope scope;
    private final boolean matchCase;
    private final boolean regularExpression;
    private final boolean wrapSearches;
    public SearchQuery(
            final Type type,
            final SearchText findWhat,
            final SearchText replaceWith,
            final Data data,
            final Direction direction,
            final Scope scope,
            final boolean matchCase,
            final boolean regularExpression,
            final boolean wrapSearches) {
        this.type = type;
        this.findWhat = findWhat;
        this.replaceWith = replaceWith;
        this.data = data;
        this.direction = direction;
        this.scope = scope;
        this.matchCase = matchCase;
        this.regularExpression = regularExpression;
        this.wrapSearches = wrapSearches;
    }

    public Type getType() {
        return type;
    }

    public SearchText getFindWhat() {
        return findWhat;
    }

    public SearchText getReplaceWith() {
        return replaceWith;
    }

    public Data getData() {
        return data;
    }

    public Direction getDirection() {
        return direction;
    }

    public Scope getScope() {
        return scope;
    }

    public boolean isMatchCase() {
        return matchCase;
    }

    public boolean isRegularExpression() {
        return regularExpression;
    }

    public boolean isWrapSearches() {
        return wrapSearches;
    }

    public enum Type {FindNext, ReplaceNext, ReplaceAll}

    public enum Data {Hex, Text}

    public enum Direction {Up, Down}

    public enum Scope {CurrentView, Selection, AllViews}
}
