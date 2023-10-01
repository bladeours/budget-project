package com.budget.project.model.dto.response;

import lombok.Builder;

@Builder(toBuilder = true)
public record AuthenticationResponse(String token) {}
