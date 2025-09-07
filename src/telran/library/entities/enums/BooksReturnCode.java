package telran.library.entities.enums;

public enum BooksReturnCode {
    OK,
    BOOK_ITEM_EXISTS,                 // addBookItem: книга с таким ISBN уже есть
    READER_EXISTS,                    // addReader: читатель с таким id уже есть
    NO_BOOK_ITEM,                     // addBookExemplars: книги с ISBN нет
    WRONG_BOOK_PICK_PERIOD,
    PICK_PERIOD_LESS_MIN,
    PICK_PERIOD_GRATER_MAX// addBookItem: pickPeriod вне [min,max]


}
