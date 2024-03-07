package com.tgf.exception.handler.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.tgf.exception.handler.condition.SingleExceptionCondition;
import com.tgf.exception.handler.exception.SingleException;
import com.tgf.exception.handler.response.ResponseGenerator;
import com.tgf.exception.handler.util.ExceptionHandlerUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
@Conditional(SingleExceptionCondition.class)
public class SingleExceptionConfig {

    private final ApplicationContext context;
    private static Class<? extends Exception> exceptionClazz;
    private final ObjectMapper objectMapper;

    private final ResponseGenerator responseGenerator;

    public SingleExceptionConfig(ApplicationContext context, @Autowired(required = false) ObjectMapper objectMapper,
                                 @Autowired(required = false) ResponseGenerator responseGenerator) {
        this.context = context;
        this.objectMapper = objectMapper;
        this.responseGenerator = responseGenerator;
    }

    @Bean
    public FilterRegistrationBean<Filter> registrationBean() {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registrationBean.setFilter(new Filter() {
            @Override
            public void init(FilterConfig filterConfig) {
                // find exception in initialization to get better performance
                exceptionClazz = ExceptionHandlerUtil.findExceptionInSpringContext(context);
            }

            @Override
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException {
                HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
                HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
                try {
                    // Before bussiness logic
                    filterChain.doFilter(httpServletRequest, httpServletResponse);
                    // After bussiness logic

                    // If status of response is 4xx or 5xx it is a client side or response side error...
                    if (HttpStatus.resolve(httpServletResponse.getStatus()).isError() && !httpServletResponse.isCommitted()) {
                        RuntimeException runtimeException = HttpStatus.resolve(httpServletResponse.getStatus()).is4xxClientError() ?
                                new RuntimeException("Client side error") : new RuntimeException("Server side error");
                        sendErrorResponse(httpServletRequest, httpServletResponse, runtimeException);
                    }
                } catch (Throwable throwable) {
                    if (!httpServletResponse.isCommitted()) {
                        // If exception is thrown from anywhere, catching exception to convert it GlobalException which implements SingleException
                        sendErrorResponse(httpServletRequest, httpServletResponse, throwable);
                    }
                }
            }
        });
        return registrationBean;
    }

    private void sendErrorResponse(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Throwable throwable) throws IOException {
        try {
            String detailMessage = "";
            if (throwable != null) {
                // Not handled or unexcepted error = 500
                httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                // Get message
                detailMessage = throwable.getMessage();
            }
            // Initialize exception class
            Exception exception = getInstanceOfExceptionClazz();

            // If application context has generator class, then set it or use basicGenerator
            ResponseGenerator<ObjectNode> generator =
                    responseGenerator == null ? ExceptionHandlerUtil.basicGenerator() : responseGenerator;

            // Set payload
            ExceptionHandlerUtil.setPayload(exception,
                    generator.apply(httpServletRequest, httpServletResponse, new ObjectNode(prepareJsonNodeFactory()),
                            detailMessage));
            // Write response with payload
            ExceptionHandlerUtil.writeErrorResponse(httpServletResponse,
                    objectMapper.writeValueAsString(((SingleException<?>) exception).getErrorResponse()));

        } catch (InstantiationException | NoSuchMethodException e) {
            new RuntimeException("Define default constructor or implement interface (without arguments)");
        } catch (IllegalAccessException e) {
            new RuntimeException("Default constructor should be public (without arguments)");
        } catch (InvocationTargetException e) {
            new RuntimeException("Exception class has to be public");
        }
    }

    private static Exception getInstanceOfExceptionClazz() throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return exceptionClazz.getDeclaredConstructor().newInstance();
    }

    private JsonNodeFactory prepareJsonNodeFactory() {
        return objectMapper != null ? objectMapper.getNodeFactory() : new JsonNodeFactory(true);
    }

    public static Class<? extends Exception> getExceptionClazz() {
        return exceptionClazz;
    }
}
