package com.koundary.global.i18n;

import com.koundary.domain.language.entity.Language;
import com.koundary.domain.language.util.NationalityLanguageMapper;
import com.koundary.global.security.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class LocaleUserFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
                String nationality = cud.getUser().getNationality();
                if (nationality != null) {
                    String canonical = NationalityLanguageMapper.canonicalize(nationality);
                    Language lang = NationalityLanguageMapper.defaultLanguageOf(canonical);
                    LocaleContextHolder.setLocale(Locale.forLanguageTag(lang.getCode())); // "ko"/"en"
                }
            }
            chain.doFilter(req, res);
        } finally {
            LocaleContextHolder.resetLocaleContext();
        }
    }
}
