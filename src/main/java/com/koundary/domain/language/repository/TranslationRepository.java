package com.koundary.domain.language.repository;

import com.koundary.domain.language.entity.Language;
import com.koundary.domain.language.entity.Translation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TranslationRepository extends JpaRepository<Translation, Long> {
    Optional<Translation> findByTargetTypeAndTargetIdAndFieldAndTargetLanguage(
            String targetType, Long targetId, String field, Language targetLanguage
    );
}
