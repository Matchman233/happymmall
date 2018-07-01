package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by apple on 2018/6/28.
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService {
    Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    public String upload(MultipartFile multipartFile, String path) {
        String fileOriginName = multipartFile.getOriginalFilename();
        String fileExtensionName = fileOriginName.substring(fileOriginName.lastIndexOf(".") + 1);
        String uniqueName = UUID.randomUUID().toString() + "." + fileExtensionName;
        logger.info("开始上传文件，上传文件的名字为:{},上传的路径为:{},修改后的新文件名字为：{}", fileOriginName, path, uniqueName);

        File pathDir = new File(path);
        if (!pathDir.exists()) {
            pathDir.setWritable(true);
            pathDir.mkdirs();
        }
        File targetFile = new File(pathDir, uniqueName);

        try {
            // 转一下
            multipartFile.transferTo(targetFile);
            // 文件上传成功之后
            //todo 将targetFile上传到FTP服务器
            FTPUtil.uploadFile(Lists.<File>newArrayList(targetFile));
            //todo 上传完之后删除upload下对应的文件
            targetFile.delete();
        } catch (IOException e) {
            logger.error("上传文件异常!", e);
            return null;
        }
        return targetFile.getName();
    }

}
