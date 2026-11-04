package com.campuspath.backend.common;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CodeMismatchException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ExpiredCodeException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidParameterException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidPasswordException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.LimitExceededException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.TooManyRequestsException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotConfirmedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApi(ApiException ex) {

        return ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Some fields are invalid");
        pd.setProperty("errors", errors);
        return pd;
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleBadJson(HttpMessageNotReadableException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request body is missing or not valid JSON");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleIntegrity(DataIntegrityViolationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Duplicate email or EUID");
    }

    @ExceptionHandler(CognitoIdentityProviderException.class)
    public ProblemDetail handleCognito(CognitoIdentityProviderException ex) {
        HttpStatus status;
        String detail;

        if (ex instanceof NotAuthorizedException || ex instanceof UserNotFoundException) {
            status = HttpStatus.UNAUTHORIZED;
            detail = "Incorrect email or password.";
        } else if (ex instanceof UserNotConfirmedException) {
            status = HttpStatus.UNAUTHORIZED;
            detail = "Please verify your email before logging in.";
        } else if (ex instanceof UsernameExistsException) {
            status = HttpStatus.CONFLICT;
            detail = "An account with this email already exists.";
        } else if (ex instanceof CodeMismatchException) {
            status = HttpStatus.BAD_REQUEST;
            detail = "That verification code is incorrect.";
        } else if (ex instanceof ExpiredCodeException) {
            status = HttpStatus.BAD_REQUEST;
            detail = "That verification code has expired. Request a new one.";
        } else if (ex instanceof InvalidPasswordException) {
            status = HttpStatus.BAD_REQUEST;
            detail = "Password does not meet the requirements: at least 8 characters, including a special character.";
        } else if (ex instanceof LimitExceededException || ex instanceof TooManyRequestsException) {
            status = HttpStatus.TOO_MANY_REQUESTS;
            detail = "Too many attempts. Please wait a few minutes and try again.";
        } else if (ex instanceof InvalidParameterException) {
            status = HttpStatus.BAD_REQUEST;
            detail = "Some of the details you entered are not valid.";
        } else {
            log.error("Unexpected Cognito error", ex);
            status = HttpStatus.BAD_GATEWAY;
            detail = "The login service had a problem. Please try again.";
        }
        return ProblemDetail.forStatusAndDetail(status, detail);
    }
}