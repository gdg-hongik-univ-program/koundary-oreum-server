package com.koundary.domain.report.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "report",
        uniqueConstraints = {
                // 같은 사용자가 같은 게시글에 대해 한 번만 신고 가능
                @UniqueConstraint(name = "uk_report_once", columnNames = {"reporter_id", "post_id"})
        },
        indexes = {
                @Index(name = "idx_report_post", columnList = "post_id")
        }
)
public class Report {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    // 신고한 사용자 ID
    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    // 신고 대상 게시글 ID
    @Column(name = "post_id", nullable = false)
    private Long postId;

    // 신고 사유
    @Column(name = "reason", length = 2000, nullable = false)
    private String reason;

    // 생성 시각
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
}
