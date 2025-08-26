package com.koundary.domain.language.service;

import com.koundary.domain.language.entity.Language;
import com.koundary.domain.language.entity.Translation;
import com.koundary.domain.language.repository.TranslationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
// ✅ [수정] @RequiredArgsConstructor 어노테이션을 삭제하여 생성자 중복 문제를 해결합니다.
public class TranslationService {

    private final TranslationRepository translationRepository;
    private final DeepLTranslationClient deepL;
    private final TranslationService self;

    // @Lazy를 사용한 생성자 주입은 그대로 유지합니다.
    public TranslationService(TranslationRepository translationRepository, DeepLTranslationClient deepL, @Lazy TranslationService self) {
        this.translationRepository = translationRepository;
        this.deepL = deepL;
        this.self = self;
    }

    @Transactional(readOnly = true)
    public String translateAndCache(String targetType, Long targetId, String field,
                                    String original, Language targetLang) {
        if (original == null || original.isBlank()) {
            return original;
        }

        Optional<Translation> cached = translationRepository
                .findByTargetTypeAndTargetIdAndFieldAndTargetLanguage(targetType, targetId, field, targetLang);

        if (cached.isPresent()) {
            return cached.get().getTranslatedText();
        }

        String translated = deepL.translate(original, targetLang);

        if (!original.equals(translated)) {
            self.saveTranslation(targetType, targetId, field, targetLang, translated);
        }

        return translated;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveTranslation(String targetType, Long targetId, String field,
                                Language targetLang, String translatedText) {
        Translation newTranslation = new Translation(targetType, targetId, field, targetLang, translatedText);
        newTranslation.setUpdatedAt(Instant.now());
        translationRepository.save(newTranslation);
    }
}