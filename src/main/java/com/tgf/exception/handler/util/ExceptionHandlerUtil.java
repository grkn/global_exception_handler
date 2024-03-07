package com.tgf.exception.handler.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.tgf.exception.handler.annotation.SelectedException;
import com.tgf.exception.handler.exception.SingleException;
import com.tgf.exception.handler.response.ResponseGenerator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ExceptionHandlerUtil {
    private ExceptionHandlerUtil() {
    }

    public static ResponseGenerator<ObjectNode> basicGenerator() {
        ResponseGenerator<ObjectNode> generator;
        generator = (httpServletRequest, httpServletResponse, jsonNode, msg) -> {
            ObjectNode rootNode = (ObjectNode) jsonNode;
            rootNode.set("method", new TextNode(httpServletRequest.getMethod()));
            rootNode.set("path", new TextNode(httpServletRequest.getRequestURI()));
            rootNode.set("time", new TextNode(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)));
            rootNode.set("msg", new TextNode(msg));
            rootNode.set("status", new IntNode(httpServletResponse.getStatus()));
            return rootNode;
        };
        return generator;
    }

    public static void setPayload(Exception exception, ObjectNode response) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        exception.getClass().getDeclaredMethod("setErrorResponse", JsonNode.class).invoke(exception, response);
    }

    public static String getGetMessage(Exception exception) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return (String) exception.getClass().getDeclaredMethod("getMessage").invoke(exception);
    }

    public static void writeErrorResponse(HttpServletResponse httpServletResponse, String errorString) throws IOException {
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        httpServletResponse.getWriter().write(errorString);
        httpServletResponse.getWriter().flush();
    }

    public static Class<? extends Exception> findExceptionInSpringContext(ApplicationContext context) {
        Object bean = context.getBeansWithAnnotation(SelectedException.class).values().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Add SelectedException annotation"));
        SelectedException selectedException = bean.getClass().getAnnotation(SelectedException.class);
        if (selectedException == null) {
            // checking proxy bean java proxy or cglib
            selectedException = bean.getClass().getSuperclass().getAnnotation(SelectedException.class);
        }

        if (selectedException != null) {
            return selectedException.exception();
        }

        throw new RuntimeException("Annotation has to be defined on spring bean");
    }

}
