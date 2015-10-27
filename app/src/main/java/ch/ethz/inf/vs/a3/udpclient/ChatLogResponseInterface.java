package ch.ethz.inf.vs.a3.udpclient;

import java.util.List;

import ch.ethz.inf.vs.a3.message.Message;

public interface ChatLogResponseInterface {
    public void handleResponse(List<String> data);
    public void handleError(Message msg);
}
