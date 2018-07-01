package com.mmall.util;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by apple on 2018/6/28.
 */
public class FTPUtil {

    public static final Logger logger = LoggerFactory.getLogger(FTPUtil.class);

    private static String ftpIP = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpuser = PropertiesUtil.getProperty("ftp.user");
    private static String ftppass = PropertiesUtil.getProperty("ftp.pass");


    private String ip;
    private Integer port;
    private String user;
    private String password;
    private FTPClient ftpClient;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }

    public FTPUtil(String ip, Integer port, String user, String password) {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.password = password;
    }


    public static boolean uploadFile(List<File> fileList) {
        FTPUtil ftpUtil = new FTPUtil(ftpIP, 21, ftpuser, ftppass);
        logger.info("开始连接ftp服务器");
        boolean result = ftpUtil.uploadFile("img", fileList);
        logger.info("结束上传,上传结果:{}", result);
        return result;
    }


    private boolean uploadFile(String remotePath, List<File> fileList) {
        boolean upload = true;
        FileInputStream fileInputStream = null;
        // 连接FTP服务器
        if (connectServer(ip, port, user, password)) {
            try {
                ftpClient.changeWorkingDirectory(remotePath);
                ftpClient.setBufferSize(1024);
                ftpClient.setControlEncoding("utf-8");
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE); // 二进制文件，减少出错
                // ftpClient.enterLocalPassiveMode();
                for (File file : fileList) {
                    fileInputStream = new FileInputStream(file);
                    ftpClient.storeFile(file.getName(), fileInputStream);
                }
            } catch (IOException e) {
                logger.error("上传文件出错!", e);
                upload = false;
            } finally {
                try {
                    fileInputStream.close();
                    ftpClient.disconnect();
                } catch (IOException e) {
                    logger.error("关闭写入文件流时出错，或者ftpClient关闭异常!");
                }

            }

        }
        return upload;
    }


    private boolean connectServer(String ip, Integer port, String user, String password) {
        boolean isSuccess = true;
        ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip);
            isSuccess = ftpClient.login(user, password);
            ftpClient.setBufferSize(1024);
        } catch (IOException e) {
            logger.error("FTP连接出错", e);
        }
        return isSuccess;
    }


}
