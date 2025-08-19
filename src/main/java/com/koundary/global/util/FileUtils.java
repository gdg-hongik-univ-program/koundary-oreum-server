package com.koundary.global.util;

import com.koundary.domain.language.util.NationalityLanguageMapper;

public class FileUtils {

    /**
     * 파일명에서 확장자를 추출 (예: "image.png" -> "png")
     */
    public static String getExtension(String filename) {
        if (filename == null) return null;
        int lastDot = filename.lastIndexOf('.');
        return (lastDot == -1) ? "" : filename.substring(lastDot + 1);
    }

    /**
     * 파일 이름을 안전하게 변환 (공백, 특수문자 제거)
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null) return null;
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    /**
     * 국가코드를 정규화 (예: "대한민국" -> "KR")
     */
    public static String canonicalizeNationality(String rawNationality) {
        return NationalityLanguageMapper.canonicalize(rawNationality);
    }

    /**
     * 국가에 따른 기본 언어 반환
     */
    public static String getDefaultLanguageByNationality(String nationality) {
        return NationalityLanguageMapper.defaultLanguageOf(
                NationalityLanguageMapper.canonicalize(nationality)
        ).name();
    }
}
