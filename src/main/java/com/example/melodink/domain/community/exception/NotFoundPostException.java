package com.example.melodink.domain.community.exception;

import com.example.melodink.global.exception.ApplicationException;
import com.example.melodink.global.exception.ErrorCode;

public class NotFoundPostException extends ApplicationException {
    private static final ErrorCode ERROR_CODE = ErrorCode.POST_NOT_FOUND;

    public NotFoundPostException() {
      super(ERROR_CODE);
    }

}
