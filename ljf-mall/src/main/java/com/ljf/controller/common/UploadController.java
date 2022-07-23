package com.ljf.controller.common;

import com.ljf.constant.CommonConstant;
import com.ljf.utils.Result;
import com.ljf.utils.ResultGenerator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 文件上传相关
 * */
@Api(value = "文件上传相关控制层类")
@Controller
@RequestMapping("/admin")
public class UploadController {
    /**
     * 从Spring3.1开始，Spring提供了两个MultipartResolver的实现用于处理multipart请求。
     *         CommonsMultipartResolver
     *         StandardServletMultipartResolver
     *  CommonsMultipartResolver使用commons Fileupload来处理multipart请求，所以在使用时，
     *              必须要引入相应的jar包；
     *  StandardServletMultipartResolver是基于Servlet3.0来处理multipart请求的，
     *              所以不需要引用其他jar包，但是必须使用支持Servlet3.0的容器才可以。
     *
     *  Spring配置之Maven web项目：https://blog.csdn.net/just4you/article/details/70233133
     *
     * */
    @Autowired
    private StandardServletMultipartResolver standardServletMultipartResolver;

    @ApiOperation(value = "上传单个文件")
    @PostMapping({"/upload/file"})
    @ResponseBody
    public Result upload(HttpServletRequest httpServletRequest,
                         @RequestParam("file") MultipartFile file) throws URISyntaxException {
        String fileName = file.getOriginalFilename();
        // 获取除了扩展名之外的名称
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        // 生成文件名称通用方法
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Random r = new Random();
        String newFileName = sdf.format(new Date()) + r.nextInt(100) + suffixName;

        // 文件存放目录
        File fileDirectory = new File(CommonConstant.FILE_UPLOAD_PATH);
        // 创建目标文件
        File destFile = new File(CommonConstant.FILE_UPLOAD_PATH + newFileName);
        System.out.println(destFile);
        try {
            if (!fileDirectory.exists()) {
                if (!fileDirectory.mkdir()) {
                    throw new IOException("文件夹创建失败,路径为：" + fileDirectory);
                }
            }

            file.transferTo(destFile);
            Result resultSuccess = ResultGenerator.genSuccessResult();
            resultSuccess.setData("/upload/" + newFileName);
            return resultSuccess;
        } catch (IOException e) {
            e.printStackTrace();
            return ResultGenerator.genFailResult("文件上传失败");
        }
    }

    @ApiOperation(value = "上传多个文件")
    @PostMapping({"/upload/files"})
    @ResponseBody
    public Result uploadV2(HttpServletRequest httpServletRequest) {
        List<MultipartFile> multipartFiles = new ArrayList<>(8);

        /**
         * 如果是文件相关的请求
         * */
        if (standardServletMultipartResolver.isMultipart(httpServletRequest)) {
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) httpServletRequest;
            // 获取上传文件的文件名的迭代
            Iterator<String> iter = multiRequest.getFileNames();
            int total = 0;
            while (iter.hasNext()) {
                // 最多上传5张图片
                if (total > 5) {
                    return ResultGenerator.genFailResult("最多上传5张图片");
                }
                total += 1;
                MultipartFile file = multiRequest.getFile(iter.next());
                multipartFiles.add(file);
            }
        }
        if (CollectionUtils.isEmpty(multipartFiles)) {
            return ResultGenerator.genFailResult("参数异常");
        }
        if (multipartFiles.size() > 5) {
            return ResultGenerator.genFailResult("最多上传5张图片");
        }

        List<String> fileNames = new ArrayList(multipartFiles.size());
        for (int i = 0; i < multipartFiles.size(); i++) {
            // 文件全名
            String fileName = multipartFiles.get(i).getOriginalFilename();
            // 文件前缀名，即去除文件扩展名
            String suffixName = fileName.substring(fileName.lastIndexOf("."));

            //生成文件名称通用方法
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            Random r = new Random();
            String newFileName = sdf.format(new Date()) + r.nextInt(100) + suffixName;
            File fileDirectory = new File(CommonConstant.FILE_UPLOAD_PATH);
            //创建文件
            File destFile = new File(CommonConstant.FILE_UPLOAD_PATH + newFileName);

            try {
                if (!fileDirectory.exists()) {
                    if (!fileDirectory.mkdir()) {
                        throw new IOException("文件夹创建失败,路径为：" + fileDirectory);
                    }
                }
                multipartFiles.get(i).transferTo(destFile);
                fileNames.add("/upload/" + newFileName);
            } catch (IOException e) {
                e.printStackTrace();
                return ResultGenerator.genFailResult("文件上传失败");
            }
        }
        Result resultSuccess = ResultGenerator.genSuccessResult();
        resultSuccess.setData(fileNames);
        return resultSuccess;
    }

}
