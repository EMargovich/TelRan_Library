package telran.library.tests;

import java.time.LocalDate;
import java.util.*;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static telran.library.entities.enums.BooksReturnCode.*;

import telran.library.entities.*;
import telran.library.entities.models.*;

public class LibraryMapsTests {

    final long ISBN1 = 1111111111L;
    final long ISBN2 = 2222222222L;
    final long ISBN3 = 3333333333L;
    final long ISBN4 = 4444444444L;
    final long ISBN5 = 5555555555L;
    final long ISBN6 = 6666666666L;
    final long ISBN7 = 7777777777L;
    final String AUTHOR1 = "author1";
    final String AUTHOR2 = "author2";
    final String AUTHOR3 = "author3";
    final String AUTHOR4 = "author4";
    final String TITLE = "book_title";
    final int AMOUNT = 10;
    final int PICK_PERIOD = 5;
    final int PICK_PERIOD_NEGATIVE_MIN = 2;
    final int PICK_PERIOD_NEGATIVE_MAX = 31;
    final int READER_ID1 = 1;
    final int READER_ID2 = 2;
    final int READER_ID3 = 3;
    final int READER_ID4 = 4;
    final String NAME1 = "name1";
    final String NAME2 = "name2";
    final String NAME3 = "name3";
    final String PHONE1 = "0501111111";
    final String PHONE2 = "0502222222";
    final String PHONE3 = "0503333333";
    final LocalDate BIRTH_DATE1 = LocalDate.of(1980, 6, 1);
    final LocalDate BIRTH_DATE2 = LocalDate.of(2000, 1, 1);
    final LocalDate BIRTH_DATE3 = LocalDate.of(2015, 12, 1);
    final LocalDate PICK_DATE1 = LocalDate.now();
    final LocalDate PICK_DATE2 = LocalDate.now().minusDays(3);


    private Book[] books = new Book[6];
    private Reader[] readers = new Reader[3];
    private ILibrary library;

    @BeforeEach
    void setUp() {

        library = new LibraryMaps();

        //6 книг трех авторов (3 + 2 + 1), название одной книги повторяется
        books[0] = new Book(ISBN1, AUTHOR1, TITLE + "1", AMOUNT, PICK_PERIOD);
        books[1] = new Book(ISBN2, AUTHOR1, TITLE + "2", AMOUNT, PICK_PERIOD);
        books[2] = new Book(ISBN3, AUTHOR1, TITLE + "3", AMOUNT, PICK_PERIOD);
        books[3] = new Book(ISBN4, AUTHOR2, TITLE + "1", AMOUNT, PICK_PERIOD);
        books[4] = new Book(ISBN5, AUTHOR2, TITLE + "5", AMOUNT, PICK_PERIOD);
        books[5] = new Book(ISBN6, AUTHOR3, TITLE + "6", AMOUNT, PICK_PERIOD);

        for (Book b : books) {
            library.addBookItem(b);
        }

        //Три читателя разного возраста
        readers[0] = new Reader(READER_ID1, NAME1, PHONE1, BIRTH_DATE1);
        readers[1] = new Reader(READER_ID2, NAME2, PHONE2, BIRTH_DATE2);
        readers[2] = new Reader(READER_ID3, NAME3, PHONE3, BIRTH_DATE3);

        for (Reader r : readers) {
            library.addReader(r);
        }
    }

    @Test
     void testAddBookOK() {
        Book newBook = new Book(ISBN7, AUTHOR3, TITLE + "7", AMOUNT, PICK_PERIOD);
        assertEquals(OK, library.addBookItem(newBook));

        assertEquals(newBook, library.getBookItem(ISBN7));
        assertEquals(books[0], library.getBookItem(ISBN1));
        assertEquals(books[5], library.getBookItem(ISBN6));
    }

