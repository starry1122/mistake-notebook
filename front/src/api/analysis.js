import request from "@/utils/request";

export function getStudentAnalysisDashboard(params) {
  return request({
    url: "/analysis/student/dashboard",
    method: "get",
    params,
  });
}

