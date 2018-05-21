package messages;

import java.io.Serializable;

public class SearchResponse implements Serializable{
    private String title;
    private int price;

    public SearchResponse(String title, int price) {
        this.title = title;
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
