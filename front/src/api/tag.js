import request from "@/utils/request";

// 我的标签列表
export function listMyTags() {
    return request({
        url: "/api/student/tags",
        method: "get",
    });
}

// 新建标签
export function createTag(data) {
    return request({
        url: "/api/student/tags",
        method: "post",
        data, // { name }
    });
}

// 删除标签
export function deleteTag(tagId) {
    return request({
        url: `/api/student/tags/${tagId}`,
        method: "delete",
    });
}
