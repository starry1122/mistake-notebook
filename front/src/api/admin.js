import request from "@/utils/request";

export function listAdminStudents() {
  return request({
    url: "/api/admin/analysis/students",
    method: "get",
  });
}

export function getAdminStudentOverview(studentId) {
  return request({
    url: `/api/admin/analysis/students/${studentId}/overview`,
    method: "get",
  });
}

export function getAdminSystemStats(params) {
  return request({
    url: "/api/admin/analysis/system",
    method: "get",
    params,
  });
}

