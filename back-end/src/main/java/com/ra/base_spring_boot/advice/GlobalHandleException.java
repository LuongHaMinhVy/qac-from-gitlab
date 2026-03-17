package com.ra.base_spring_boot.advice;

import com.ra.base_spring_boot.exception.*;
import com.ra.base_spring_boot.dto.ResponseWrapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalHandleException {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<?> handleValidException(MethodArgumentNotValidException ex) {
                Map<String, String> errors = new HashMap<>();
                ex.getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                ResponseWrapper.builder()
                                                .data(errors)
                                                .code(HttpStatus.BAD_REQUEST.value())
                                                .status(HttpStatus.BAD_REQUEST)
                                                .build());
        }

        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public ResponseEntity<?> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                ResponseWrapper.builder()
                                                .data("Kích thước tệp tin vượt quá giới hạn cho phép. Vui lòng chọn tệp nhỏ hơn.")
                                                .code(HttpStatus.BAD_REQUEST.value())
                                                .status(HttpStatus.BAD_REQUEST)
                                                .build());
        }

        @ExceptionHandler(NoResourceFoundException.class)
        public ResponseEntity<?> handleNoResourceFoundException(NoResourceFoundException ex) {
                String resourcePath = ex.getResourcePath();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                                ResponseWrapper.builder()
                                                .data("Không tìm thấy tài nguyên: "
                                                                + (resourcePath != null ? resourcePath
                                                                                : "tuyến đường này"))
                                                .code(HttpStatus.NOT_FOUND.value())
                                                .status(HttpStatus.NOT_FOUND)
                                                .build());
        }

        @ExceptionHandler(UsernameNotFoundException.class)
        public ResponseEntity<?> handleUsernameNotFoundException(UsernameNotFoundException ex) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                                ResponseWrapper.builder()
                                                .data(ex.getMessage())
                                                .code(HttpStatus.NOT_FOUND.value())
                                                .status(HttpStatus.NOT_FOUND)
                                                .build());
        }

        @ExceptionHandler(HttpBadRequest.class)
        public ResponseEntity<?> handleHttpBadReqeust(HttpBadRequest ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                ResponseWrapper.builder()
                                                .data(ex.getMessage())
                                                .code(HttpStatus.BAD_REQUEST.value())
                                                .status(HttpStatus.BAD_REQUEST)
                                                .build());
        }

        @ExceptionHandler(HttpUnAuthorized.class)
        public ResponseEntity<?> handleHttpUnAuthorized(HttpUnAuthorized ex) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                                ResponseWrapper.builder()
                                                .data(ex.getMessage())
                                                .code(HttpStatus.UNAUTHORIZED.value())
                                                .status(HttpStatus.UNAUTHORIZED)
                                                .build());
        }

        @ExceptionHandler(HttpForbiden.class)
        public ResponseEntity<?> handleHttpForbiden(HttpForbiden ex) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                                ResponseWrapper.builder()
                                                .data(ex.getMessage())
                                                .code(HttpStatus.FORBIDDEN.value())
                                                .status(HttpStatus.FORBIDDEN)
                                                .build());
        }

        @ExceptionHandler(HttpNotFound.class)
        public ResponseEntity<?> handleHttpNotFound(HttpNotFound ex) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                                ResponseWrapper.builder()
                                                .data(ex.getMessage())
                                                .code(HttpStatus.NOT_FOUND.value())
                                                .status(HttpStatus.NOT_FOUND)
                                                .build());
        }

        @ExceptionHandler(HttpConflict.class)
        public ResponseEntity<?> handleHttpConflict(HttpConflict ex) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                                ResponseWrapper.builder()
                                                .data(ex.getMessage())
                                                .code(HttpStatus.CONFLICT.value())
                                                .status(HttpStatus.CONFLICT)
                                                .build());
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<?> handleAllOtherExceptions(Exception ex) {
                ex.printStackTrace(); // Log the error for debugging

                String errorMessage = ex.getMessage();
                if (errorMessage == null || errorMessage.isEmpty()) {
                        errorMessage = "Lỗi không xác định";
                }

                String detailedMessage = String.format("[%s] %s",
                                ex.getClass().getSimpleName(),
                                errorMessage);

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                ResponseWrapper.builder()
                                                .data(detailedMessage)
                                                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .build());
        }
}
