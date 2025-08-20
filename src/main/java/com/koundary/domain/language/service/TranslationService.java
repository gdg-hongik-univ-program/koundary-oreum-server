package com.koundary.domain.language.service;

import com.koundary.domain.language.entity.Language;
import com.koundary.domain.language.entity.Translation;
import com.koundary.domain.language.repository.TranslationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TranslationService {

    private final TranslationRepository translationRepository;
    private final DeepLTranslationClient deepL;

    /**
     * 캐시 우선: 없으면 DeepL 번역 후 저장
     */
    @Transactional
    public String translateAndCache(String targetType, Long targetId, String field,
                                    String original, Language targetLang) {
        if (original == null || original.isBlank()) return original;

        return translationRepository
                .findByTargetTypeAndTargetIdAndFieldAndTargetLanguage(targetType, targetId, field, targetLang)
                .map(Translation::getTranslatedText)
                .orElseGet(() -> {
                    String translated = deepL.translate(original, targetLang);
                    Translation t = new Translation(targetType, targetId, field, targetLang, translated);
                    t.setUpdatedAt(Instant.now());
                    translationRepository.save(t);
                    return translated;
                });
    }
}