    @Test
    void testAddBookDuplicate() {
        Book newBook = new Book(ISBN2, AUTHOR3, TITLE + "7", AMOUNT, PICK_PERIOD);
        assertEquals(BOOK_ITEM_EXISTS, library.addBookItem(newBook));
        assertEquals(books[1], library.getBookItem(ISBN2));
    }

    @Test
    void testAddBookPickPeriodNegative() {
        Book newBook = new Book(ISBN7, AUTHOR3, TITLE + "7", AMOUNT, PICK_PERIOD_NEGATIVE_MIN);
        assertEquals(PICK_PERIOD_LESS_MIN, library.addBookItem(newBook));
        assertNull(library.getBookItem(ISBN7));

        newBook = new Book(ISBN7, AUTHOR3, TITLE + "7", AMOUNT, PICK_PERIOD_NEGATIVE_MAX);
        assertEquals(PICK_PERIOD_GRATER_MAX, library.addBookItem(newBook));
        assertNull(library.getBookItem(ISBN7));
    }

    @Test
    void testAddDriverOK() {
        Reader newReader = new Reader(READER_ID4, NAME1, PHONE1, BIRTH_DATE1);
        assertEquals(OK, library.addReader(newReader));
        assertEquals(newReader, library.getReader(READER_ID4));
        assertEquals(readers[0], library.getReader(READER_ID1));
    }

    @Test
    void testAddReaderDuplicate() {
        Reader newReader = new Reader(READER_ID1, NAME2, PHONE3, BIRTH_DATE1);
        assertEquals(READER_EXISTS, library.addReader(newReader));
        assertEquals(readers[0], library.getReader(READER_ID1));
    }

    @Test
    void testGetEntitiesNegative() {
        assertNull(library.getBookItem(ISBN7));
        assertNull(library.getReader(READER_ID4));
    }

    //Нет метода, который позволяет посмотреть, сколько в библиотеке книг
    // каждого вида и сколько из них на руках

    @Test
    void testAddBookExemplars() {
        assertEquals(AMOUNT, library.getBookItem(ISBN1).getAmount());
        library.addBookExemplars(ISBN1, 5);
        assertEquals(AMOUNT + 5, library.getBookItem(ISBN1).getAmount());
    }

    @Test
    void testAmountInUse() {
        assertEquals(AMOUNT, library.getBookItem(ISBN1).getAmount());
        assertEquals(0, library.getBookItem(ISBN1).getAmountInUse());
        library.getBookItem(ISBN1).setAmountInUse(AMOUNT-1);
        assertEquals(AMOUNT-1, library.getBookItem(ISBN1).getAmountInUse());
    }

    @Test
    void testPickBookOk() {
        assertEquals(OK, library.pickBook(ISBN1, READER_ID1, PICK_DATE1));
    }

    @Test
    void testPickBookNegative() {
        assertEquals(NO_BOOK_ITEM, library.pickBook(ISBN7, READER_ID1, PICK_DATE1));

        library.getBookItem(ISBN1).setAmountInUse(AMOUNT);
        assertEquals(NO_BOOKS_EXEMPLARS, library.pickBook(ISBN1, READER_ID1, PICK_DATE1));

        assertEquals(NO_READER, library.pickBook(ISBN2, READER_ID4, PICK_DATE1));

        assertEquals(OK, library.pickBook(ISBN3, READER_ID1, PICK_DATE2));
        assertEquals(READER_READS_IT, library.pickBook(ISBN3, READER_ID1, PICK_DATE1));
    }

    @Test
    void getBooksPickedByReader() {
        List<Book> expected = new ArrayList<>();
        assertEquals(expected, library.getBooksPickedByReader(READER_ID1));

        library.pickBook(ISBN1, READER_ID1, PICK_DATE2);
        expected.add(library.getBookItem(ISBN1));
        assertEquals(expected, library.getBooksPickedByReader(READER_ID1));

        library.pickBook(ISBN2, READER_ID1, PICK_DATE1);
        expected.add(library.getBookItem(ISBN2));
        assertEquals(expected, library.getBooksPickedByReader(READER_ID1));
    }

