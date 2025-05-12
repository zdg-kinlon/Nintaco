package cn.kinlon.emu;

public class Main {
    public static void main(final String... args) {
        try {
            App.init(args);
        } catch (Throwable e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }
    }
}
