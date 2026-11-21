package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文件上传结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadVO implements Serializable {

    /** 应保存到业务数据中的 OSS 对象键，例如 dish/xxx.jpg。 */
    private String objectName;

    /** 仅用于即时预览的临时签名 URL，禁止持久化。 */
    private String previewUrl;
}
