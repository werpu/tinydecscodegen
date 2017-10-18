package probes;

public class ReturnValue<T> {

    private T value;

    public ReturnValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}
