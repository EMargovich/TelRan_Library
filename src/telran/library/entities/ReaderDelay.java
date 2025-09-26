package telran.library.entities;

import java.util.Objects;

public class ReaderDelay {

    private Reader reader;
    private int delay;

    public ReaderDelay() {
    }

    public ReaderDelay(Reader reader, int delay) {
        this.reader = reader;
        this.delay = delay;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ReaderDelay that = (ReaderDelay) o;
        return delay == that.delay && Objects.equals(reader, that.reader);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reader, delay);
    }

    @Override
    public String toString() {
        return "telran.library.entities.ReaderDelay{" +
                "reader=" + getReader() +
                ", delay=" + getDelay() +
                '}';
    }

    public Reader getReader() {
        return reader;
    }

    public int getDelay() {
        return delay;
    }
}
