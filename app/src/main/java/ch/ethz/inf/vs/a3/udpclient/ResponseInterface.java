package ch.ethz.inf.vs.a3.udpclient;

import ch.ethz.inf.vs.a3.message.Message;

/**
 * Created by jan on 26.10.15.
 */
public interface ResponseInterface {
    public void handleResponse(Message msg);
}
