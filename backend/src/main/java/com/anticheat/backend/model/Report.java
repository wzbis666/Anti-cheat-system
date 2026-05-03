package com.anticheat.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "reports")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "reporter_name", nullable = false)
    private String reporterName;
    
    @Column(name = "reporter_uuid")
    private String reporterUuid;
    
    @Column(name = "reported_name", nullable = false)
    private String reportedName;
    
    @Column(name = "reported_uuid")
    private String reportedUuid;
    
    @Column(name = "reason", nullable = false)
    private String reason;
    
    @Column(name = "report_type")
    private String reportType;
    
    @Column(name = "report_time", nullable = false)
    private long reportTime;
    
    @Column(name = "status", nullable = false)
    private String status = "PENDING";
    
    @Column(name = "handled_by")
    private String handledBy;
    
    @Column(name = "handled_time")
    private Long handledTime;
    
    @Column(name = "result")
    private String result;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getReporterUuid() {
        return reporterUuid;
    }

    public void setReporterUuid(String reporterUuid) {
        this.reporterUuid = reporterUuid;
    }

    public String getReportedName() {
        return reportedName;
    }

    public void setReportedName(String reportedName) {
        this.reportedName = reportedName;
    }

    public String getReportedUuid() {
        return reportedUuid;
    }

    public void setReportedUuid(String reportedUuid) {
        this.reportedUuid = reportedUuid;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public long getReportTime() {
        return reportTime;
    }

    public void setReportTime(long reportTime) {
        this.reportTime = reportTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHandledBy() {
        return handledBy;
    }

    public void setHandledBy(String handledBy) {
        this.handledBy = handledBy;
    }

    public Long getHandledTime() {
        return handledTime;
    }

    public void setHandledTime(Long handledTime) {
        this.handledTime = handledTime;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
