package messages;

import java.io.Serializable;

public class StreamRequest implements Request, Serializable{
    private String client;
    private String title;

    public StreamRequest(String client, String title) {
        this.client = client;
        this.title = title;
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

    @Override
    public String getClient() {
        return client;
    }
}
