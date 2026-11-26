package cz.gastro.dto;

public record LoginRequest(
        String username,
        String password
) {}