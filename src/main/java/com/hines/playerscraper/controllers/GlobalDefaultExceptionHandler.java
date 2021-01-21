package com.hines.playerscraper.controllers;

import com.hines.playerscraper.entities.ErrorDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.Date;

/**
 * The purpose of this class is to handle any unexpected error/exception that may occur during processing. This class is responsible for receiving
 * that error/exception and transforming it into an {@link ErrorDetail} that will be returned as the HTTP response of the controller.
 */
@ControllerAdvice
public class GlobalDefaultExceptionHandler
{

    private static Logger logger = LoggerFactory.getLogger(GlobalDefaultExceptionHandler.class.getName());

    /**
     * Handle any generic {@link Exception}s that occur and return an {@link ErrorDetail} that contains the details of the error.
     *
     * @param req the current request object
     * @param e   the thrown exception
     * @return {@link ErrorDetail} detailing the error that occurred
     * @throws Exception
     */
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "An application error occurred")
    @ExceptionHandler(value = {Exception.class, RuntimeException.class})
    public @ResponseBody
    ErrorDetail defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception
    {
        // If the exception has already been annotated to handle a response
        // (@ResponseStatus), let it do so.
        if (isExceptionMappedToResponse(e))
        {
            logger.warn("[Response Exception Thrown]", e);
            throw e;
        }

        // Otherwise setup and send the user to a default error-view.
        ErrorDetail error = new ErrorDetail();
        error.setPath(encodeUrl(req.getRequestURL()));
        error.setMessage("An error occurred, please check the application logs.");
        error.setTimestamp(new Date());
        error.setError(e.getMessage());
        error.setException(e.toString());

        logException(e);

        return error;
    }

    /**
     * Encode the URL before returning it so that it cannot be used in an XSS attack when displayed to the user on the website.
     *
     * @param url
     * @return
     */
    private String encodeUrl(StringBuffer url)
    {
        Base64.Encoder encoder = Base64.getUrlEncoder();

        return encoder.encodeToString(url.toString().getBytes());
    }

    /**
     * Map any IllegalArgumentException to a 400.
     *
     * @param response
     * @throws IOException
     */
    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Invalid arguments provided")
    @ExceptionHandler(IllegalArgumentException.class)
    public @ResponseBody
    ErrorDetail handleBadRequests(HttpServletResponse response, Exception e) throws IOException
    {
        ErrorDetail error = new ErrorDetail();
        error.setMessage("Invalid arguments provided.");
        error.setTimestamp(new Date());
        error.setError(e.getMessage());
        error.setException(e.toString());

        logException(e);

        return error;
    }

    /**
     * Returns true if the provided exception is already annotated to handle a response (@ResponseStatus)
     *
     * @param e
     * @return
     */
    private boolean isExceptionMappedToResponse(Exception e)
    {
        return AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null;
    }

    /**
     * Output the exception to the logs.
     *
     * @param e
     */
    private void logException(Exception e)
    {
        logger.error("An application error occurred: ", e);
    }
}