package telran.library.entities.models;

import telran.library.entities.*;
import telran.library.entities.enums.BooksReturnCode;
import telran.utils.Persistable;

import static telran.library.entities.enums.BooksReturnCode.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class LibraryMaps extends AbstractLibrary implements Persistable {

    private Map<Long, Book> books = new HashMap<>();
    private Map<Integer, Reader> readers = new HashMap<>();
    private Map<Integer, List<PickRecord>> readersRecords = new HashMap<>();
    private Map<Long, List<PickRecord>> booksRecords = new HashMap<>();
    private NavigableMap<LocalDate, List<PickRecord>> records = new TreeMap<>();
    private Map<String, Set<Book>> authorBooks = new HashMap<>();

    @Override
    public BooksReturnCode addBookItem(Book book) {
       if(book.getPickPeriod() < minPickPeriod)
           return PICK_PERIOD_LESS_MIN;
       if(book.getPickPeriod() > maxPicPeriod)
           return PICK_PERIOD_GRATER_MAX;
       if(books.putIfAbsent(book.getIsbn(), book) != null) return BOOK_ITEM_EXISTS;
       addAuthorBooks(book);
       return OK;
    }

    private void addAuthorBooks(Book book) {
//        String key = book.getAuthor();
//        List<Book> list = authorBooks.computeIfAbsent(key, k -> new ArrayList<>()).;
//        boolean exist = list.stream().anyMatch(b -> b.getIsbn() == book.getIsbn());
//        if(!exist)
//            list.add(book);
        //Переделал под сет
        String key = book.getAuthor();
        authorBooks.computeIfAbsent(key, k -> new HashSet<>()).add(book);
    }

    @Override
    public BooksReturnCode addReader(Reader reader) {
        if(reader == null) return NO_READER;
        BooksReturnCode res = readers.putIfAbsent(reader.getReaderId(), reader) == null ?
                OK : READER_EXISTS;
        return res;
    }

    @Override
    public BooksReturnCode addBookExemplars(long isbn, int amount) {
        if(!books.containsKey(isbn)) return NO_BOOK_ITEM;
        Book book = books.get(isbn);
        book.setAmount(book.getAmount() + amount);
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

        Book book = books.get(isbn);
        if(book == null||book.getAmount() < 0)
            return NO_BOOK_ITEM;

        if(book.getAmountInUse() >= book.getAmount())
            return NO_BOOKS_EXEMPLARS;

        if (!readers.containsKey(readerId))
            return NO_READER;

        if(pickDate == null || pickDate.isBefore(LocalDate.of(2000, 01, 01)))
            return WRONG_BOOK_PICK_PERIOD;

        List<PickRecord> pickRecord = readersRecords.get(readerId);
        if(pickRecord != null &&
            pickRecord.stream()
                    .anyMatch(r -> r.getIsbn() == isbn && r.getReturnDate() == null))
            return READER_READS_IT;

        PickRecord record = new PickRecord(isbn, readerId, pickDate);
        addToMap(booksRecords, record.getIsbn(), record);
        addToMap(readersRecords, record.getReaderId(), record);
        addToMap(records, record.getPickDate(), record);

        book.setAmountInUse(book.getAmountInUse() + 1);
        return OK;
    }

    private <K> void addToMap(Map<K, List<PickRecord>> map, K key, PickRecord value) {
        map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }

    @Override
    public List<Book> getBooksPickedByReader(int readerId) {
        List<PickRecord> listRecords = readersRecords.getOrDefault(readerId, new ArrayList<>());

        return listRecords.stream()
                .map(r -> books.get(r.getIsbn()))
                .distinct()
                .toList();
    }

    @Override
    public List<Reader> getReadersPickedBook(long isbn) {
        List<PickRecord> listRecords = booksRecords.getOrDefault(isbn, new ArrayList<>());

        return listRecords.stream()
                .map(r -> readers.get(r.getReaderId()))
                .distinct()
                .toList();
    }

    @Override
    public List<Book> getBooksAuthor(String authorName) {
        if(authorName == null || authorName.isBlank())
            return new ArrayList<>();

        return authorBooks.getOrDefault(authorName, new HashSet<>()).stream()
                .filter(b -> b.getAmount()>b.getAmountInUse())
                .toList();
    }

    @Override
    public List<PickRecord> getPickedRecordsAtDates(LocalDate from, LocalDate to) {
        if(to.isBefore(from)) return new ArrayList<>();
        return records.subMap(from, to).values().stream()
                .flatMap(List::stream)
                .toList();

    }

    //Sprint 3

    @Override
    public RemovedBookData removeBook(long isbn) {
        Book book = getBookItem(isbn);
        if(book == null || book.getAmount() < 0)
            return null;
        book.setAmount(-1);
        return book.getAmountInUse() > 0 ?
                new RemovedBookData(book, null) : actualBookRemove(book);
    }

    private RemovedBookData actualBookRemove(Book book) {
        long isbn = book.getIsbn();
        List<PickRecord> removedRecords = booksRecords.get(isbn);

        if (removedRecords != null && !removedRecords.isEmpty()) {
            removeFromMap(records, removedRecords, PickRecord::getPickDate);
            removeFromMap(readersRecords, removedRecords, PickRecord::getReaderId);
        }

        books.remove(isbn);
        booksRecords.remove(isbn);

        Set<Book> booksByAuthor = authorBooks.get(book.getAuthor());
        if (booksByAuthor != null)
            booksByAuthor.remove(book);

        return new RemovedBookData(book,
                removedRecords == null ? new ArrayList<>() : removedRecords);
    }

    private <K> void removeFromMap(Map<K, List<PickRecord>> map
            , List<PickRecord> removedRecords
            , Function<PickRecord, K> getter) {
        if(removedRecords == null)
            return;
        removedRecords.forEach( r -> {
            List<PickRecord> list = map.get(getter.apply(r));
            if(list != null) list.remove(r);
        });
    }

    @Override
    public List<RemovedBookData> removeAuthor(String author) {
        List<Book> booksByAuthor = authorBooks.getOrDefault(author, new HashSet<>()).stream().toList();

        return booksByAuthor.stream()
                .filter(b -> b.getAmount() >= 0)
                .map(b -> removeBook(b.getIsbn()))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public RemovedBookData returnBook(long isbn, int readerId, LocalDate returnDate) {
        PickRecord record = getPickRecord(isbn, readerId);
        if(record == null || returnDate == null)
            return new RemovedBookData(null, null);

        //Обновляем запись данными возврата (в том числе днями просрочки)
        updateRecord(record, returnDate);

        //Обновляем информацию о книге
        Book returnedBook = getBookItem(isbn);
        returnedBook.setAmountInUse(returnedBook.getAmountInUse() - 1);

        //Если книга помечена на удаление и все экземпляры вернули,
        // то удаляем книгу
        if(returnedBook.getAmount() < 0
                && returnedBook.getAmountInUse() <= 0) {
            return actualBookRemove(returnedBook);
        }

        return new RemovedBookData(returnedBook, null);
    }


    private void updateRecord(PickRecord record, LocalDate returnDate) {
        record.setReturnDate(returnDate);
        int delay = getDaysDelay(record);
        record.setDelayDays(delay);
    }

    private int getDaysDelay(PickRecord record) {
        int pickDuration = (int) ChronoUnit.DAYS.
                between(record.getPickDate(), record.getReturnDate());
        int pickPeriod = books.get(record.getIsbn()).getPickPeriod();
        return pickDuration > pickPeriod ? pickDuration - pickPeriod : 0;
    }

    private PickRecord getPickRecord(long isbn, int readerId) {
        List<PickRecord> list = readersRecords.get(readerId);
        if (list == null) return null;
        return list.stream().filter(r ->
                r.getIsbn() == isbn && r.getReturnDate() == null)
                .findFirst()
                .orElse(null);
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

    //Sprint 4
    @Override
    public List<ReaderDelay> getReadersDelayingBooks(LocalDate currentDate) {
        //Формирует перечень читателей, которые на данный момент задерживают книги
        // и считает количество дней задержки
        if(currentDate == null)
            return new ArrayList<>();

        return records.values().stream()
                .flatMap(Collection::stream)
                .filter(r -> r.getReturnDate() == null)
                .filter(r -> getDayDelayBeforeReturn(r, currentDate) > 0)
                .map(r ->  new ReaderDelay(getReader(r.getReaderId()), getDayDelayBeforeReturn(r, currentDate)))
                .toList();
    }

    int getDayDelayBeforeReturn(PickRecord record, LocalDate currentDate) {
        int pickPeriod = books.get(record.getIsbn()).getPickPeriod();
        long pickDuration = ChronoUnit.DAYS.between(record.getPickDate(), currentDate);
        return (int) (pickDuration > pickPeriod ? pickDuration - pickPeriod : 0);
    }

    @Override
    public List<ReaderDelay> getReadersDelayedBooks() {
        //Формирует перечень читателей, которые когда-либо не возвращали книги
        return records.values().stream()
                .flatMap(lr -> lr.stream())
                .filter(r -> r.getDelayDays() > 0)
                .map(r -> new ReaderDelay
                        (readers.get(r.getReaderId())
                        , r.getDelayDays()))
                .toList();
    }

    @Override
    public List<Book> getMostPopularBooks(LocalDate fromDate, LocalDate toDate
            , int fromAge, int toAge) {
        //Популярность оцениваем по количеству записей о получении книги
        if( fromDate == null || toDate == null)
            return new ArrayList<>();

        if(fromDate.isAfter(toDate))
            return new ArrayList<>();

        List<PickRecord> recordsFromTo = getPickedRecordsAtDates(fromDate, toDate);
        if(recordsFromTo.isEmpty()) return new ArrayList<>();

        Map<Book, Long> mapTemp = recordsFromTo.stream()
                .filter(r -> isProperAge(r, fromAge, toAge))
                .collect(Collectors.groupingBy(r -> getBookItem(r.getIsbn())
                        , Collectors.counting()));

        return mapTemp.isEmpty() ? new ArrayList<>()
                : getListMaxValueFromMap(mapTemp);
    }

    private boolean isProperAge(PickRecord r, int fromAge, int toAge) {
        LocalDate pickDate = r.getPickDate();
        Reader reader = getReader(r.getReaderId());
        int readerAge = (int) ChronoUnit.YEARS.between(reader.getBirthDay()
                , pickDate);
        return readerAge >= fromAge && readerAge < toAge;
    }

    @Override
    public List<String> getMostPopularAuthors() {
        Map<String, Long> mapTemp = new HashMap<>();
        booksRecords.entrySet()
                .forEach(e -> mapTemp.merge(
                        getBookItem(e.getKey()).getAuthor()
                        , (long) e.getValue().size()
                        , Long::sum));
        return mapTemp.isEmpty() ? new ArrayList<>()
                :getListMaxValueFromMap(mapTemp);
    }

    @Override
    public List<Reader> getMostActiveReaders(LocalDate fromDate, LocalDate toDate) {
        if( fromDate == null || toDate == null)
            return new ArrayList<>();

        List<PickRecord> recordsFromTo = getPickedRecordsAtDates(fromDate, toDate);
        if(recordsFromTo.isEmpty()) return new ArrayList<>();

        Map<Reader, Long> mapTemp = recordsFromTo.stream()
                .collect(Collectors.groupingBy(r
                        -> getReader(r.getReaderId())
                        , Collectors.counting()));

        return mapTemp.isEmpty() ? new ArrayList<>()
                :getListMaxValueFromMap(mapTemp);
    }

    private <K> List<K> getListMaxValueFromMap(Map<K, Long> mapTemp) {
        long maxValue = Collections.max(mapTemp.values());
        List<K> res = new ArrayList<>();
        mapTemp.forEach((k, v) -> {
            if(v == maxValue) res.add(k);
        });
        return res;
    }

}