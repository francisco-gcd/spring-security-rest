package es.pongo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import es.pongo.exception.ServiceException;

public class GenericController {
	
    private static final Logger LOG = LoggerFactory.getLogger(GenericController.class.toString());

    private static final String LOG_MESSAGE = "Request Exception"; 
	private static final String VALIDATION_MESSAGE = "Validation error"; 
	
	@ExceptionHandler(value = Exception.class)
    public ResponseEntity<Response> error(Exception exception) {
        Response respuesta = new Response(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        LOG.error(LOG_MESSAGE, exception);

        return new ResponseEntity<Response>(respuesta, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = ServiceException.class)
    public ResponseEntity<Response> error(ServiceException exception) {
        Response respuesta = new Response(exception.getStatus(), exception.getMessage());
        return new ResponseEntity<Response>(respuesta, exception.getStatus());
    }
    
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<Response> validation(MethodArgumentNotValidException exception){
        Response response = new Response(HttpStatus.BAD_REQUEST, VALIDATION_MESSAGE);
    	for(ObjectError error : exception.getBindingResult().getAllErrors()){
    		response.addFieldError(error.getObjectName(), error.getCode());
    	}
    	
        return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
    }
}
