package com.koundary.domain.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReportRequest {
    @NotNull
    private Long postId;

    @NotBlank
    private String reason;
}
