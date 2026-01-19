package nintaco.util;

public class Reference<T> {

    private T ref;

    public T get() {
        return ref;
    }

    public void set(T ref) {
        this.ref = ref;
    }
}
