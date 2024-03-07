package com.tgf.exception.handler.response;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * External functional interface which can be implemented
 * @param <T extends JsonNode>
 */
@FunctionalInterface
public interface ResponseGenerator<T extends JsonNode> {
    T apply(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, JsonNode jsonNode, String message);
}
