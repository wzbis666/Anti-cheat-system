package com.anticheat.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class ReportHandleRequest {

    @NotBlank(message = "处理人不能为空")
    private String handledBy;

    @NotBlank(message = "状态不能为空")
    private String status;

    @NotBlank(message = "处理结果不能为空")
    private String result;

    public String getHandledBy() { return handledBy; }
    public void setHandledBy(String handledBy) { this.handledBy = handledBy; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
}
