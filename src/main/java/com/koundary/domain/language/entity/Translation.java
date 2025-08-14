package com.koundary.domain.language.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter @Setter
@NoArgsConstructor
@Entity
@Table(name = "translation",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"target_type","target_id","field","target_language"}
        ))
public class Translation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 무엇을 번역했는지(ex:POST, COMMENT) */
    @Column(name = "target_type", nullable = false, length = 16)
    private String targetType;

    /** 대상 ID(ex:게시글ID/댓글ID) */
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    /** 어느 필드인지(ex:title, content) */
    @Column(name = "field", nullable = false, length = 32)
    private String field;

    /** 번역 대상 언어 */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_language", nullable = false, length = 5)
    private Language targetLanguage;

    /** 번역된 결과 */
    @Lob
    @Column(name = "translated_text", nullable = false)
    private String translatedText;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Translation(String targetType, Long targetId, String field,
                       Language targetLanguage, String translatedText) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.field = field;
        this.targetLanguage = targetLanguage;
        this.translatedText = translatedText;
        this.updatedAt = Instant.now();
    }
}
