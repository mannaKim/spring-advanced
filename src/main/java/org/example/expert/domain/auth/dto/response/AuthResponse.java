package org.example.expert.domain.auth.dto.response;

import lombok.Getter;

@Getter
public class AuthResponse {

    private final String bearerToken;

    public AuthResponse(String bearerToken) {
        this.bearerToken = bearerToken;
    }
}
