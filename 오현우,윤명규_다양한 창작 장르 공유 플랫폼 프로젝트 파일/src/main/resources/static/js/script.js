// 필수 필드에 대한 간단한 폼 검증
document.addEventListener("DOMContentLoaded", function() {
    const form = document.querySelector('form');

    form.addEventListener('submit', function(event) {
        const emailField = document.getElementById('email');
        const passwordField = document.getElementById('password');

        if (!emailField.value || !passwordField.value) {
            alert('이메일과 비밀번호는 필수 입력 사항입니다!');
            event.preventDefault(); // 폼 제출 중단
        }
    });
});

// 기본적인 상호작용 처리 (예: 포커스 시 입력 필드 강조)
const inputs = document.querySelectorAll('input');
inputs.forEach(input => {
    input.addEventListener('focus', function() {
        input.style.borderColor = 'blue'; // 포커스 시 테두리 색 변경
    });
    input.addEventListener('blur', function() {
        input.style.borderColor = ''; // 포커스 해제 시 테두리 원상 복귀
    });
});
// 좋아요 버튼 토글
function toggleLike(postId) {
    fetch(`/novel/${postId}/like`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (response.ok) {
            updateLikeStatus(postId);
        } else {
            alert('Error toggling like');
        }
    });
}

// 좋아요 상태 업데이트
function updateLikeStatus(postId) {
    fetch(`/novel/${postId}/like-status`)
    .then(response => response.json())
    .then(hasLiked => {
        const likeButton = document.getElementById(`like-button-${postId}`);
        const likeCountElem = document.getElementById(`like-count-${postId}`);
        let likeCount = parseInt(likeCountElem.innerText);

        if (hasLiked) {
            likeButton.innerText = "Unlike";
            likeCountElem.innerText = likeCount + 1;
        } else {
            likeButton.innerText = "Like";
            likeCountElem.innerText = likeCount - 1;
        }
    });
}

// 댓글 제출 시 폼 검증
function validateCommentForm() {
    const commentContent = document.querySelector('textarea[name="content"]').value.trim();
    if (commentContent === "") {
        alert("Please enter a comment.");
        return false;  // 제출 방지
    }
    return true;  // 제출 진행
}

// 페이지 로드 시 좋아요 상태 확인
document.addEventListener("DOMContentLoaded", function() {
    const postId = document.getElementById('like-button').dataset.postId;
    updateLikeStatus(postId);
});
