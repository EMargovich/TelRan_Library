package telran.library.entities.models;

import telran.library.entities.Book;
import telran.library.entities.Reader;
import telran.library.entities.enums.BooksReturnCode;
import telran.utils.Persistable;

import static telran.library.entities.enums.BooksReturnCode.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

public class LibraryMaps extends AbstractLibrary implements Persistable {

    Map<Long, Book> books = new HashMap<>();
    Map<Integer, Reader> readers = new HashMap<>();


    @Override
    public BooksReturnCode addBookItem(Book book) {
       if(book.getPickPeriod() < minPickPeriod)
           return BooksReturnCode.PICK_PERIOD_LESS_MIN;
       if(book.getPickPeriod() > maxPicPeriod)
           return BooksReturnCode.PICK_PERIOD_GRATER_MAX;

       BooksReturnCode res = books.putIfAbsent(book.getIsbn(), book) == null?
            OK : BOOK_ITEM_EXISTS;
       return res;
    }

    @Override
    public BooksReturnCode addReader(Reader reader) {
        if(reader == null) return null;
        BooksReturnCode res = readers.putIfAbsent(reader.getReaderId(), reader) == null ?
                OK : READER_EXISTS;
        return res;
    }

    @Override
    public BooksReturnCode addBookExemplars(long isbn, int amount) {
        if(!books.containsKey(isbn)) return NO_BOOK_ITEM;
        Book book = books.get(isbn);
        book.setAmountInUse(book.getAmountInUse()+amount);
        return OK;
    }

    @Override
    public Reader getReader(int readerId) {
        return readers.get(readerId);
    }

    @Override
    public Book getBookItem(long isbn) {
        return books.get(isbn);
    }

    @Override
    public void save(String fileName) {
        try (ObjectOutputStream outputStream =
                     new ObjectOutputStream(
                             new FileOutputStream(fileName))) {
            outputStream.writeObject(this);
        } catch (IOException e) {
            System.out.println("Error in method save " + e.getMessage());
        }
    }
}
