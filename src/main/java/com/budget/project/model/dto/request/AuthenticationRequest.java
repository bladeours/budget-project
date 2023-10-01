package com.budget.project.model.dto.request;

import lombok.Builder;

@Builder(toBuilder = true)
public record AuthenticationRequest(String email, String password) {}
