package com.tgf.exception.handler.exception;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Single exception is interface that forces user to set message and payload which is JsonNode
 * Each exception system can have multiple response structure, so it is better to write it unstructured way.
 * @param <T>
 */
public interface SingleException<T extends JsonNode> {
    T getErrorResponse();
    void setErrorResponse(T t);
    String getMessage();
}
