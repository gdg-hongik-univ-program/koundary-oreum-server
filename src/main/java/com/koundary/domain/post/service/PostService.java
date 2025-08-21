package com.koundary.domain.post.service;

import com.koundary.domain.board.entity.Board;
import com.koundary.domain.board.repository.BoardRepository;
import com.koundary.domain.post.dto.PostCreateRequest;
import com.koundary.domain.post.dto.PostResponse;
import com.koundary.domain.post.dto.PostUpdateRequest;
import com.koundary.domain.post.entity.Image;
import com.koundary.domain.post.entity.Post;
import com.koundary.domain.post.repository.PostRepository;
import com.koundary.domain.user.entity.User;
import com.koundary.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.koundary.domain.language.entity.Language;
import com.koundary.domain.language.service.TranslationService;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TranslationService translationService;

    private static final String INFORMATION_BOARD_CODE = "INFORMATION";

    @Transactional
    public PostResponse createPost(String boardCode, PostCreateRequest request, Long userId) {
        Board board = boardRepository.findByBoardCode(boardCode)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시판입니다"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다"));

        // 일반 게시판 + 정보글 체크 → 정보게시판에도 복사 (둘 다 같은 groupKey)
        if (!INFORMATION_BOARD_CODE.equals(boardCode) && Boolean.TRUE.equals(request.isInformation())) {
            Board infoBoard = boardRepository.findByBoardCode(INFORMATION_BOARD_CODE)
                    .orElseThrow(() -> new IllegalStateException("정보게시판이 존재하지 않습니다"));

            String groupKey = UUID.randomUUID().toString();

            Post original = buildPost(request, board, user, true);
            original.setGroupKey(groupKey);

            Post copy = buildPost(request, infoBoard, user, true);
            copy.setGroupKey(groupKey);

            postRepository.save(original);
            postRepository.save(copy);
            return PostResponse.from(original);
        }

        // 단일 저장 (정보게시판 자체에 쓰는 경우 포함)
        Post post = buildPost(request, board, user, Boolean.TRUE.equals(request.isInformation()));
        // 단독 글은 groupKey 생략(null)
        postRepository.save(post);
        return PostResponse.from(post);
    }

    private Post buildPost(PostCreateRequest req, Board board, User user, boolean forceInformation) {
        Post post = Post.builder()
                .title(req.title())
                .content(req.content())
                .isInformation(forceInformation || Boolean.TRUE.equals(req.isInformation()))
                .board(board)
                .user(user)
                .build();

        if (req.imageUrls() != null) {
            for (String url : req.imageUrls()) {
                Image image = Image.builder()
                        .imageUrl(url)
                        .post(post)
                        .build();
                post.getImages().add(image);
            }
        }

        return post;
    }

    // 기존 리스트 반환 (호환)
    @Transactional(readOnly = true)
    public List<PostResponse> getPostsByBoard(String boardCode) {
        Board board = boardRepository.findByBoardCode(boardCode)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시판입니다"));

        return postRepository.findAllByBoardOrderByCreatedAtDesc(board).stream()
                .map(PostResponse::from)
                .toList();
    }

    // ✅ 페이징 버전 (스프링 Page로 반환)
    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByBoard(String boardCode, Pageable pageable) {
        // 존재 여부 검증
        boardRepository.findByBoardCode(boardCode)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시판입니다"));

        return postRepository.findPageByBoardCode(boardCode, pageable)
                .map(PostResponse::from);
    }
    @Transactional(readOnly = true)
    public PostResponse getPost(String boardCode, Long postId) {
        Post post = postRepository.findByPostIdAndBoard_BoardCode(postId, boardCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "게시글을 찾을 수 없습니다. (boardCode=%s, id=%d)".formatted(boardCode, postId)
                ));
        return PostResponse.from(post);
    }
    // ✅ 게시글 수정 (원본/복사본 동시 반영)
    @Transactional
    public PostResponse updatePost(String boardCode, Long postId, PostUpdateRequest req, Long userId) {
        Post target = postRepository.findByPostIdAndBoard_BoardCode(postId, boardCode)
                .orElseThrow(() -> new IllegalArgumentException("수정 대상 게시글을 찾을 수 없습니다."));

        // 권한 체크 (작성자만 허용) — Role 도입 전 임시
        if (!Objects.equals(target.getUser().getUserId(), userId)) {
            throw new IllegalStateException("본인이 작성한 글만 수정할 수 있습니다.");
        }

        // 변경 후 isInformation 결정 로직
        boolean requestIsInfo = req.isInformation() != null ? req.isInformation() : target.isInformation();

        // 이미지 변환
        List<Image> newImages = null;
        if (req.imageUrls() != null) {
            newImages = req.imageUrls().stream()
                    .map(url -> Image.builder().imageUrl(url).post(null).build())
                    .toList();
        }

        // groupKey가 있으면 (원본/복사 세트) → 두 글 모두 업데이트
        if (target.getGroupKey() != null) {
            List<Post> groupPosts = postRepository.findAllByGroupKey(target.getGroupKey());

            // isInformation=false로 변경 요청이면: 정보게시판 글은 제거, 일반 글만 남김
            if (!requestIsInfo) {
                for (Post p : groupPosts) {
                    boolean isInfoBoard = INFORMATION_BOARD_CODE.equals(p.getBoard().getBoardCode());
                    if (isInfoBoard) {
                        postRepository.delete(p);
                    } else {
                        // 일반게시판 글은 업데이트하고 groupKey 해제(단독으로 전환)
                        applyUpdate(p, req, newImages, false);
                        p.setGroupKey(null);
                    }
                }
                // 남은 일반 글 반환
                Post remaining = groupPosts.stream()
                        .filter(p -> !INFORMATION_BOARD_CODE.equals(p.getBoard().getBoardCode()))
                        .findFirst()
                        .orElse(target);
                return PostResponse.from(remaining);
            }

            // isInformation=true 유지 → 두 글 모두 내용/이미지 동기화
            for (Post p : groupPosts) {
                applyUpdate(p, req, newImages, true);
            }
            // 호출한 글 기준 반환
            return PostResponse.from(
                    groupPosts.stream().filter(p -> p.getPostId().equals(target.getPostId()))
                            .findFirst().orElse(target)
            );
        }

        // groupKey가 없는 단독 글
        // 1) 현재 게시판이 일반 + isInformation=true로 바꾸는 경우 → 정보게시판에 복제 생성 + groupKey 부여
        if (!INFORMATION_BOARD_CODE.equals(boardCode) && requestIsInfo) {
            // 먼저 현재 글 업데이트
            applyUpdate(target, req, newImages, true);

            String groupKey = UUID.randomUUID().toString();
            target.setGroupKey(groupKey);

            Board infoBoard = boardRepository.findByBoardCode(INFORMATION_BOARD_CODE)
                    .orElseThrow(() -> new IllegalStateException("정보게시판이 존재하지 않습니다"));

            Post copy = Post.builder()
                    .title(target.getTitle())
                    .content(target.getContent())
                    .isInformation(true)
                    .board(infoBoard)
                    .user(target.getUser())
                    .build();
            copy.setGroupKey(groupKey);
            // 이미지 복사
            if (req.imageUrls() != null) {
                List<Image> copied = req.imageUrls().stream()
                        .map(url -> Image.builder().imageUrl(url).post(copy).build())
                        .toList();
                copy.replaceImages(copied);
            } else {
                // 기존 이미지 그대로 복제
                List<Image> copied = target.getImages().stream()
                        .map(img -> Image.builder().imageUrl(img.getImageUrl()).post(copy).build())
                        .toList();
                copy.replaceImages(copied);
            }

            postRepository.save(copy);
            return PostResponse.from(target);
        }

        // 2) 정보게시판 글을 단독으로 수정 (groupKey가 없고 boardCode=INFORMATION) → 그냥 업데이트
        applyUpdate(target, req, newImages, requestIsInfo);
        return PostResponse.from(target);
    }

    private void applyUpdate(Post post, PostUpdateRequest req, List<Image> newImages, boolean isInformationFinal) {
        post.updateContent(req.title(), req.content(), isInformationFinal);
        if (newImages != null) {
            // 소유관계 설정
            List<Image> attach = new ArrayList<>();
            for (Image img : newImages) {
                attach.add(Image.builder().imageUrl(img.getImageUrl()).post(post).build());
            }
            post.replaceImages(attach);
        }
    }

    // ✅ 게시글 삭제 (세트로 함께 삭제)
    @Transactional
    public void deletePost(String boardCode, Long postId, Long userId) {
        Post target = postRepository.findByPostIdAndBoard_BoardCode(postId, boardCode)
                .orElseThrow(() -> new IllegalArgumentException("삭제 대상 게시글을 찾을 수 없습니다."));

        if (!Objects.equals(target.getUser().getUserId(), userId)) {
            throw new IllegalStateException("본인이 작성한 글만 삭제할 수 있습니다.");
        }

        if (target.getGroupKey() != null) {
            // 세트 전체 삭제
            List<Post> groupPosts = postRepository.findAllByGroupKey(target.getGroupKey());
            postRepository.deleteAll(groupPosts);
        } else {
            // 단독 삭제
            postRepository.delete(target);
        }
    }
}
