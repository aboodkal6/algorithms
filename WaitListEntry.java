import java.util.Date;

public class WaitListEntry {
    private Borrower borrower;
    private long isbn;
    private Date requestDate;
    private int priority;

    public WaitListEntry(long isbn, Borrower borrower) {
        this.isbn = isbn;
        this.borrower = borrower;
        this.priority = borrower.getPriority();
        this.requestDate = new Date();
    }

    public Borrower getBorrower() {
        return borrower;
    }

    public long getIsbn() {
        return isbn;
    }

    public int getPriority() {
        return priority;
    }

    public int comperTo(WaitListEntry other){
        if(this.priority != other.priority)
            return Integer.compare(this.priority, other.priority);
        return this.requestDate.compareTo(other.requestDate);
    }
}
