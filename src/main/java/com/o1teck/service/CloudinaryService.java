package com.o1teck.service;

import com.cloudinary.Cloudinary;
//import com.cloudinary.Singleton;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

	/*
	
	
    @Autowired
    private Cloudinary cloudinaryConfig;
    
    
    public String uploadFile(MultipartFile file) {
    	
    	System.out.println();
    	System.out.println("CloudinaryService has called uploadFile()");
    	System.out.println();
    	
        try {
            File uploadedFile = convertMultiPartToFile(file);
            
            String fileName = file.getOriginalFilename();
            
            System.out.println();
            System.out.println();
            System.out.println("The original filename is " + fileName);
            System.out.println();
            System.out.println();
            
            //String fileNameTrimmed = removeLastChars(fileName,4);
            
            Map params = ObjectUtils.asMap(
            	    "public_id", "my_folder/my_sub_folder/" + fileName, 
            	    "overwrite", true,
            	    "resource_type", "image"         
            	);
           
           //Map uploadResult = cloudinaryConfig.uploader().upload(uploadedFile, ObjectUtils.emptyMap());
            // Map uploadResult = cloudinaryConfig.uploader().upload(uploadedFile, params);
            
            Cloudinary cloudinaryConfig = Singleton.getCloudinary();
            Map uploadResult = cloudinaryConfig.uploader().upload(uploadedFile, params);
            
            //I am trying to patch the problem where the image url comes back with .jpg.jpg (i.e. twice)
            String uploadResultString = uploadResult.get("url").toString();
            //String cloudinaryResponseUrl = removeLastChars(uploadResultString,4);
            
            //return cloudinaryResponseUrl;
            return uploadResultString;
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static String removeLastChars(String str, int chars) {
        return str.substring(0, str.length() - chars);
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }
*/
}