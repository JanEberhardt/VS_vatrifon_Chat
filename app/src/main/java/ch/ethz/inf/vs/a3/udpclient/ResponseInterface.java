package ch.ethz.inf.vs.a3.udpclient;

import java.util.List;

import ch.ethz.inf.vs.a3.message.Message;

public interface ResponseInterface {
    public void handleResponse(List<String> data);
    public void handleError(int errorCode);
}
