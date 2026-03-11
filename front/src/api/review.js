import request from "@/utils/request";

export function getReviewSummary() {
  return request({
    url: "/api/student/review/summary",
    method: "get",
  });
}

export function listDueReviewQuestions() {
  return request({
    url: "/api/student/review/due-now",
    method: "get",
  });
}

export function listDueNowReviewQuestions() {
  return request({
    url: "/api/student/review/due-now",
    method: "get",
  });
}

export function listTodayPlanReviewQuestions() {
  return request({
    url: "/api/student/review/today-plan",
    method: "get",
  });
}

export function getNextReviewQuestion() {
  return request({
    url: "/api/student/review/next",
    method: "get",
  });
}

export function getReviewQuestion(questionId) {
  return request({
    url: `/api/student/review/question/${questionId}`,
    method: "get",
  });
}

export function submitReview(questionId, data) {
  return request({
    url: `/api/student/review/question/${questionId}/submit`,
    method: "post",
    data,
  });
}
