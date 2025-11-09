package com.ex.unduckauthservice.domain.jwt.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenRequestDTO {

    private String refreshToken;
}
