package es.pongo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import es.pongo.exception.ServiceException;

public class GenericController {
	
    private static final Logger LOG = LoggerFactory.getLogger(GenericController.class.toString());

    private static final String LOG_MESSAGE = "Request Exception"; 
	private static final String VALIDATION_MESSAGE = "Validation error";
	private static final String INSUFFICIENT_PERMISSIONS = "Insufficient permissions";
	
	@ExceptionHandler(value = Exception.class)
    public ResponseEntity<Response> error(Exception exception) {
        Response respuesta = new Response(HttpStatus.INTERNAL_SERVER_ERROR, exception);
        LOG.error(LOG_MESSAGE, exception);

        return new ResponseEntity<Response>(respuesta, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = ServiceException.class)
    public ResponseEntity<Response> error(ServiceException exception) {
        Response respuesta = new Response(exception.getStatus(), exception);
        return new ResponseEntity<Response>(respuesta, exception.getStatus());
    }
    
    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<Response> error(AccessDeniedException exception) {
        Response respuesta = new Response(HttpStatus.FORBIDDEN, exception, INSUFFICIENT_PERMISSIONS);
        return new ResponseEntity<Response>(respuesta, HttpStatus.FORBIDDEN);
    }
    
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<Response> validation(MethodArgumentNotValidException exception){
        Response response = new Response(HttpStatus.BAD_REQUEST, exception, VALIDATION_MESSAGE);
    	for(ObjectError error : exception.getBindingResult().getAllErrors()){
    		response.addFieldError(error.getObjectName(), error.getCode());
    	}
    	
        return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
    }
}
