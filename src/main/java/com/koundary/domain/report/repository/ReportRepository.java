package com.koundary.domain.report.repository;

import com.koundary.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByReporterIdAndPostId(Long reporterId, Long postId);

    int countByPostId(Long postId);
}
