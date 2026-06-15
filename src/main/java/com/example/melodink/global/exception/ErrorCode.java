package com.example.melodink.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Permission
    PERMISSION_DENIED(HttpStatus.FORBIDDEN, "해당 작업을 수행할 권한이 없습니다."),

    // S3
    S3_ERROR(HttpStatus.FORBIDDEN, "이미지 업로드 중 오류가 발생했습니다."),

    // EMAIL
    EMAIL_SENDING_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 전송에 실패했습니다."),
    EMAIL_TEMPLATE_LOAD_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 템플릿 로드에 실패했습니다."),
    EMAIL_VERIFY_FAILURE(HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않습니다."),

    // USER
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 회원입니다."),
    USER_ALREADY_REGISTERED(HttpStatus.BAD_REQUEST, "이미 가입된 회원입니다."),
    USER_INVALID_ACCESS(HttpStatus.BAD_REQUEST, "잘못된 유저의 접근입니다."),

    // COMMUNITY - POST
    POST_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 게시글입니다."),
    POST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 게시글에 접근할 권한이 없습니다."),
    POST_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 게시글입니다."),
    POST_CONTENT_EMPTY(HttpStatus.BAD_REQUEST, "게시글 내용을 입력해주세요."),
    POST_TITLE_EMPTY(HttpStatus.BAD_REQUEST, "게시글 제목을 입력해주세요."),
    POST_TITLE_TOO_LONG(HttpStatus.BAD_REQUEST, "게시글 제목이 너무 깁니다."),
    POST_CONTENT_TOO_LONG(HttpStatus.BAD_REQUEST, "게시글 내용이 너무 깁니다."),

    // COMMUNITY - COMMENT
    COMMENT_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 댓글입니다."),
    COMMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 댓글에 접근할 권한이 없습니다."),
    COMMENT_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 댓글입니다."),
    COMMENT_CONTENT_EMPTY(HttpStatus.BAD_REQUEST, "댓글 내용을 입력해주세요."),
    COMMENT_CONTENT_TOO_LONG(HttpStatus.BAD_REQUEST, "댓글 내용이 너무 깁니다."),

    // COMMUNITY - REPLY (대댓글)
    REPLY_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 답글입니다."),
    REPLY_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 답글에 접근할 권한이 없습니다."),
    REPLY_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 답글입니다."),
    REPLY_CONTENT_EMPTY(HttpStatus.BAD_REQUEST, "답글 내용을 입력해주세요."),
    REPLY_CONTENT_TOO_LONG(HttpStatus.BAD_REQUEST, "답글 내용이 너무 깁니다."),

    // COMMUNITY - LIKE
    LIKE_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 좋아요를 누른 게시글입니다."),
    LIKE_NOT_FOUND(HttpStatus.BAD_REQUEST, "좋아요 정보가 존재하지 않습니다."),

    // COMMUNITY - BOOKMARK (추후 추가 시)
    BOOKMARK_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 저장한 게시글입니다."),
    BOOKMARK_NOT_FOUND(HttpStatus.BAD_REQUEST, "저장한 게시글이 아닙니다."),

    // FOLLOW
    SELF_FOLLOW_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "자기 자신을 팔로우할 수 없습니다."),
    FOLLOW_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 팔로우한 사용자입니다."),
    FOLLOW_NOT_FOUND(HttpStatus.BAD_REQUEST, "팔로우 관계가 존재하지 않습니다."),


    // NOTIFICATION
    NOTIFICATION_ID_IS_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 알림id 입니다."),
    NOTIFICATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 알림입니다."),
    NOTIFICATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 알림에 접근할 권한이 없습니다."),

    // REPLY
    REPLY_ID_IS_INVALID(HttpStatus.BAD_REQUEST, "잘못된 replyId 입니다."),

    // FREEBOARD
    FREE_BOARD_ID_IS_INVALID(HttpStatus.BAD_REQUEST, "잘못된 freeBoardId 입니다."),

    // FREEBOARDREPLY
    FREE_BOARD_REPLY_ID_IS_INVALID(HttpStatus.BAD_REQUEST, "잘못된 freeBoardReplyId 입니다."),

    // AUTH
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 틀렸습니다."),

    // myPage
    CURRENT_PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다."),
    NEW_PASSWORD_SAME_AS_OLD(HttpStatus.BAD_REQUEST, "새 비밀번호가 기존 비밀번호와 동일합니다."),
    NEW_PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "변경을 위해 입력하신 비밀번호와 다릅니다."),
    EMAIL_NOT_MATCH(HttpStatus.BAD_REQUEST, "이메일이 일치하지 않습니다"),
    NICKNAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "중복된 닉네임입니다."),
    INTRODUCTION_TOO_LONG(HttpStatus.BAD_REQUEST, "소개는 20자 내로 작성해주세요."),
    NICKNAME_CHANGE_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "닉네임 변경 불가기간(6개월)이 지나지 않았습니다."),

    // 5xx
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 에러");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
