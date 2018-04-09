package com.videoweber.internet.client;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public interface ResponseProcessor<T extends Request> {
    public void process(T request, Response response, RequestManager requestManager);
    
    public Class<T> getRequestClass();
}
