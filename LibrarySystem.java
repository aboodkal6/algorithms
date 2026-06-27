import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class LibrarySystem {
    private AVLTree bookTree;
    private HashMap<String, Borrower> borrowers; //String = borrowerUserName
    private HashMap<Long, PrioryQueue> waitLists; //Long = ISBN

    public LibrarySystem(){
        this.bookTree = new AVLTree();
        this.borrowers = new HashMap<>();
        this.waitLists = new HashMap<>();
    }

    public void addBook(Book book) {
        bookTree.insert(book);
    }

    public void removeBook(long isbn) {
        bookTree.delete(isbn);
    }

    public Book searchBook(long isbn) {
        return bookTree.search(isbn);
    }

    public void updateCopies(long isbn, int delta) {
        Book book = bookTree.search(isbn);
        if(book != null)
            book.updateCopies(delta);
    }

    public Result borrowBook(String borrowerUserName, long isbn) {
        Book book = searchBook(isbn);
        if (book == null)
            return new Result(false, "Book not found.");
        Borrower borrower = borrowers.get(borrowerUserName);
        if (borrower == null)
            return new Result(false, "Borrower not found.");
        if (!borrower.canBorrows())
            return new Result(false, "Borrow limit reached.");
        if (!book.isAvailable()) {
            waitLists.putIfAbsent(isbn, new PrioryQueue());
            waitLists.get(isbn).enqueue(new WaitListEntry(isbn, borrower));
            return new Result(false, "Book not available, added to waitlist.");
        }
        BorrowRecord borrowRecord = new BorrowRecord(borrowerUserName, isbn, new Date());
        borrower.getBorrowRecord().put(isbn, borrowRecord);
        book.updateAvailableCopies(-1);
        borrower.incrementBorrows();
        book.incrementBorrowCount();
        return new Result(true, "Book borrowed successfully");
    }

    public Result returnBook(String borrowerUserName, long isbn) {
        Book book = searchBook(isbn);
        if (book == null)
            return new Result(false, "Book not found.");
        Borrower borrower = borrowers.get(borrowerUserName);
        if (borrower == null)
            return new Result(false, "Borrower not found.");
        BorrowRecord borrowRecord = borrower.getBorrowRecord().get(isbn);
        if(borrowRecord == null || borrowRecord.isReturned())
            return new Result(false, "The borrower is not borrowing this book right now.");
        borrowRecord.markReturned();
        book.updateAvailableCopies(1);
        borrower.decrementBorrows();
        notifyNextInWaitlist(isbn);
        if(borrowRecord.isOverDue())
            return new Result(true, "The book is over due, the borrower should be fined.\n" +
                    "The book has been returned successfully.");
        return new Result(true, "The book has been returned successfully.");
    }

    public Result registerBorrower(Borrower borrower) {
        if(borrowers.containsKey(borrower.getBorrowerUserName()))
            return new Result(false,"There is another borrower with the same UserName.");
        borrowers.put(borrower.getBorrowerUserName(), borrower);
        return new Result(true, "Borrower added successfully.");
    }

    public List<String> getTopAuthors(int n){
        List<Book> allBooks = bookTree.inorder();
        HashMap<String, Integer> authorCount = new HashMap<>();
        for(Book book : allBooks){
            authorCount.put(book.getAuthor(),
                authorCount.getOrDefault(book.getAuthor(), 0) + book.getBorrowCount());
        }
        List<String> authors = new ArrayList<>(authorCount.keySet());
        authors.sort((a, b) -> authorCount.get(b) - authorCount.get(a));
        return authors.subList(0, Math.min(n, authors.size()));
    }

    public List<Book> getMostBorrowedBooks(int n) {
        List<Book> mostBorrowed = bookTree.inorder();
        mostBorrowed.sort((a, b) -> b.getBorrowCount() - a.getBorrowCount());
        return mostBorrowed.subList(0, Math.min(n, mostBorrowed.size()));
    }

    public int getAvailableCount(){
        int count = 0;
        for(Book book : bookTree.inorder()){
            count += book.getAvailableCopies();
        }
        return count;
    }

    private void notifyNextInWaitlist(long isbn){
        PrioryQueue queue = waitLists.get(isbn);
        if(queue == null || queue.isEmpty()) return;
        WaitListEntry next = queue.dequeue();
        System.out.println("Notifying " + next.getBorrower().getName() +
                ": Book with ISBN " + isbn + " is now available.");
    }
}
