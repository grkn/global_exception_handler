# Global Exception Handler Java Library for Servlet architecture

## Purpose of library

Catch exceptions in the filter and convert it to Global Exception.

Then write response to user about error

It is a global exception handler mechanism which helps user to catch error in ANYWHERE including Controller, Service, Filters, even Spring exceptions are converted.

## Usage of Library

```
<dependency>
    <groupId>com.tgf.exception</groupId>
    <artifactId>handler</artifactId>
    <version>1.0.0</version>
</dependency>
```

1- First add dependecy to your project. Also enable library with @EnableSingleException that helps to load other related configs.

2- Just select a exception which all exceptions will be converted by simply adding @SelectedException(exception = GlobalException.class)

```
@EnableSingleException
@SelectedException(exception = GlobalException.class)
@Configuration
public class Config {
}
```

3- After you enable it. You have to create a custom exception which must implements SingleException.

```
public class GlobalException extends RuntimeException implements SingleException {

    private JsonNode errorResponse;
    private String message = "";
    
    @Override
    public JsonNode getErrorResponse() {
        return errorResponse;
    }
    @Override
    public void setErrorResponse(JsonNode jsonNode) {
        this.errorResponse = jsonNode;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
```

4- Error response is unstructured data and depend on your implementation as well. Basic response is supported as below response.

```
{
    "method": "GET",
    "path": "/tgf/data",
    "time": "2024-03-08T01:44:15.4385552",
    "msg": "Request processing failed: com.to.go.fit.exception.GlobalException: Test",
    "status": 500
}
```

 5- If you create a bean which implements ResponseGenerator then you can customize your error response.
 
```
@Component
public class CustomGenerator implements ResponseGenerator {
    @Override
    public JsonNode apply(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, JsonNode jsonNode, String message) {
        // you can return Array or object or whatever which will be converted to json.
    }
}
```





