package com.budget.project.model.dto;

import lombok.Builder;

@Builder(toBuilder = true)
public record AuthenticationResponse(String token) {
}

