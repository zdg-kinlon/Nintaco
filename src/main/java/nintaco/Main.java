package nintaco;

public class Main {
    public static final void main(final String... args) {
        try {
            App.init(args);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
