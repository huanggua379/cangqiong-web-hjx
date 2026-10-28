package com.sky.controller.admin;

import com.sky.exception.FileUploadException;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

/**
 * 通用接口。
 */
@RestController
@RequestMapping("/admin/common")
@Slf4j
public class CommonController {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;

    private final AliOssUtil aliOssUtil;

    public CommonController(AliOssUtil aliOssUtil) {
        this.aliOssUtil = aliOssUtil;
    }

    /**
     * 上传菜品图片。multipart/form-data 参数名必须为 file。
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam(value = "file", required = false) MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("请选择要上传的图片");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new FileUploadException("图片大小不能超过 5MB");
        }

        try {
            byte[] bytes = file.getBytes();
            ImageType imageType = detectImageType(bytes);
            String originalExtension = getExtension(file.getOriginalFilename());
            if (imageType == null || !imageType.matches(originalExtension)) {
                throw new FileUploadException("仅支持 JPG、PNG、GIF、BMP 或 WEBP 图片，且扩展名须与内容一致");
            }

            String objectName = "dish/" + UUID.randomUUID().toString().replace("-", "")
                    + "." + imageType.extension;
            String url = aliOssUtil.upload(bytes, objectName, imageType.contentType);
            return Result.success(url);
        } catch (IOException ex) {
            log.error("读取上传图片失败，exceptionType={}", ex.getClass().getSimpleName());
            throw new FileUploadException("读取上传图片失败", ex);
        }
    }

    private String getExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot + 1).toLowerCase(Locale.ROOT);
    }

    private ImageType detectImageType(byte[] bytes) {
        if (startsWith(bytes, 0xFF, 0xD8, 0xFF)) {
            return ImageType.JPG;
        }
        if (startsWith(bytes, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) {
            return ImageType.PNG;
        }
        if (startsWith(bytes, 0x47, 0x49, 0x46, 0x38)) {
            return ImageType.GIF;
        }
        if (startsWith(bytes, 0x42, 0x4D)) {
            return ImageType.BMP;
        }
        if (bytes.length >= 12 && startsWith(bytes, 0x52, 0x49, 0x46, 0x46)
                && bytes[8] == 0x57 && bytes[9] == 0x45 && bytes[10] == 0x42 && bytes[11] == 0x50) {
            return ImageType.WEBP;
        }
        return null;
    }

    private boolean startsWith(byte[] bytes, int... signature) {
        if (bytes.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if ((bytes[i] & 0xFF) != signature[i]) {
                return false;
            }
        }
        return true;
    }

    private enum ImageType {
        JPG("jpg", "image/jpeg"),
        PNG("png", "image/png"),
        GIF("gif", "image/gif"),
        BMP("bmp", "image/bmp"),
        WEBP("webp", "image/webp");

        private final String extension;
        private final String contentType;

        ImageType(String extension, String contentType) {
            this.extension = extension;
            this.contentType = contentType;
        }

        private boolean matches(String candidate) {
            return extension.equals(candidate) || (this == JPG && "jpeg".equals(candidate));
        }
    }
}
