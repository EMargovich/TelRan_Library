package telran.library.entities;

import java.time.LocalDate;
import java.util.Objects;

public class PickRecord {
    private long isbn;
    private int readerId;
    private LocalDate pickDate;
    private LocalDate returnDate;
    private int delayDays;

    public PickRecord() {
    }

    public PickRecord(long isbn, int readerId, LocalDate pickDate) {
        this.isbn = isbn;
        this.readerId = readerId;
        this.pickDate = pickDate;
    }

    public void setDelayDays(int delayDays) {
        this.delayDays = delayDays;
    }

    public long getIsbn() {
        return isbn;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public int getReaderId() {
        return readerId;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public LocalDate getPickDate() {
        return pickDate;
    }

    public int getDelayDays() {
        return delayDays;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PickRecord that = (PickRecord) o;
        return isbn == that.isbn && readerId == that.readerId && delayDays == that.delayDays && Objects.equals(pickDate, that.pickDate) && Objects.equals(returnDate, that.returnDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn, readerId, pickDate, returnDate, delayDays);
    }

    @Override
    public String toString() {
        return "PickRecord{" +
                "isbn=" + getIsbn() +
                ", readerId=" + getReaderId() +
                ", pickDate=" + getPickDate() +
                ", returnDate=" + getReturnDate() +
                ", delayDays=" + getDelayDays() +
                '}';
    }
}
