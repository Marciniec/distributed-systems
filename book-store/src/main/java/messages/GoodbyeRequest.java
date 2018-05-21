package messages;

import java.io.Serializable;

public class GoodbyeRequest implements Request, Serializable {

    private String client;

    public GoodbyeRequest(String client) {
        this.client = client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    @Override
    public String getClient() {
        return client;
    }
}
