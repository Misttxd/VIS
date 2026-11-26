package cz.rest.dto;

public record LoginRequest(
        String username,
        String password
) {}