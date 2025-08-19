package com.koundary.domain.post.service;

import com.koundary.domain.board.entity.Board;
import com.koundary.domain.board.repository.BoardRepository;
import com.koundary.domain.post.entity.Image;
import com.koundary.domain.post.dto.PostCreateRequest;
import com.koundary.domain.post.dto.PostResponse;
import com.koundary.domain.post.entity.Post;
import com.koundary.domain.post.repository.PostRepository;
import com.koundary.domain.user.entity.User;
import com.koundary.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private static final String INFORMATION_BOARD_CODE = "INFORMATION";

    @Transactional
    public PostResponse createPost(String boardCode, PostCreateRequest request, Long userId) {
        Board board = boardRepository.findByBoardCode(boardCode)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시판입니다"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다"));

        // 1. 일반 게시판 + 정보글 체크된 경우 → 게시글 2개 저장
        if (!INFORMATION_BOARD_CODE.equals(boardCode) && Boolean.TRUE.equals(request.isInformation())) {
            Board infoBoard = boardRepository.findByBoardCode(INFORMATION_BOARD_CODE)
                    .orElseThrow(() -> new IllegalStateException("정보게시판이 존재하지 않습니다"));

            Post original = buildPost(request, board, user, true);
            Post copy = buildPost(request, infoBoard, user, true);

            postRepository.save(original);
            postRepository.save(copy);

            return toResponse(original);
        }

        // 2. 일반 게시판 or 정보게시판 (isInformation 무시)
        Post post = buildPost(request, board, user, false);
        postRepository.save(post);

        return toResponse(post);
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

    private PostResponse toResponse(Post post) {
        return PostResponse.from(post);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPostsByBoard(String boardCode) {
        Board board = boardRepository.findByBoardCode(boardCode)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시판입니다"));

        return postRepository.findAllByBoardOrderByCreatedAtDesc(board).stream()
                .map(this::toResponse)
                .toList();
    }

}