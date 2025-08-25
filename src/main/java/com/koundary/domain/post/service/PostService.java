package com.koundary.domain.post.service;

import com.koundary.domain.board.entity.Board;
import com.koundary.domain.board.repository.BoardRepository;
import com.koundary.domain.myPage.dto.MyCommentedPostItemResponse;
import com.koundary.domain.post.dto.PostCreateRequest;
import com.koundary.domain.post.dto.PostResponse;
import com.koundary.domain.post.dto.PostUpdateRequest;
import com.koundary.domain.post.entity.Image;
import com.koundary.domain.post.entity.Post;
import com.koundary.domain.post.repository.PostRepository;
import com.koundary.domain.scrap.repository.ScrapRepository; // ✅ 추가
import com.koundary.domain.user.entity.User;
import com.koundary.domain.user.repository.UserRepository;
import com.koundary.global.security.CustomUserDetails;
import com.koundary.global.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PostService {

    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ScrapRepository scrapRepository; // ✅ 추가

    private final S3Uploader s3Uploader;

    private static final String INFORMATION_BOARD_CODE = "INFORMATION";
    private static final String POST_IMAGE_DIR = "posts";

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
            return PostResponse.from(original); // 방금 작성 → 기본 false로 내려감
        }

        // 단일 저장 (정보게시판 자체에 쓰는 경우 포함)
        Post post = buildPost(request, board, user, Boolean.TRUE.equals(request.isInformation()));
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
                .map(PostResponse::from) // 로그인 미반영 버전: isScrapped=false
                .toList();
    }

    // ✅ 페이징 버전 (viewerUserId로 isScrapped 채움)
    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByBoard(String boardCode, Pageable pageable, Long viewerUserId) {
        boardRepository.findByBoardCode(boardCode)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시판입니다"));

        Page<Post> page = postRepository.findPageByBoardCode(boardCode, pageable);

        if (viewerUserId == null || page.isEmpty()) {
            return page.map(p -> PostResponse.from(p, false));
        }

        // N+1 방지: 한 번에 내가 스크랩한 postId 모으기
        List<Long> postIds = page.getContent().stream().map(Post::getPostId).toList();
        List<Long> scrappedIds = scrapRepository.findScrappedPostIdsByUserAndPostIds(viewerUserId, postIds); // ✅ 여기서 사용
        Set<Long> scrappedSet = new HashSet<>(scrappedIds);

        return page.map(p -> PostResponse.from(p, scrappedSet.contains(p.getPostId())));
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getMyPostsByBoard(String boardCode, Long userId, Pageable pageable) {
        User me = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다. id=" + userId));

        if ("NATIONALITY".equalsIgnoreCase(boardCode)) {
            String nation = me.getNationality();
            if (nation == null || nation.trim().isEmpty()) {
                throw new IllegalStateException("회원 프로필에 국가 정보가 없습니다. 마이페이지에서 국가를 설정해주세요.");
            }
            return postRepository.findByBoard_BoardCodeAndUser_Nationality("NATIONALITY", nation, pageable)
                    .map(PostResponse::from);
        }

        if ("UNIVERSITY".equalsIgnoreCase(boardCode)) {
            String univ = me.getUniversity();
            if (univ == null || univ.trim().isEmpty()) {
                throw new IllegalStateException("회원 프로필에 학교 정보가 없습니다. 마이페이지에서 학교를 설정해주세요.");
            }
            return postRepository.findByBoard_BoardCodeAndUser_University("UNIVERSITY", univ, pageable)
                    .map(PostResponse::from);
        }

        throw new IllegalArgumentException("지원하지 않는 보드 코드입니다: " + boardCode);
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(String boardCode, Long postId, Long viewerUserId) {
        Post post = postRepository.findByPostIdAndBoard_BoardCode(postId, boardCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "게시글을 찾을 수 없습니다. (boardCode=%s, id=%d)".formatted(boardCode, postId)
                ));

        boolean isScrapped = false;
        if (viewerUserId != null) {
            isScrapped = scrapRepository.existsByPost_PostIdAndUser_UserId(postId, viewerUserId); // ✅ 여기서 사용
        }

        return PostResponse.from(post, isScrapped);
    }

    // ✅ 게시글 수정 (원본/복사본 동시 반영)
    @Transactional
    public PostResponse updatePost(String boardCode, Long postId, PostUpdateRequest req, Long userId) {
        Post target = postRepository.findByPostIdAndBoard_BoardCode(postId, boardCode)
                .orElseThrow(() -> new IllegalArgumentException("수정 대상 게시글을 찾을 수 없습니다."));

        // 권한 체크 (작성자만 허용)
        if (!Objects.equals(target.getUser().getUserId(), userId)) {
            throw new IllegalStateException("본인이 작성한 글만 수정할 수 있습니다.");
        }

        boolean requestIsInfo = req.isInformation() != null ? req.isInformation() : target.isInformation();

        List<Image> newImages = null;
        if (req.imageUrls() != null) {
            newImages = req.imageUrls().stream()
                    .map(url -> Image.builder().imageUrl(url).post(null).build())
                    .toList();
        }

        if (target.getGroupKey() != null) {
            List<Post> groupPosts = postRepository.findAllByGroupKey(target.getGroupKey());

            if (!requestIsInfo) {
                for (Post p : groupPosts) {
                    boolean isInfoBoard = INFORMATION_BOARD_CODE.equals(p.getBoard().getBoardCode());
                    if (isInfoBoard) {
                        deleteImagesFromS3(p);
                        postRepository.delete(p);
                    } else {
                        applyUpdate(p, req, newImages, false);
                        p.setGroupKey(null);
                    }
                }
                Post remaining = groupPosts.stream()
                        .filter(p -> !INFORMATION_BOARD_CODE.equals(p.getBoard().getBoardCode()))
                        .findFirst()
                        .orElse(target);
                return PostResponse.from(remaining);
            }

            for (Post p : groupPosts) {
                if (newImages != null) deleteImagesFromS3(p);
                applyUpdate(p, req, newImages, true);
            }
            return PostResponse.from(
                    groupPosts.stream().filter(p -> p.getPostId().equals(target.getPostId()))
                            .findFirst().orElse(target)
            );
        }

        if (!INFORMATION_BOARD_CODE.equals(boardCode) && requestIsInfo) {
            if (newImages != null) deleteImagesFromS3(target);
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

            if (req.imageUrls() != null) {
                List<Image> copied = req.imageUrls().stream()
                        .map(url -> Image.builder().imageUrl(url).post(copy).build())
                        .toList();
                copy.replaceImages(copied);
            } else {
                List<Image> copied = target.getImages().stream()
                        .map(img -> Image.builder().imageUrl(img.getImageUrl()).post(copy).build())
                        .toList();
                copy.replaceImages(copied);
            }

            postRepository.save(copy);
            return PostResponse.from(target);
        }

        if (newImages != null) deleteImagesFromS3(target);
        applyUpdate(target, req, newImages, requestIsInfo);
        return PostResponse.from(target);
    }

    // 멀티파트 전용: 수정(JSON + files)
    @Transactional
    public PostResponse updatePostWithFiles(
            String boardCode,
            Long postId,
            PostUpdateRequest req,
            Long userId,
            List<MultipartFile> newFiles
    ) {
        // 1) 새 파일 업로드
        List<String> uploadedUrls = uploadAll(newFiles);

        // 2) 최종 이미지 세트 결정(치환 규칙 유지)
        // - req.imageUrls() != null → (req.imageUrls + 업로드 파일)로 치환
        // - req.imageUrls() == null && 새 파일 존재 → 업로드 파일로 치환
        // - 둘 다 없으면 → 이미지 변경 없음
        List<String> finalUrls = null;
        if (req.imageUrls() != null) {
            finalUrls = mergeUrls(req.imageUrls(), uploadedUrls);
        } else if (!uploadedUrls.isEmpty()) {
            finalUrls = uploadedUrls;
        }

        // 3) 기존 updatePost 로직을 재사용하되, newImages 리스트를 finalUrls로 대체
        Post target = postRepository.findByPostIdAndBoard_BoardCode(postId, boardCode)
                .orElseThrow(() -> new IllegalArgumentException("수정 대상 게시글을 찾을 수 없습니다."));
        if (!Objects.equals(target.getUser().getUserId(), userId)) {
            throw new IllegalStateException("본인이 작성한 글만 수정할 수 있습니다.");
        }

        boolean requestIsInfo = req.isInformation() != null ? req.isInformation() : target.isInformation();

        List<Image> newImageEntities = null;
        if (finalUrls != null) {
            newImageEntities = finalUrls.stream()
                    .map(url -> Image.builder().imageUrl(url).post(null).build())
                    .toList();
        }

        if (target.getGroupKey() != null) {
            List<Post> groupPosts = postRepository.findAllByGroupKey(target.getGroupKey());

            if (!requestIsInfo) {
                for (Post p : groupPosts) {
                    boolean isInfoBoard = INFORMATION_BOARD_CODE.equals(p.getBoard().getBoardCode());
                    if (isInfoBoard) {
                        deleteImagesFromS3(p);
                        postRepository.delete(p);
                    } else {
                        if (newImageEntities != null) deleteImagesFromS3(p);
                        applyUpdate(p, req, newImageEntities, false);
                        p.setGroupKey(null);
                    }
                }
                Post remaining = groupPosts.stream()
                        .filter(p -> !INFORMATION_BOARD_CODE.equals(p.getBoard().getBoardCode()))
                        .findFirst()
                        .orElse(target);
                return PostResponse.from(remaining);
            }

            for (Post p : groupPosts) {
                if (newImageEntities != null) deleteImagesFromS3(p);
                applyUpdate(p, req, newImageEntities, true);
            }
            return PostResponse.from(
                    groupPosts.stream().filter(p -> p.getPostId().equals(target.getPostId()))
                            .findFirst().orElse(target)
            );
        }

        if (!INFORMATION_BOARD_CODE.equals(boardCode) && requestIsInfo) {
            if (newImageEntities != null) deleteImagesFromS3(target);
            applyUpdate(target, req, newImageEntities, true);

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

            List<Image> copied;
            if (finalUrls != null) {
                copied = finalUrls.stream()
                        .map(url -> Image.builder().imageUrl(url).post(copy).build())
                        .toList();
            } else {
                copied = target.getImages().stream()
                        .map(img -> Image.builder().imageUrl(img.getImageUrl()).post(copy).build())
                        .toList();
            }
            copy.replaceImages(copied);

            postRepository.save(copy);
            return PostResponse.from(target);
        }

        if (newImageEntities != null) deleteImagesFromS3(target);
        applyUpdate(target, req, newImageEntities, requestIsInfo);
        return PostResponse.from(target);
    }


    private void applyUpdate(Post post, PostUpdateRequest req, List<Image> newImages, boolean isInformationFinal) {
        post.updateContent(req.title(), req.content(), isInformationFinal);
        if (newImages != null) {
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
            List<Post> groupPosts = postRepository.findAllByGroupKey(target.getGroupKey());
            groupPosts.forEach(this::deleteImagesFromS3);
            postRepository.deleteAll(groupPosts);
        } else {
            deleteImagesFromS3(target);
            postRepository.delete(target);
        }
    }

    // 멀티파트 생성
    @Transactional
    public PostResponse createPostWithFiles(
            String boardCode,
            PostCreateRequest request,
            Long userId,
            List<MultipartFile> images
    ) {
        // 1) 파일 업로드
        List<String> uploadedUrls = uploadAll(images);

        // 2) 요청 JSON 안의 imageUrls(있다면) + 업로드된 URL 병합
        List<String> mergedUrls = mergeUrls(request.imageUrls(), uploadedUrls);

        // 3) 기존 로직 + 이미지 세팅 버전으로 저장
        Board board = boardRepository.findByBoardCode(boardCode)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시판입니다"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다"));

        if (!INFORMATION_BOARD_CODE.equals(boardCode) && Boolean.TRUE.equals(request.isInformation())) {
            Board infoBoard = boardRepository.findByBoardCode(INFORMATION_BOARD_CODE)
                    .orElseThrow(() -> new IllegalStateException("정보게시판이 존재하지 않습니다"));

            String groupKey = UUID.randomUUID().toString();

            Post original = buildPostWithUrls(request, board, user, true, mergedUrls);
            original.setGroupKey(groupKey);

            Post copy = buildPostWithUrls(request, infoBoard, user, true, mergedUrls);
            copy.setGroupKey(groupKey);

            postRepository.save(original);
            postRepository.save(copy);
            return PostResponse.from(original);
        }

        Post post = buildPostWithUrls(request, board, user, Boolean.TRUE.equals(request.isInformation()), mergedUrls);
        postRepository.save(post);
        return PostResponse.from(post);
    }

    // 이미지 URL 리스트를 명시적으로 주입하는 build 버전
    private Post buildPostWithUrls(PostCreateRequest req, Board board, User user, boolean forceInformation, List<String> imageUrls) {
        Post post = Post.builder()
                .title(req.title())
                .content(req.content())
                .isInformation(forceInformation || Boolean.TRUE.equals(req.isInformation()))
                .board(board)
                .user(user)
                .build();

        if (imageUrls != null) {
            for (String url : imageUrls) {
                Image image = Image.builder()
                        .imageUrl(url)
                        .post(post)
                        .build();
                post.getImages().add(image);
            }
        }
        return post;
    }

    // -----------------------------
    // S3 관련 헬퍼
    // -----------------------------
    private List<String> uploadAll(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return List.of();
        return files.stream()
                .filter(f -> f != null && !f.isEmpty())
                .map(f -> s3Uploader.upload(f, POST_IMAGE_DIR)) // S3에 업로드 후 public URL 반환
                .toList();
    }

    private void deleteImagesFromS3(Post post) {
        if (post.getImages() == null || post.getImages().isEmpty()) return;
        for (Image img : post.getImages()) {
            String url = img.getImageUrl();
            if (url == null || url.isBlank()) continue;
            try {
                String key = s3Uploader.keyFromUrl(url);
                s3Uploader.delete(key);
            } catch (Exception ignored) {
                // URL 포맷이 다르거나 외부 링크일 수 있으니 서비스 흐름은 유지
            }
        }
    }

    private List<String> mergeUrls(List<String> urlsFromRequest, List<String> uploadedUrls) {
        List<String> result = new ArrayList<>();
        if (urlsFromRequest != null) result.addAll(urlsFromRequest);
        if (uploadedUrls != null) result.addAll(uploadedUrls);
        return result;
    }
}
