package com.budget.project.auth.model.dto;

import lombok.Builder;

@Builder(toBuilder = true)
public record AuthenticationResponse(String jwt) {}
