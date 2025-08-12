package com.koundary.domain.report.service;

import com.koundary.domain.post.exception.PostNotFoundException;
import com.koundary.domain.post.repository.PostRepository;
import com.koundary.domain.report.dto.ReportRequest;
import com.koundary.domain.report.entity.Report;
import com.koundary.domain.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;

    @Transactional
    public void report(Long requesterId, ReportRequest req) {
        // 존재하는 게시글인지 검증
        if (!postRepository.existsById(req.getPostId())) {
            throw new IllegalArgumentException("존재하지 않는 게시글입니다.");
        }

        // 중복 신고 방지
        if (reportRepository.existsByReporterIdAndPostId(requesterId, req.getPostId())) {
            throw new IllegalStateException("이미 신고한 게시글입니다.");
        }

        // 저장
        Report report = Report.builder()
                .reporterId(requesterId)
                .postId(req.getPostId())
                .reason(req.getReason().trim())
                .build();

        reportRepository.save(report);
    }

    public int countReports(Long postId) {
        return reportRepository.countByPostId(postId);
    }
}
