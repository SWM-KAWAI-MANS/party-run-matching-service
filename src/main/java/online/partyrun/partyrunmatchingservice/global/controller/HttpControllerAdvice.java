package online.partyrun.partyrunmatchingservice.global.controller;

import lombok.extern.slf4j.Slf4j;

import online.partyrun.partyrunmatchingservice.global.dto.MessageResponse;
import online.partyrun.partyrunmatchingservice.global.exception.BadRequestException;
import online.partyrun.partyrunmatchingservice.global.exception.InternalServerException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
public class HttpControllerAdvice {
    private static final String BAD_REQUEST_MESSAGE = "잘못된 요청입니다.";
    private static final String SERVER_ERROR_MESSAGE = "알 수 없는 에러입니다.";

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<MessageResponse> handleBadRequestException(BadRequestException exception) {
        log.warn(exception.getMessage());
        return Mono.just(new MessageResponse(BAD_REQUEST_MESSAGE));
    }

    @ExceptionHandler(InternalServerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public MessageResponse handleInternalServerException(InternalServerException exception) {
        log.warn(exception.getMessage());
        return new MessageResponse(SERVER_ERROR_MESSAGE);
    }
}
