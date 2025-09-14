package telran.library.entities.models;

import telran.library.entities.Book;
import telran.library.entities.PickRecord;
import telran.library.entities.Reader;
import telran.library.entities.enums.BooksReturnCode;
import telran.utils.Persistable;

import static telran.library.entities.enums.BooksReturnCode.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.util.*;

public class LibraryMaps extends AbstractLibrary implements Persistable {

    private Map<Long, Book> books = new HashMap<>();
    private Map<Integer, Reader> readers = new HashMap<>();
    private Map<Integer, List<PickRecord>> readersRecords = new HashMap<>();
    private Map<Long, List<PickRecord>> booksRecords = new HashMap<>();
    private Map<LocalDate, List<PickRecord>> records = new TreeMap<>();

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
        book.setAmount(book.getAmount()+amount);
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

    //Stream 2
    @Override
    public BooksReturnCode pickBook(long isbn, int readerId, LocalDate pickDate) {
        if (!books.containsKey(isbn))
            return NO_BOOK_ITEM;
        Book book = books.get(isbn);
        if (!readers.containsKey(readerId))
            return NO_READER;
        if(book.getAmountInUse() >= book.getAmount())
            return BOOKS_IN_USE;
        if(pickDate == null || pickDate.isBefore(LocalDate.of(2000, 01, 01)))
            return WRONG_BOOK_PICK_PERIOD;

        //Допускается ли читателю получить второй раз ту же книгу?

        PickRecord record = new PickRecord(isbn, readerId, pickDate);

        addReadersRecord(record);
        addBookRecords(record);
        addRecords(record);
        book.setAmountInUse(book.getAmountInUse() + 1);

        return OK;
    }

    private void addRecords(PickRecord record) {
        if (record == null) return;
        records.computeIfAbsent(record.getPickDate(), r -> new ArrayList<>()).add(record);
    }

    private void addBookRecords(PickRecord record) {
        if (record == null) return;
        booksRecords.computeIfAbsent(record.getIsbn(), r -> new ArrayList<>()).add(record);
    }

    private void addReadersRecord(PickRecord record) {
        if (record == null) return;
        readersRecords.computeIfAbsent(record.getReaderId(), r -> new ArrayList<>()).add(record);
    }

    @Override
    public List<Book> getBooksPickedByReader(int readerId) {
        //У метода может быть два смысла:
        // 1) показать все книги, которые брал читатель когда-либо;
        // 2) показать все книги, которые сейчас на руках у читателя;
        // Реализован первый вариант, для реализации второго нужно раскомментировать строку с фильтром

        List<PickRecord> listRecords = readersRecords.getOrDefault(readerId, new ArrayList<>());
        return listRecords.stream()
                //.filter(r -> r.getReturnDate() == null)
                .map(r -> books.get(r.getIsbn()))
                .distinct()
                .toList();
    }

    @Override
    public List<Reader> getReadersPickedBook(long isbn) {
        //Реализован вариант, при котором
        // отображаются все уникальные читатели книги за все время

        List<PickRecord> listRecords = booksRecords.getOrDefault(isbn, new ArrayList<>());

        return booksRecords.get(isbn).stream()
                .map(r -> readers.get(r.getReaderId()))
                .distinct()
                .toList();
    }

    @Override
    public List<Book> getBooksAuthor(String authorName) {
        // Возвращает все книги автора, которые зарегистрированы в библиотеке.
        // Если требуется найти только книги автора, которые есть в наличии,
        // то нужно раскомментировать второй фильтр
        return books.values().stream()
                .filter(b -> b.getAuthor().equals(authorName))
                //.filter(b -> b.getAmountInUse() < b.getAmount())
                .toList();
    }

    @Override
    public List<PickRecord> getPickedRecordsAtDates(LocalDate from, LocalDate to) {
        Collection<List<PickRecord>> res =
            ((TreeMap<LocalDate, List<PickRecord>>) records).subMap(from, to).values();
        return res.stream()
                .flatMap(l -> l.stream()).toList();
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
