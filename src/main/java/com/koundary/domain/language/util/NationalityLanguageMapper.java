package com.koundary.domain.language.util;

import com.koundary.domain.language.entity.Language;

public final class NationalityLanguageMapper {

    private NationalityLanguageMapper() {}

    /**
     * 국가명(nationality) 문자열을 정규화 (KR/US/GB)
     */
    public static String canonicalize(String raw) {
        if (raw == null) return "KR"; // 기본값: 한국
        String s = raw.trim().toUpperCase();
        return switch (s) {
            case "KR", "KOREA", "SOUTH KOREA", "대한민국", "한국" -> "KR";
            case "US", "USA", "UNITED STATES", "미국" -> "US";
            case "GB", "UK", "UNITED KINGDOM", "영국" -> "GB";
            default -> "KR"; // 범위를 세 나라로 제한
        };
    }

    /**
     * 정규화된 nationality → 기본 언어 매핑
     */
    public static Language defaultLanguageOf(String canonicalNationality) {
        return ("US".equals(canonicalNationality) || "GB".equals(canonicalNationality))
                ? Language.EN
                : Language.KO;
    }
}
