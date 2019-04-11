package com.project.gva.service.aws;

import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.Tag;
import com.condos.shared.aws.FileRequest;
import com.condos.shared.aws.S3;
import com.project.gva.model.Types;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service(value = "defaultAwsService")
public class DefaultAwsService implements AwsService {

    private final String AWS_ACCESS_KEY;
    private final String AWS_SECRET_KEY;
    private final String S3_BASE_URL;
    private final String S3_MAIN_BUCKET;

    public DefaultAwsService(@Value(value = "${aws.s3.baseUrl}") String s3BaseUrl,
                             @Value(value = "${aws.s3.mainBucket}") String mainBucket,
                             @Value(value = "${aws.accessKey}") String accessKey,
                             @Value(value = "${aws.secretKey}") String secretKey) {
        this.AWS_ACCESS_KEY = accessKey;
        this.AWS_SECRET_KEY = secretKey;
        this.S3_BASE_URL = s3BaseUrl;
        this.S3_MAIN_BUCKET = mainBucket;
    }

    @Override
    public File download(String file, Types.File fileType) {

        S3.Config config = S3.Config.builder()
                .AWS_ACCESS_KEY(AWS_ACCESS_KEY)
                .AWS_SECRET_KEY(AWS_SECRET_KEY)
                .BUCKET_NAME(fileType.bucket())
                .S3_BASE_URL(S3_BASE_URL)
                .DEFAULT_BUCKET_NAME(S3_MAIN_BUCKET)
                .build();

        FileRequest request = FileRequest.builder().extension(FileRequest.FileExtension.BIN).fileName(file.concat(FileRequest.FileExtension.BIN.extension())).build();
        try {
            S3.Reader.downloadFile(request, config);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new File(request.getExtension().temp());
    }

    @Override
    public List<Tag> downloadTagging(String file, Types.File fileType) {

        S3.Config config = S3.Config.builder()
                .AWS_ACCESS_KEY(AWS_ACCESS_KEY)
                .AWS_SECRET_KEY(AWS_SECRET_KEY)
                .BUCKET_NAME(fileType.bucket())
                .S3_BASE_URL(S3_BASE_URL)
                .DEFAULT_BUCKET_NAME(S3_MAIN_BUCKET)
                .build();

        FileRequest request = FileRequest.builder().extension(FileRequest.FileExtension.BIN).fileName(file.concat(FileRequest.FileExtension.BIN.extension())).build();

        return S3.Reader.readFileTags(config, request).getTagSet();
    }

    @Override
    public ListObjectsV2Result readFiles(Types.File fileType) {

        S3.Config config = S3.Config.builder()
                .AWS_ACCESS_KEY(AWS_ACCESS_KEY)
                .AWS_SECRET_KEY(AWS_SECRET_KEY)
                .BUCKET_NAME(fileType.bucket())
                .S3_BASE_URL(S3_BASE_URL)
                .DEFAULT_BUCKET_NAME(S3_MAIN_BUCKET)
                .build();

        return S3.Reader.readFiles(config, 100);
    }
}
