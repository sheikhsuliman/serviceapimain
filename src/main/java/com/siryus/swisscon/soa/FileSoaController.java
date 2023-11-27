package com.siryus.swisscon.soa;

import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.file.file.FileService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/soa/")
public class FileSoaController {

    private final FileService fileService;

    @Autowired
    public FileSoaController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping(path="file/{fileId}", produces="application/json")
    @ApiOperation(value = "Retrieve file details", notes = "This is only for non-sensitive information")
    public File getFile(@PathVariable Integer fileId) {
        return fileService.findById(fileId);
    }

}
