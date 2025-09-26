package telran.library.entities;

import java.io.Serializable;
import java.util.Objects;

public class Book implements Serializable {

    private long isbn;
    private String title;
    private String author;
    private int amount;
    private int amountInUse;
    private int pickPeriod;

    public Book() {
    }

    public Book(long isbn, String author, String title, int amount,
                int pickPeriod) {
        if(isbn > 0)
            this.isbn = isbn;
        this.author = author;
        this.title = title;
        if(amount >= 0)
            this.amount = amount;
        if(pickPeriod > 0)
            this.pickPeriod = pickPeriod;
    }

    public void setAmountInUse(int amountInUse) {
        this.amountInUse = amountInUse;
    }

    public void setPickPeriod(int pickPeriod) {
        if(pickPeriod >= 0)
            this.pickPeriod = pickPeriod;
    }

    public void setAmount(int amount) {
       // if(amount >= 0)
            this.amount = amount;
    }

    public long getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getAmount() {
        return amount;
    }

    public int getAmountInUse() {
        return amountInUse;
    }

    public int getPickPeriod() {
        return pickPeriod;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return isbn == book.isbn;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(isbn);
    }

    @Override
    public String toString() {
        return "Book{" +
                "isbn=" + getIsbn() +
                ", title='" + getTitle() + '\'' +
                ", author='" + getAuthor() + '\'' +
                ", amount=" + getAmount() +
                ", amountInUse=" + getAmountInUse() +
                ", pickPeriod=" + getPickPeriod() +
                '}';
    }
}
