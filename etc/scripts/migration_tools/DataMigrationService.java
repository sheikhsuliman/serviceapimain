package com.siryus.swisscon.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siryus.swisscon.api.Application;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.*;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class)
public class DataMigrationService extends AbstractBaseServiceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataMigrationService.class);

    private final FileService fileService;

    @Autowired
    public DataMigrationService(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * Load Pictures from Url's and then upload it to S3
     */
    public void saveFiles() {

        byte[] imageBytes = new byte[]{};

        for (int i = 225; i <= 427; i++) {

            try {
                File file = fileService.findById(i);

                String basePath = new StringBuffer(File.class.getSimpleName())
                        .append('/').append(file.getId()).append('/').toString();
                String propertyName = "url";
                Field fileField = SpecificationUtils.getField(File.class, propertyName);

                String fakeUrl = "https://sc-dev-private-storage.s3.amazonaws.com/File/20/url_800x800";


                RestTemplate restTemplate = new RestTemplate();
                System.out.println(file.getUrl());
                if (imageBytes.length == 0) {
                    imageBytes = restTemplate.getForObject(fakeUrl, byte[].class);
                }

                BASE64DecodedMultipartFile file = new BASE64DecodedMultipartFile(imageBytes, "image/png");
                String url = this.filePersistenceService.saveFile(fileField, file, basePath + propertyName);

                System.out.println("FILE FINISHED");
                System.out.println(url);
            } catch (Exception e) {
                System.out.print(e);
            }
            if (i >= 102 && i <= 217) {
                continue;
            }


        }


    }

    /**
     * Load Pictures from Harddisk and then upload it to S3
     */
    @Test
    public void jsonFileToFiles() throws IOException {
        String jsonPath = "C:\\Users\\Richard\\Downloads\\ettingen_location_files\\migration.json";


        ObjectMapper mapper = new ObjectMapper();

        HashMap result = mapper.readValue(new File(jsonPath), HashMap.class);
        List<LinkedHashMap> allFiles = new LinkedList<>();
        addFiles(allFiles, result);

        for (int i = 1; i <= 955; i++) {

            try {
                File file = fileService.findById(i);

                String basePath = new StringBuffer(File.class.getSimpleName())
                        .append('/').append(file.getId()).append('/').toString();
                String propertyName = "url";
                Field fileField = SpecificationUtils.getField(File.class, propertyName);

                // get the bytes
                final Integer id = Integer.valueOf(i);

                Optional<LinkedHashMap> localFile = allFiles.stream().filter(lhm -> lhm.get("newFileId").equals(id)).findFirst();

                localFile.ifPresent(file -> {
                    if (((String) file.get("mimeType")).contains("image")) {
                        try {
                            File fi = new File("C:/Users/Richard/Downloads/ettingen_location_files/" + file.get("file"));
                            byte[] fileContent = Files.readAllBytes(fi.toPath());
                            BASE64DecodedMultipartFile multipartFile = new BASE64DecodedMultipartFile(fileContent, "image/png");
                            String url = this.filePersistenceService.saveFile(fileField, multipartFile, basePath + propertyName);
                            System.out.println(url);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });


            } catch (Exception e) {
                System.out.print(e);
            }
        }


    }

    private void addFiles(List<LinkedHashMap> allFiles, HashMap map) {

        List<LinkedHashMap> files = (List<LinkedHashMap>) map.get("files");
        System.out.print(files);
        allFiles.addAll(files);

        List<LinkedHashMap> children = (List<LinkedHashMap>) map.get("children");
        children.forEach(lhm -> addFiles(allFiles, lhm));

    }


    /*
     *<p>
     * Trivial implementation of the {@link MultipartFile} interface to wrap a byte[] decoded
     * from a BASE64 encoded String
     *</p>
     */
    public class BASE64DecodedMultipartFile implements MultipartFile {
        private final byte[] content;
        private final String contentType;

        public BASE64DecodedMultipartFile(byte[] content, String contentType) {
            this.content = content;
            this.contentType = contentType;
        }

        @NotNull
        @Override
        public String getName() {
            return "";
        }

        @Override
        public String getOriginalFilename() {
            return "";
        }

        @Override
        public String getContentType() {
            return this.contentType;
        }

        @Override
        public boolean isEmpty() {
            return content == null || content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @NotNull
        @Override
        public byte[] getBytes() {
            return content;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(File dest) {
            //
        }
    }


}
