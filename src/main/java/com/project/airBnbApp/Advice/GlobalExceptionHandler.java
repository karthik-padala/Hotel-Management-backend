package com.project.airBnbApp.Advice;


import com.project.airBnbApp.exception.ResourceNotFoundException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFound(ResourceNotFoundException exception){
        ApiError apiError =ApiError.builder()
                .status(HttpStatus.NOT_FOUND)
                .message(exception.getMessage())
                .build();
        return buildErrorResponseEntity(apiError);


    }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<?>> HandleAuthenticationException(AuthenticationException ex){
        ApiError apiError =ApiError.builder()
                .status(HttpStatus.UNAUTHORIZED)
                .message(ex.getMessage())
                .build();
        return buildErrorResponseEntity(apiError);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<?>> HandleJwtException(JwtException ex){
        ApiError apiError =ApiError.builder()
                .status(HttpStatus.UNAUTHORIZED)
                .message(ex.getMessage())
                .build();
        return buildErrorResponseEntity(apiError);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> HandleAccessDeniedException(AccessDeniedException ex){
        ApiError apiError =ApiError.builder()
                .status(HttpStatus.FORBIDDEN)
                .message(ex.getMessage())
                .build();
        return buildErrorResponseEntity(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleInternalServerError(Exception exception){
      ApiError apiError=ApiError.builder()
              .status(HttpStatus.INTERNAL_SERVER_ERROR)
              .message(exception.getMessage())
              .build();
              return buildErrorResponseEntity(apiError);


    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public  ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception){
        List<String> errors= exception.getBindingResult()
                .getAllErrors()
                .stream()
                .map(fieldError->fieldError.getDefaultMessage())
                .collect(Collectors.toList());
        ApiError apiError= ApiError.builder()
                .message("Input field validation Error")
                .subErrors(errors)
                .status(HttpStatus.BAD_REQUEST)
                .build();
        return buildErrorResponseEntity(apiError);

    }

    private ResponseEntity<ApiResponse<?>> buildErrorResponseEntity(ApiError apiError){
        return new ResponseEntity<>(new ApiResponse<>(apiError),apiError.getStatus());
    }






}
