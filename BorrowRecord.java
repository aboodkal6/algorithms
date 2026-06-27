import java.util.Date;

public class BorrowRecord {
    private String borrowerUserName;
    private long isbn;
    private Date borrowDate;
    private Date expectedReturn;
    private boolean isReturned;

    public BorrowRecord(String borrowerUserName, long isbn, Date borrowDate) {
        this.borrowerUserName = borrowerUserName;
        this.isbn = isbn;
        this.borrowDate = borrowDate;
        this.expectedReturn = new Date(borrowDate.getTime() + 14L * 24 * 60 * 60 * 1000);
        this.isReturned = false;
    }

    public String getborrowerUserName() {
        return borrowerUserName;
    }

    public long getIsbn(){
        return isbn;
    }

    public Date getBorrowDate(){
        return borrowDate;
    }

    public Date getExpectedReturn() {
        return expectedReturn;
    }

    public boolean isReturned(){
        return isReturned;
    }

    public void markReturned(){
        this.isReturned = true;
    }

    public boolean isOverDue(){
        return expectedReturn.before(new Date());
    }
}
