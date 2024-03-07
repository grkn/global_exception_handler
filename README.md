# Global Exception Handler Java Library for Servlet architecture

## Purpose of library

Catch exceptions in the filter and convert it to Global Exception.

Then write response to user about error

It is a global exception handler mechanism which helps user to catch error in ANYWHERE including Controller, Service, Filters, even Spring exceptions are converted.

## Usage of Library

'''
@EnableSingleException
@SelectedException(exception = GlobalException.class)
public class Config {
  //...
}


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
'''


