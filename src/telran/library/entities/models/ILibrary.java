package telran.library.entities.models;

import telran.library.entities.Book;
import telran.library.entities.Reader;
import telran.library.entities.enums.BooksReturnCode;

import java.io.Serializable;

public interface ILibrary extends Serializable {
    //Sprint 1
    BooksReturnCode addBookItem(Book book);
    BooksReturnCode addReader(Reader reader);
    BooksReturnCode addBookExemplars(long isbn, int amount);
    Reader getReader(int readerId);
    Book getBookItem(long isbn);

}