    @Test
    void testGetReadersPickedBooks() {
        List<Reader> expected = new ArrayList<>();
        assertEquals(expected, library.getReadersPickedBook(ISBN1));

        library.pickBook(ISBN1, READER_ID1, PICK_DATE2);
        expected.add(library.getReader(READER_ID1));
        assertEquals(expected, library.getReadersPickedBook(ISBN1));

        library.pickBook(ISBN1, READER_ID2, PICK_DATE2);
        expected.add(library.getReader(READER_ID2));
        assertEquals(expected, library.getReadersPickedBook(ISBN1));
    }

    @Test
    void tesGetBooksByAuthor() {
        assertEquals(new ArrayList<>(), library.getBooksAuthor(AUTHOR4));

        List<Book> booksByAuthor1 = library.getBooksAuthor(AUTHOR1);
        assertEquals(3, booksByAuthor1.size());
        assertTrue(booksByAuthor1.contains(library.getBookItem(ISBN1)));
        assertTrue(booksByAuthor1.contains(library.getBookItem(ISBN2)));
        assertTrue(booksByAuthor1.contains(library.getBookItem(ISBN3)));

        assertEquals(List.of(library.getBookItem(ISBN6))
                , library.getBooksAuthor(AUTHOR3));
    }

    @Test
    void testGetPickedRecordsAtDates() {
        assertEquals(new ArrayList<>(), library.getPickedRecordsAtDates(
                PICK_DATE2.minusDays(3), PICK_DATE2.minusDays(1)));

        library.pickBook(ISBN1, READER_ID1, PICK_DATE2.minusDays(4));
        library.pickBook(ISBN2, READER_ID1, PICK_DATE2.minusDays(3));
        library.pickBook(ISBN1, READER_ID2, PICK_DATE2.minusDays(2));
        library.pickBook(ISBN3, READER_ID2, PICK_DATE2.minusDays(1));

        List<PickRecord> actual = library.getPickedRecordsAtDates(
                PICK_DATE2.minusDays(3), PICK_DATE2.minusDays(1));

        assertEquals(2, actual.size());
        assertFalse(actual.contains(new PickRecord(ISBN1, READER_ID1,PICK_DATE2.minusDays(4) )));
        assertTrue(actual.contains(new PickRecord(ISBN2, READER_ID1,PICK_DATE2.minusDays(3) )));
        assertTrue(actual.contains(new PickRecord(ISBN1, READER_ID2,PICK_DATE2.minusDays(2) )));
        assertFalse(actual.contains(new PickRecord(ISBN3, READER_ID2,PICK_DATE2.minusDays(1) )));
    }

    //Sprint 3

    @Test
    void testRemoveBookNotInUse() {
        //Удаление существующей книги, которая не выдавалась
        String author = library.getBookItem(ISBN6).getAuthor();
        assertEquals(new RemovedBookData(library.getBookItem(ISBN6)
                , new ArrayList<>()), library.removeBook(ISBN6));
        assertNull(library.getBookItem(ISBN6));
        assertEquals(new ArrayList<>(), library.getBooksAuthor(author));
    }

    @Test
    void testRemoveBookNotExist() {
        assertNull(library.removeBook(ISBN7));
    }

    @Test
    void testRemoveBookInUse () {
        Book book = library.getBookItem(ISBN1);
        String author = library.getBookItem(ISBN1).getAuthor();

        library.pickBook(ISBN1, READER_ID1, PICK_DATE2);

        assertEquals(new RemovedBookData(book, null), library.removeBook(ISBN1));

        //Книга есть в библиотеке
        assertEquals(book, library.getBookItem(ISBN1));
        assertEquals(List.of(book), library.getBooksPickedByReader(READER_ID1));

        //Установлено отрицательное количество, то есть книга заблокирована
        //assertEquals(-1, library.getBookItem(ISBN1).getAmount());

        //Но она уже недоступна к выдаче, выборка по автору ее не показывает
        assertEquals(2, library.getBooksAuthor(author).size());
    }

