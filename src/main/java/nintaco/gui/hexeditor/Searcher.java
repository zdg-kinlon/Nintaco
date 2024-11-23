package nintaco.gui.hexeditor;

import nintaco.gui.hexeditor.SearchQuery.Data;
import nintaco.gui.hexeditor.SearchQuery.Direction;
import nintaco.gui.hexeditor.SearchQuery.Scope;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Searcher {

    public static final Result NOT_FOUND = new Result();

    private Searcher() {
    }

    public static Result findNext(final DataSource[] sources,
                                  final DataSource source, final SearchQuery query,
                                  final CharTable charTable) {

        if (source.getIndex() < 0 || query.getFindWhat() == null) {
            return NOT_FOUND;
        }

        int start = source.getStartSelectedAddress();
        int end = source.getEndSelectedAddress();
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }
        if (start >= source.getSize()) {
            start = source.getSize() - 1;
        }
        if (start < 0) {
            start = 0;
        }
        if (end >= source.getSize()) {
            end = source.getSize() - 1;
        }
        if (end < 0) {
            end = 0;
        }
        source.setStartSelectedAddress(start);
        source.setEndSelectedAddress(end);

        Pattern pattern = null;
        int[] lower = null;
        int[] upper = null;
        if (query.getData() == Data.Text && query.isRegularExpression()) {
            pattern = Pattern.compile(query.getFindWhat().getValue(),
                    query.isMatchCase() ? 0 : Pattern.CASE_INSENSITIVE);
        } else if (query.getData() == Data.Text && !query.isMatchCase()) {
            lower = query.getFindWhat().parseLower(charTable);
            upper = query.getFindWhat().parseUpper(charTable);
        } else {
            lower = query.getFindWhat().parse(charTable);
            upper = null;
        }

        final int minAddress;
        final int maxAddress;
        if (query.getScope() == Scope.Selection) {
            minAddress = start;
            maxAddress = end;
        } else if (query.getDirection() == Direction.Down) {
            minAddress = start;
            maxAddress = source.getSize() - 1;
        } else {
            minAddress = 0;
            maxAddress = end;
        }

        Result result = find(query, source, charTable, pattern, lower, upper,
                minAddress, maxAddress);
        if (result != NOT_FOUND) {
            final boolean atCurrent = result.getStart() == start
                    && result.getEnd() == end;
            if (!atCurrent || (atCurrent && query.getScope() == Scope.Selection)) {
                return result;
            }
        }

        if (query.getDirection() == Direction.Down) {
            result = find(query, source, charTable, pattern, lower, upper,
                    minAddress + 1, maxAddress);
        } else {
            result = find(query, source, charTable, pattern, lower, upper,
                    minAddress, maxAddress - 1);
        }
        if (result != NOT_FOUND) {
            return result;
        } else if (query.getScope() == Scope.Selection || !query.isWrapSearches()) {
            return NOT_FOUND;
        }

        if (query.getScope() == Scope.AllViews) {
            for (int i = 0; i < sources.length - 1; i++) {
                int j = source.getIndex();
                if (query.getDirection() == Direction.Down) {
                    j += i + 1;
                    if (j >= sources.length) {
                        j -= sources.length;
                    }
                } else {
                    j -= i + 1;
                    if (j < 0) {
                        j += sources.length;
                    }
                }
                result = find(query, sources[j], charTable, pattern, lower, upper,
                        0, sources[j].getSize() - 1);
                if (result != NOT_FOUND) {
                    return result;
                }
            }
        }

        if (query.getDirection() == Direction.Down) {
            return find(query, source, charTable, pattern, lower, upper,
                    0, maxAddress);
        } else {
            return find(query, source, charTable, pattern, lower, upper,
                    minAddress, source.getSize() - 1);
        }
    }

    private static Result find(SearchQuery query, DataSource source,
                               CharTable charTable, Pattern pattern, int[] lower, int[] upper,
                               int minAddress, int maxAddress) {
        if (minAddress >= source.getSize()) {
            minAddress = source.getSize() - 1;
        }
        if (minAddress < 0) {
            minAddress = 0;
        }
        if (maxAddress >= source.getSize()) {
            maxAddress = source.getSize() - 1;
        }
        if (maxAddress < 0) {
            maxAddress = 0;
        }
        final Result result;
        if (query.getDirection() == Direction.Down) {
            if (pattern == null) {
                result = findHexDown(source, lower, upper, minAddress, maxAddress);
            } else {
                result = findRegExDown(source, charTable, pattern, minAddress,
                        maxAddress);
            }
        } else {
            if (pattern == null) {
                result = findHexUp(source, lower, upper, minAddress, maxAddress);
            } else {
                result = findRegExUp(source, charTable, pattern, minAddress,
                        maxAddress);
            }
        }
        return result;
    }

    private static Result findRegExUp(DataSource source, CharTable charTable,
                                      Pattern pattern, int minAddress, int maxAddress) {
        final DataSourceCharSequence sequence
                = new DataSourceCharSequence(source, charTable);
        final Matcher matcher = pattern.matcher(sequence);
        int lastStart = -1;
        int lastEnd = -1;
        if (matcher.find(minAddress)) {
            do {
                final int start = matcher.start();
                final int end = matcher.end() - 1;
                if (end <= maxAddress) {
                    lastStart = start;
                    lastEnd = end;
                } else {
                    break;
                }
            } while (matcher.find(lastStart + 1));
        }
        if (lastStart >= 0) {
            return new Result(source, lastStart, lastEnd);
        }
        return NOT_FOUND;
    }

    private static Result findRegExDown(DataSource source, CharTable charTable,
                                        Pattern pattern, int minAddress, int maxAddress) {
        final DataSourceCharSequence sequence
                = new DataSourceCharSequence(source, charTable);
        final Matcher matcher = pattern.matcher(sequence);
        if (matcher.find(minAddress)) {
            final int start = matcher.start();
            final int end = matcher.end() - 1;
            if (end <= maxAddress) {
                return new Result(sequence.getSource(), start, end);
            }
        }
        return NOT_FOUND;
    }

    private static Result findHexUp(DataSource source, int[] lower, int[] upper,
                                    int minAddress, int maxAddress) {
        maxAddress -= lower.length - 1;
        for (int address = maxAddress; address >= minAddress; address--) {
            if (matchesHex(source, address, lower, upper)) {
                return new Result(source, address, address + lower.length - 1);
            }
        }
        return NOT_FOUND;
    }

    private static Result findHexDown(DataSource source, int[] lower, int[] upper,
                                      int minAddress, int maxAddress) {
        maxAddress -= lower.length - 1;
        for (int address = minAddress; address <= maxAddress; address++) {
            if (matchesHex(source, address, lower, upper)) {
                return new Result(source, address, address + lower.length - 1);
            }
        }
        return NOT_FOUND;
    }

    private static boolean matchesHex(DataSource source, int address,
                                      int[] lower, int[] upper) {
        for (int offset = lower.length - 1; offset >= 0; offset--) {
            final int value = source.peek(address + offset) & 0xFF;
            if (!(value == lower[offset]
                    || (upper != null && value == upper[offset]))) {
                return false;
            }
        }
        return true;
    }

    public static class Result {

        private final DataSource source;
        private final int start;
        private final int end;
        private final boolean found;

        public Result() {
            this.found = false;
            this.start = -1;
            this.end = -1;
            this.source = null;
        }

        public Result(final DataSource source, final int start, final int end) {
            this.source = source;
            this.found = true;
            this.start = start;
            this.end = end;
        }

        public DataSource getSource() {
            return source;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public boolean isFound() {
            return found;
        }

        @Override
        public boolean equals(final Object obj) {
            Result r = (Result) obj;
            return r.getSource() == source && r.getStart() == start
                    && r.getEnd() == end && r.isFound() == found;
        }
    }
}
