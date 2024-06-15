package com.creativepool.exception;

import com.creativepool.models.ApiException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiException> handleException(BadRequestException ex) {
        ApiException apiException=new ApiException(ex.getMessage(), HttpStatus.BAD_REQUEST);
        return  new ResponseEntity<>(apiException,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiException> handleException(DataIntegrityViolationException ex) {
        ApiException apiException=new ApiException(ex.getMessage(), HttpStatus.CONFLICT);
        return  new ResponseEntity<>(apiException,HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CreativePoolException.class)
    public ResponseEntity<ApiException> handleException(CreativePoolException ex) {
        ApiException apiException=new ApiException(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        return  new ResponseEntity<>(apiException,HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
