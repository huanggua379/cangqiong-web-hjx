package com.sky.exception;

/**
 * 文件校验或上传失败。
 */
public class FileUploadException extends BaseException {

    public FileUploadException(String message) {
        super(message);
    }

    public FileUploadException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