    @Test
    void testRemoveAuthor() {
        Book book = library.getBookItem(ISBN1);

        List<RemovedBookData> actual = library.removeAuthor(AUTHOR1);
        RemovedBookData rbdBook1 = new RemovedBookData(book, new ArrayList<>());

        assertEquals(3, actual.size());
        assertTrue(actual.contains(rbdBook1));
    }

    @Test
    void testReturnBookNegative() {
        library.pickBook(ISBN1, READER_ID1, PICK_DATE2);

        assertEquals(new RemovedBookData(null, null)
                , library.returnBook(ISBN2, READER_ID1, PICK_DATE2));
        assertEquals(new RemovedBookData(null, null)
                , library.returnBook(ISBN1, READER_ID2, PICK_DATE2));
    }

    @Test
    void testReturnBookPickDateNull() {
        library.pickBook(ISBN5, READER_ID3, PICK_DATE2);

        assertEquals(new RemovedBookData(null, null)
                , library.returnBook(ISBN5, READER_ID3, null));
    }

    @Test
    void testReturnBook_AllOk() {
        //Выдача
        library.pickBook(ISBN1, READER_ID1, PICK_DATE2);
        assertEquals(AMOUNT, library.getBookItem(ISBN1).getAmount());
        assertEquals(1, library.getBookItem(ISBN1).getAmountInUse());

        //Возврат
        assertEquals(
                new RemovedBookData(library.getBookItem(ISBN1), null)
                , library.returnBook(ISBN1, READER_ID1, PICK_DATE1));

        assertEquals(0, library.getBookItem(ISBN1).getAmountInUse());

        //Обновление записи
        PickRecord pickRecord = library.getPickedRecordsAtDates(PICK_DATE2, PICK_DATE2.plusDays(1)).get(0);
        assertEquals(0, pickRecord.getDelayDays());
        assertEquals(PICK_DATE1, pickRecord.getReturnDate());
    }

    @Test
    void testReturnBookWithRemove() {
        Book book = library.getBookItem(ISBN1);

        library.pickBook(ISBN1, READER_ID1, PICK_DATE2);
        library.pickBook(ISBN1, READER_ID2, PICK_DATE2);

        assertEquals(new RemovedBookData(book, null), library.removeBook(ISBN1));
        assertEquals(book, library.getBookItem(ISBN1));
        assertEquals(-1, book.getAmount());
        assertEquals(2, book.getAmountInUse());

        assertEquals(new RemovedBookData(book, null), library.returnBook(ISBN1, READER_ID1, PICK_DATE1));
        assertEquals(book, library.getBookItem(ISBN1));
        assertEquals(-1, book.getAmount());
        assertEquals(1, book.getAmountInUse());

        PickRecord pr1 = new PickRecord(ISBN1, READER_ID1, PICK_DATE2);
        pr1.setReturnDate(PICK_DATE1);
        pr1.setDelayDays(0);

        PickRecord pr2 = new PickRecord(ISBN1, READER_ID2, PICK_DATE2);
        pr2.setReturnDate(PICK_DATE1);
        pr2.setDelayDays(0);

        RemovedBookData removeRBD = library.returnBook(ISBN1, READER_ID2, PICK_DATE1);
        List<PickRecord> removedPickRec = removeRBD.getRecords();
        assertTrue(removedPickRec.contains(pr1));
        assertTrue(removedPickRec.contains(pr2));
        assertEquals(book, removeRBD.getBook());

        assertNull(library.getBookItem(ISBN1));
        assertEquals(NO_BOOK_ITEM, library.pickBook(ISBN1, READER_ID2, PICK_DATE1));
    }

