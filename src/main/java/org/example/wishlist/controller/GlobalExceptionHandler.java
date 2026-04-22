package org.example.wishlist.controller;

import org.example.wishlist.exception.*;
import org.example.wishlist.model.User;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthenticatedException.class)
    public String handleUnauthorized() {
        return "redirect:/wishlist/login";
    }

    @ExceptionHandler(InvalidInputException.class)
    public String handleInvalidInput(Model model, InvalidInputException ex) {
        model.addAttribute("status", 400);
        model.addAttribute("message", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public String handleInvalidCredentials(Model model, InvalidCredentialsException ex) {
        model.addAttribute("status", 401);
        model.addAttribute("message", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(ForbiddenAccessException.class)
    public String handleForbidden(Model model, ForbiddenAccessException ex) {
        model.addAttribute("status", 403);
        model.addAttribute("message", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public String handleUserNotFound(Model model) {
        model.addAttribute("status", 404);
        model.addAttribute("message", "Resource not found");
        return "error";
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public String handleUserAlreadyExists(Model model) {
        model.addAttribute("registrationError", true);
        model.addAttribute("user", new User());
        return "register-page";
    }

    @ExceptionHandler(DataAccessException.class)
    public String handleDatabaseError(Model model, DataAccessException ex) {
        model.addAttribute("status", 500);
        model.addAttribute("message", "Database error occurred");
        return "error";
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleGenericError(Model model, RuntimeException ex) {
        model.addAttribute("status", 500);
        model.addAttribute("message", ex.getMessage() != null ? ex.getMessage() : "Unexpected error");
        return "error";
    }
}