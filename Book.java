public class Book {
    private long isbn;
    private String title;
    private String author;
    private int totalCopies;
    private int availableCopies;
    private int borrowCount;

    public Book(long isbn, String title, String author, int totalCopies) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
        this.borrowCount = 0;
    }

    public long getIsbn() {
        return this.isbn;
    }

    public String getTitle(){
        return this.title;
    }

    public String getAuthor(){
        return this.author;
    }

    public int getAvailableCopies(){
        return this.availableCopies;
    }

    public boolean isAvailable(){
        return availableCopies>0;
    }

    public int getBorrowCount(){
        return this.borrowCount;
    }

    public void incrementBorrowCount(){
        this.borrowCount++;
    }

    public void updateCopies(int delta){
        this.totalCopies += delta;
        this.availableCopies += delta;
    }

    public void updateAvailableCopies(int delta){
        this.availableCopies += delta;
    }
}