    @Test
    void testReturnBookWithDelay() {
        //Выдача
        library.pickBook(ISBN1, READER_ID1, PICK_DATE2.minusDays(8));
        //Возврат
        assertEquals(
                new RemovedBookData(library.getBookItem(ISBN1), null)
                , library.returnBook(ISBN1, READER_ID1, PICK_DATE2));

        //Обновление записи
        PickRecord pickRecord = library.getPickedRecordsAtDates(PICK_DATE2.minusDays(8), PICK_DATE2.plusDays(1)).get(0);
        assertEquals(3, pickRecord.getDelayDays());
        assertEquals(PICK_DATE2, pickRecord.getReturnDate());
    }

    //Sprint 4

    @Test
    void testGetReaderDelayingBooksNull() {
        library.pickBook(ISBN1, READER_ID1,
                PICK_DATE1.minusDays(PICK_PERIOD + 3));
        assertEquals(new ArrayList<>(),
                library.getReadersDelayingBooks(null));
    }

    @Test
    void testGetReadersDelayingBooks() {
        //Есть актуальная задержка
        library.pickBook(ISBN1, READER_ID1,
                PICK_DATE1.minusDays(PICK_PERIOD + 3));
        library.pickBook(ISBN3, READER_ID2,
                PICK_DATE1.minusDays(PICK_PERIOD + 2));

        //Нет актуальной задержки
        library.pickBook(ISBN2, READER_ID1,
                PICK_DATE1.minusDays(PICK_PERIOD - 1));
        library.pickBook(ISBN4, READER_ID2,
                PICK_DATE1.minusDays(PICK_PERIOD + 4));
        library.returnBook(ISBN4, READER_ID2, PICK_DATE1);

        List<ReaderDelay> delayingList =
                library.getReadersDelayingBooks(PICK_DATE1);
        assertEquals(2, delayingList.size());
        assertTrue(delayingList.contains(
                new ReaderDelay(library.getReader(READER_ID1), 3)));
        assertTrue(delayingList.contains(
                new ReaderDelay(library.getReader(READER_ID2), 2)));

    }

    @Test
    void testGetReadersDelayedBooks() {
        //Есть актуальная задержка, но книга на руках
        library.pickBook(ISBN1, READER_ID1, PICK_DATE1.minusDays(PICK_PERIOD + 3));

        //Книга возвращена вовремя
        library.pickBook(ISBN2, READER_ID1, PICK_DATE1.minusDays(PICK_PERIOD - 1));
        library.returnBook(ISBN2, READER_ID1, PICK_DATE1);

        //Книги возвращены, но с задержкой
        library.pickBook(ISBN4, READER_ID2, PICK_DATE1.minusDays(PICK_PERIOD + 4));
        library.returnBook(ISBN4, READER_ID2, PICK_DATE1.minusDays(3));

        library.pickBook(ISBN3, READER_ID2, PICK_DATE1.minusDays(PICK_PERIOD + 2));
        library.returnBook(ISBN3, READER_ID2, PICK_DATE1);

        List<ReaderDelay> delayingList = library.getReadersDelayedBooks();
        assertEquals(2, delayingList.size());
        assertTrue(delayingList.contains(new ReaderDelay(library.getReader(READER_ID2), 1)));
        assertTrue(delayingList.contains(new ReaderDelay(library.getReader(READER_ID2), 2)));
    }

    @Test
    void testGetMostPopularBooksNullErrors() {
        assertEquals(new ArrayList<>()
                ,library.getMostPopularBooks(null, PICK_DATE2
                        ,20, 100) );
        assertEquals(new ArrayList<>()
                ,library.getMostPopularBooks(PICK_DATE2, null
                        ,20, 100) );
    }

