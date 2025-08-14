package com.koundary.domain.language.entity;

/**
 * 지원 언어 목록
 * code → 번역 API에서 사용하는 언어 코드
 */
public enum Language {
    KO("ko", "Korean"),
    EN("en", "English");

    private final String code;      // API나 Locale에서 사용할 코드
    private final String displayName; // 사람이 읽기 좋은 이름

    Language(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }
}
