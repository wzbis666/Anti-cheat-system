package com.anticheat.backend.repository;

import com.anticheat.backend.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    
    List<Report> findByStatus(String status);
    
    List<Report> findByReporterUuid(String reporterUuid);
    
    List<Report> findByReportedUuid(String reportedUuid);
    
    @Query("SELECT r FROM Report r ORDER BY r.reportTime DESC")
    List<Report> findAllOrderByTimeDesc();
    
    @Query("SELECT COUNT(r) FROM Report r WHERE r.status = :status")
    long countByStatus(@Param("status") String status);
}
