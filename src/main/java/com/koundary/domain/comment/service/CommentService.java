package com.koundary.domain.comment.service;

import com.koundary.domain.comment.dto.CommentCreateRequest;
import com.koundary.domain.comment.dto.CommentResponse;
import com.koundary.domain.comment.dto.CommentUpdateRequest;
import com.koundary.domain.comment.entity.Comment;
import com.koundary.domain.comment.repository.CommentRepository;
import com.koundary.domain.language.entity.Language;
import com.koundary.domain.language.service.TranslationService;
import com.koundary.domain.language.util.NationalityLanguageMapper;
import com.koundary.domain.post.entity.Post;
import com.koundary.domain.post.repository.PostRepository;
import com.koundary.domain.user.entity.User;
import com.koundary.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TranslationService translationService;

    /**
     * [리팩토링] 댓글 DTO 변환을 중앙에서 처리하는 헬퍼 메서드.
     */
    private CommentResponse toTranslatedCommentResponse(Comment comment, Long currentUserId, int replyCount) {
        Long authorId = comment.getUser().getUserId();

        // 1. 번역할 목표 언어 결정
        User viewer = (currentUserId != null) ? userRepository.findById(currentUserId).orElse(null) : null;
        Language targetLanguage = Language.KO;
        if (viewer != null && viewer.getNationality() != null && !viewer.getNationality().isEmpty()) {
            String canonical = NationalityLanguageMapper.canonicalize(viewer.getNationality());
            targetLanguage = NationalityLanguageMapper.defaultLanguageOf(canonical);
        }

        // 2. 내용 번역
        String translatedContent = comment.isDeleted() ? "(삭제된 댓글입니다)" : comment.getContent();
        if (!comment.isDeleted() && targetLanguage != Language.KO) {
            translatedContent = translationService.translateAndCache(
                    "COMMENT", comment.getId(), "content", comment.getContent(), targetLanguage
            );
        }

        // 3. 최종 DTO 생성 및 반환
        return CommentResponse.builder()
                .commentId(comment.getId())
                .postId(comment.getPost().getPostId())
                .authorId(authorId)
                .authorNickname(comment.getUser().getNickname())
                .authorProfileImage(comment.getUser().getProfileImageUrl())
                .content(translatedContent)
                .deleted(comment.isDeleted())
                .mine(currentUserId != null && authorId.equals(currentUserId))
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .replyCount(replyCount)
                .build();
    }

    @Transactional
    public CommentResponse addComment(Long postId, Long userId, CommentCreateRequest req) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Comment comment = Comment.builder().post(post).user(user).content(req.getContent()).build();
        Comment saved = commentRepository.save(comment);
        return toTranslatedCommentResponse(saved, userId, 0);
    }

    @Transactional
    public CommentResponse addReply(Long parentCommentId, Long userId, CommentCreateRequest req) {
        Comment parent = commentRepository.findById(parentCommentId).orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        if (!parent.isTopLevel()) throw new IllegalStateException("대댓글의 대댓글은 허용하지 않습니다.");
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Comment reply = Comment.builder().post(parent.getPost()).user(user).parent(parent).content(req.getContent()).build();
        Comment saved = commentRepository.save(reply);
        return toTranslatedCommentResponse(saved, userId, 0);
    }

    public Page<CommentResponse> getComments(Long postId, Long currentUserId, Pageable pageable) {
        Page<Comment> page = commentRepository.findByPost_PostIdAndParentIsNullOrderByCreatedAtAsc(postId, pageable);
        return page.map(c -> toTranslatedCommentResponse(c, currentUserId, commentRepository.countByParent_Id(c.getId())));
    }

    public List<CommentResponse> getReplies(Long parentCommentId, Long currentUserId) {
        List<Comment> replies = commentRepository.findByParent_IdOrderByCreatedAtAsc(parentCommentId);
        return replies.stream().map(r -> toTranslatedCommentResponse(r, currentUserId, 0)).toList();
    }

    @Transactional
    public CommentResponse update(Long commentId, Long userId, CommentUpdateRequest req) {
        Comment c = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        if (!c.getUser().getUserId().equals(userId)) throw new SecurityException("수정 권한이 없습니다.");
        if (c.isDeleted()) throw new IllegalStateException("삭제된 댓글은 수정할 수 없습니다.");
        c.updateContent(req.getContent());
        // 수정 후 DTO 변환 시에도 번역 로직을 태움
        return toTranslatedCommentResponse(c, userId, c.isTopLevel() ? commentRepository.countByParent_Id(c.getId()) : 0);
    }

    @Transactional
    public void delete(Long commentId, Long userId) {
        Comment c = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        if (!c.getUser().getUserId().equals(userId)) throw new SecurityException("삭제 권한이 없습니다.");
        if (!c.isDeleted()) c.softDelete();
    }
}