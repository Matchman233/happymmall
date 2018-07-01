package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by apple on 2018/6/28.
 */
public interface IFileService {
    String upload(MultipartFile multipartFile, String path);
}
