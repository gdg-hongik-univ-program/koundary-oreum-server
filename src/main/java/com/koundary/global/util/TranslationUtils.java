package com.koundary.global.util;

import com.koundary.domain.language.entity.Language;
import com.koundary.domain.language.util.NationalityLanguageMapper;
import org.springframework.stereotype.Component;

@Component
public class TranslationUtils {

    /**
     * 주어진 문장을 지정한 언어로 번역 (현재는 더미 로직)
     * @param text 원문
     * @param targetLanguage 목표 언어 (Language enum)
     * @return 번역된 문장
     */
    public String translate(String text, Language targetLanguage) {
        if (text == null || text.isBlank()) {
            return text;
        }
        // 실제 API 연동 시 이 부분 교체
        return "[번역:" + targetLanguage.name() + "] " + text;
    }

    /**
     * 국가 코드(nationality)에 맞춰 번역
     * @param text 원문
     * @param nationality 국가 코드(예: "KR", "US", "GB")
     * @return 번역된 문장
     */
    public String translateByNationality(String text, String nationality) {
        String canonical = NationalityLanguageMapper.canonicalize(nationality);
        Language lang = NationalityLanguageMapper.defaultLanguageOf(canonical);
        return translate(text, lang);
    }
}