    @Test
    void testGetMostPopularBooks() {
        library.pickBook(ISBN1, READER_ID1, PICK_DATE1.minusDays(20)); //Не попадает по сроку
        library.pickBook(ISBN2, READER_ID2, PICK_DATE1.minusDays(15));
        library.pickBook(ISBN3, READER_ID3, PICK_DATE1.minusDays(10)); //Не попадает по возрасту

        library.pickBook(ISBN4, READER_ID2, PICK_DATE1.minusDays(13));
        library.returnBook(ISBN4, READER_ID2, PICK_DATE1.minusDays(1));
        library.pickBook(ISBN5, READER_ID3, PICK_DATE1.minusDays(12)); //Не попадает по возрасту
        library.pickBook(ISBN6, READER_ID1, PICK_DATE1.minusDays(1));  //Не попадает по сроку

        List<Book> popularBooks = library.getMostPopularBooks(
                PICK_DATE1.minusDays(15),
                PICK_DATE1.minusDays(5),
                20, 100);

        assertEquals(2, popularBooks.size());
        assertTrue(popularBooks.contains(library.getBookItem(ISBN2)));
        assertTrue(popularBooks.contains(library.getBookItem(ISBN4)));

        popularBooks = library.getMostPopularBooks(
                PICK_DATE1.minusDays(15),
                PICK_DATE1.minusDays(5),
                100, 20);
        assertTrue(popularBooks.isEmpty());

    }

    @Test
    void testGetMostPopularAuthors() {
        library.pickBook(ISBN1, READER_ID1, PICK_DATE1.minusDays(20)); //Не попадает по сроку
        library.pickBook(ISBN2, READER_ID2, PICK_DATE1.minusDays(15));
        library.pickBook(ISBN3, READER_ID3, PICK_DATE1.minusDays(10)); //Не попадает по возрасту

        library.pickBook(ISBN4, READER_ID2, PICK_DATE1.minusDays(13));
        library.returnBook(ISBN4, READER_ID2, PICK_DATE1.minusDays(10));
        library.pickBook(ISBN4, READER_ID2, PICK_DATE1.minusDays(5));

        library.pickBook(ISBN5, READER_ID3, PICK_DATE1.minusDays(12)); //Не попадает по возрасту
        library.pickBook(ISBN6, READER_ID1, PICK_DATE1.minusDays(5));

        List<String> authors = library.getMostPopularAuthors();
        assertEquals(2, authors.size());
        assertTrue(authors.contains(AUTHOR1));
        assertTrue(authors.contains(AUTHOR2));

    }

    @Test
    void testGetMostActiveReadersNull() {
        library.pickBook(ISBN1, READER_ID1, PICK_DATE2.minusDays(20)); //Не попадает по сроку
        assertEquals(new ArrayList<>(),library.getMostActiveReaders(PICK_DATE2, null));
        assertEquals(new ArrayList<>(),library.getMostActiveReaders(null, PICK_DATE2));
    }

    @Test
    void testGetMostActiveReaders() {
        library.pickBook(ISBN1, READER_ID1, PICK_DATE1.minusDays(20)); //Не попадает по сроку
        library.pickBook(ISBN2, READER_ID1, PICK_DATE1.minusDays(15));
        library.pickBook(ISBN3, READER_ID1, PICK_DATE1.minusDays(10));

        library.pickBook(ISBN4, READER_ID2, PICK_DATE1.minusDays(13));
        library.returnBook(ISBN4, READER_ID2, PICK_DATE1.minusDays(10));
        library.pickBook(ISBN4, READER_ID2, PICK_DATE1.minusDays(6));

        library.pickBook(ISBN5, READER_ID3, PICK_DATE1.minusDays(12));
        library.pickBook(ISBN6, READER_ID3, PICK_DATE1.minusDays(5));//Не попадает по сроку

        List<Reader> readers = library.getMostActiveReaders(
                PICK_DATE1.minusDays(15)
                , PICK_DATE1.minusDays(5));
        assertEquals(2, readers.size());
        assertTrue(readers.contains(library.getReader(READER_ID1)));
        assertTrue(readers.contains(library.getReader(READER_ID2)));
    }
}
