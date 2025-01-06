document.addEventListener('DOMContentLoaded', () => {
    const postId = Number(document.getElementById('reportPostButton').getAttribute('data-post-id'));
    const authorName = document.getElementById('authorName').innerText.trim();
    const category = document.querySelector('input[name="reportedCategory"]').value.trim();

    console.log(`Post ID: ${postId}, Author: ${authorName}, Category: ${category}`);

    document.getElementById('reportPostButton').addEventListener('click', () => {
        console.log(`Report button clicked for post ${postId}`);
        openReportModal('POST', postId, authorName, category, postId);
    });

    document.querySelectorAll('.reportCommentButton').forEach(button => {
        button.addEventListener('click', () => {
            const commentId = Number(button.getAttribute('data-id'));
            const commentPostId = Number(button.getAttribute('data-post-id'));
            console.log(`Comment Report clicked. Comment ID: ${commentId}, Post ID: ${commentPostId}`);
            openReportModal('COMMENT', commentId, authorName, category, commentPostId);
        });
    });

    document.getElementById('closeModalButton').addEventListener('click', () => {
        console.log('Report modal closed');
        closeReportModal();
    });

    document.getElementById('reportForm').addEventListener('submit', (e) => {
        e.preventDefault();
        console.log('Report form submitted');
        submitReport();
    });

    function openReportModal(type, id, author, category, postId = null) {
        console.log(`Opening report modal. Type: ${type}, ID: ${id}, Author: ${author}, Category: ${category}, Post ID: ${postId}`);
        document.getElementById('reportedType').value = type;
        document.getElementById('reportedId').value = id;
        document.getElementById('reportedAuthor').value = author;
        document.getElementById('reportedCategory').value = category;
        if (postId) {
            document.getElementById('reportedPostId').value = postId;
        }
        document.getElementById('reportModal').style.display = 'block';
    }

    function closeReportModal() {
        document.getElementById('reportModal').style.display = 'none';
    }

    async function submitReport() {
        const report = {
            reportedType: document.getElementById('reportedType').value.trim(),
            reportedId: Number(document.getElementById('reportedId').value),
            reason: document.getElementById('reason').value.trim(),
            reporter: getLoggedInUser(),
            reportedAuthor: document.getElementById('reportedAuthor').value.trim(),
            reportedCategory: document.getElementById('reportedCategory').value.trim(),
            post: {
                id: Number(document.getElementById('reportedPostId').value) || null,
            },
        };

        if (!report.reportedType || isNaN(report.reportedId) || !report.reason ||
            !report.reportedAuthor || !report.reportedCategory ||
            (report.reportedType === 'COMMENT' && !report.post.id)) {
            alert('모든 필수 정보를 올바르게 입력해 주세요.');
            console.error('필수 값 누락 또는 잘못된 값:', report);
            return;
        }

        console.log("Submitting report:", JSON.stringify(report, null, 2));

        try {
            const response = await fetch('/reports', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(report),
            });

            console.log(`Response status: ${response.status} ${response.statusText}`);

            if (response.ok) {
                alert('신고가 접수되었습니다!');
            } else {
                const errorData = await response.json();
                console.error('Server error:', errorData);
                alert(`신고 접수에 실패했습니다: ${errorData.message || '알 수 없는 오류'}`);
            }
        } catch (error) {
            console.error('Error while submitting report:', error);
            alert('신고 처리 중 오류가 발생했습니다. 네트워크 상태를 확인하세요.');
        } finally {
            console.log('Report modal closed after submission');
            closeReportModal();
        }
    }


    function getLoggedInUser() {
        const metaTag = document.querySelector('meta[name="current-user"]');
        if (!metaTag) {
            console.error("Meta tag with name 'current-user' not found.");
            return 'unknown';
        }
        const content = metaTag.getAttribute('content').trim();
        console.log("Logged in user:", content);
        return content;
    }

    const content = $('#postContent').text();
    const readingTime = Math.ceil(content.replace(/\s/g, '').length / 1000);
    console.log(`Reading time calculated: ${readingTime} minutes`);
    $('#readingTime').text(readingTime);
});
