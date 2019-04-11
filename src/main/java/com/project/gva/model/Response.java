package com.project.gva.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response <T> {

    private HttpStatus status;
    private String message;
    private T content;

    public Response<T> of(T content){
        return new Response<>(HttpStatus.SUCCESS, "Successful", content);
    }
    public enum HttpStatus{
        NOT_FOUND(404),
        SUCCESS(200),
        BAD_REQUEST(400),
        UNAUTHORIZED(401),
        FORBIDDEN(403),
        SERVER_UNAVAILABLE(503);

        private int code;

        public int code(){
            return code;
        }

        HttpStatus(int code){
            this.code = code;
        }
    }
}
