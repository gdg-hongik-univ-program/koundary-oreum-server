package com.koundary.domain.language.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.koundary.domain.language.config.DeepLProperties;
import com.koundary.domain.language.entity.Language;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeepLTranslationClient {

    private final RestTemplate restTemplate;
    private final DeepLProperties props;

    /**
     * 단건 번역 (DeepL이 소스언어 자동감지)
     */
    public String translate(String text, Language targetLang) {
        if (text == null || text.isBlank()) return text;

        // DeepL target_lang 매핑
        String target = mapToDeepLTarget(targetLang); // "KO" or "EN"
        if (target == null) return text;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set(HttpHeaders.AUTHORIZATION, "DeepL-Auth-Key " + props.getAuthKey());
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setAcceptCharset(List.of(StandardCharsets.UTF_8));

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("text", text);
        form.add("target_lang", target);
        // 옵션: 문장 분할/형식 보존
        form.add("split_sentences", "1");
        form.add("preserve_formatting", "1");

        HttpEntity<MultiValueMap<String, String>> req = new HttpEntity<>(form, headers);

        try {
            ResponseEntity<DeepLResponse> res = restTemplate.exchange(
                    props.getApiUrl(),
                    HttpMethod.POST,
                    req,
                    DeepLResponse.class
            );
            DeepLResponse body = res.getBody();
            if (body != null && body.translations != null && !body.translations.isEmpty()) {
                return body.translations.get(0).text;
            }
            return text;
        } catch (HttpStatusCodeException e) {
            // 429, 456(쿼터 초과) 등 에러 로깅
            log.warn("DeepL translate error: status={} body={}", e.getStatusCode(), e.getResponseBodyAsString());
            return text; // 실패 시 원문 반환
        } catch (Exception e) {
            log.warn("DeepL translate error: {}", e.toString());
            return text;
        }
    }

    private String mapToDeepLTarget(Language lang) {
        // DeepL 코드: EN, EN-US, EN-GB, KO ...
        return switch (lang) {
            case EN -> "EN";
            case KO -> "KO";
        };
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeepLResponse {
        @JsonProperty("translations")
        public List<Item> translations;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Item {
            @JsonProperty("text")
            public String text;
            @JsonProperty("detected_source_language")
            public String detectedSourceLanguage;
        }
    }
}
