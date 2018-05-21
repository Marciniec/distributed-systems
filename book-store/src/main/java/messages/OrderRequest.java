package messages;

import scala.Serializable;

public class OrderRequest implements Request, Serializable {
    private String title;
    private String client;

    public OrderRequest(String title, String client) {
        this.title = title;
        this.client = client;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setClient(String client) {
        this.client = client;
    }

    @Override
    public String getClient() {
        return client;
    }

}
