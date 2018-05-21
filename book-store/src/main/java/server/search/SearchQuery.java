package server.search;

public class SearchQuery {
    private String bookName;
    private int databaseNumber;

    public SearchQuery(String bookName, int databaseNumber) {
        this.bookName = bookName;
        this.databaseNumber = databaseNumber;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public int getDatabaseNumber() {
        return databaseNumber;
    }

    public void setDatabaseNumber(int databaseNumber) {
        this.databaseNumber = databaseNumber;
    }
}

