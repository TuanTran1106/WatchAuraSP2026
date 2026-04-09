package com.example.watchaura.exception;

/**
 * 权限不足异常
 * 当用户尝试访问没有权限的资源时抛出
 */
public class AccessDeniedException extends RuntimeException {

    private String message;
    private String messageVi;

    public AccessDeniedException(String message) {
        super(message);
        this.message = message;
        this.messageVi = "Bạn không có quyền truy cập trang này!";
    }

    public AccessDeniedException(String message, String messageVi) {
        super(message);
        this.message = message;
        this.messageVi = messageVi;
    }

    public String getMessageVi() {
        return messageVi;
    }

    public void setMessageVi(String messageVi) {
        this.messageVi = messageVi;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
