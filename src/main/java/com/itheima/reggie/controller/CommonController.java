package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

/*
 * 文件上传与下载
 * */
@RestController
@RequestMapping("/common")
public class CommonController {
    @Value("${reggie.path}")
    private String basePath;

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {   //参数名字必须与前端保持一致
        //直接使用图片原始名字时,若上传图片名字一致会覆盖掉原图片,必须保证每个图片文件名字唯一
        String originalFilename = file.getOriginalFilename();   //获取上传图片的原始名字
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));  //获取文件后缀  .jpg  .png等
        String fileName = UUID.randomUUID() + suffix; //使用随机生成的名字加后缀名拼接
        //创建指定目录
        File dir = new File(basePath);
        if (!dir.exists()) {
            dir.mkdirs(); //如果目录不存在则创建
        }
        //将图片移动到指定目录
        try {
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return R.success(fileName);
    }

    @GetMapping("/download")
    public void download(HttpServletResponse response, String name) {
        try {
            //输入流读取文件内容
            FileInputStream fis = new FileInputStream(new File(basePath + name));
            //输出流,将读取的文件重新响应会浏览器,在浏览器展示图片
            ServletOutputStream outputStream = response.getOutputStream();
            response.setContentType("image/jpeg");
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = fis.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }
            outputStream.close();
            fis.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
