package com.anticheat.backend.service;

import com.anticheat.backend.model.Report;
import com.anticheat.backend.repository.ReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    @Autowired
    private ReportRepository reportRepository;

    public List<Report> getAll() {
        return reportRepository.findAllOrderByTimeDesc();
    }

    public List<Report> getPending() {
        return reportRepository.findByStatus("PENDING");
    }

    public Optional<Report> getById(Long id) {
        return reportRepository.findById(id);
    }

    public long getPendingCount() {
        return reportRepository.countByStatus("PENDING");
    }

    @Transactional
    public Report create(String reporterName, String reporterUuid, String reportedName, 
                         String reportedUuid, String reason, String reportType) {
        Report report = new Report();
        report.setReporterName(reporterName);
        report.setReporterUuid(reporterUuid);
        report.setReportedName(reportedName);
        report.setReportedUuid(reportedUuid);
        report.setReason(reason);
        report.setReportType(reportType != null ? reportType : "CHEATING");
        report.setReportTime(System.currentTimeMillis());
        report.setStatus("PENDING");
        reportRepository.save(report);
        logger.info("新举报: {} 举报 {} - {}", reporterName, reportedName, reason);
        return report;
    }

    @Transactional
    public void handle(Long id, String handledBy, String status, String result) {
        Optional<Report> optionalReport = reportRepository.findById(id);
        if (optionalReport.isPresent()) {
            Report report = optionalReport.get();
            report.setStatus(status);
            report.setHandledBy(handledBy);
            report.setHandledTime(System.currentTimeMillis());
            report.setResult(result);
            reportRepository.save(report);
            logger.info("举报已处理: ID={}, 状态={}, 处理人={}", id, status, handledBy);
        }
    }

    @Transactional
    public void delete(Long id) {
        reportRepository.deleteById(id);
        logger.info("删除举报记录: {}", id);
    }
}
