package nintaco;

public class Main {
    public static void main(String... args) {
        try {
            App.init(args);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
