import java.util.HashMap;

public class Borrower {
    private String borrowerUserName;
    private String name;
    private boolean isGraduate;
    private int activeBorrows;
    private HashMap<Long,BorrowRecord> borrowRecord; // long = ISBN for the borrowed book
    private final int MAX_BORROWS = 3;

    public Borrower(String borrowerUserName, String name, boolean isGraduate) {
        this.borrowerUserName = borrowerUserName;
        this.name = name;
        this.isGraduate = isGraduate;
        this.activeBorrows = 0;
        this.borrowRecord = new HashMap<>();
    }

    public String getBorrowerUserName(){
        return this.borrowerUserName;
    }

    public String getName(){
        return this.name;
    }

    public boolean isGraduate(){
        return isGraduate;
    }

    public int getActiveBorrows() {
        return activeBorrows;
    }

    public HashMap<Long,BorrowRecord> getBorrowRecord(){
        return borrowRecord;
    }

    public boolean canBorrows(){
        return this.activeBorrows<3;
    }

    public int getPriority(){
        return isGraduate? 0 : 1;
    }

    public void incrementBorrows(){
        activeBorrows++;
    }

    public void decrementBorrows(){
        activeBorrows--;
    }
}
