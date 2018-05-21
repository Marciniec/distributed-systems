package messages;

import java.io.Serializable;

public class SearchRequest implements Request, Serializable {
    private String title;
    private String client;

    public SearchRequest(String title, String client) {
        this.title = title;
        this.client = client;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
