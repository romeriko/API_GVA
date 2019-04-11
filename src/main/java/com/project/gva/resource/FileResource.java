package com.project.gva.resource;

import com.project.gva.model.FileValidation;
import com.project.gva.model.Types;
import com.project.gva.service.aws.AwsService;
import com.project.gva.service.hash.JacksumHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
@RequestMapping(value = "file")
public class FileResource {

    private final
    AwsService awsService;

    @Autowired
    public FileResource(AwsService awsService) {
        this.awsService = awsService;
    }

    @GetMapping(value = "verify/update")
    public ResponseEntity file(@RequestParam(name = "file") String fileName) throws Exception {
        File file = awsService.download(fileName, Types.File.UPDATE);


        return ResponseEntity.ok(FileValidation.of(file));
    }

}
