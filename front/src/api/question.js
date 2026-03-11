import request from "@/utils/request";

export function createManualQuestion(data) {
    return request({
        url: "/api/student/questions/manual",
        method: "post",
        data,
    });
}

export function createImageQuestion({ file, meta }) {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("meta", new Blob([JSON.stringify(meta)], { type: "application/json" }));

    return request({
        url: "/api/student/questions/image",
        method: "post",
        data: formData,
    });
}

export function listQuestions(params) {
    return request({
        url: "/api/student/questions",
        method: "get",
        params,
    });
}

export function getQuestionDetail(questionId) {
    return request({
        url: `/api/student/questions/${questionId}`,
        method: "get",
    });
}

export function updateQuestion(questionId, data) {
    return request({
        url: `/api/student/questions/${questionId}`,
        method: "put",
        data,
    });
}

export function deleteQuestion(questionId) {
    return request({
        url: `/api/student/questions/${questionId}`,
        method: "delete",
    });
}

export function updateFavorite(questionId, favorite) {
    return request({
        url: `/api/student/questions/${questionId}/favorite`,
        method: "put",
        params: { favorite },
    });
}

export function listQuestionSubjects() {
    return request({
        url: "/api/student/questions/subjects",
        method: "get",
    });
}

export function listQuestionKnowledgePoints(subject) {
    return request({
        url: "/api/student/questions/knowledge-points",
        method: "get",
        params: { subject },
    });
}

