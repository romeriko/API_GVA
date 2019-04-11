package com.project.gva.service.aws;

import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.Tag;
import com.project.gva.model.Types;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface AwsService {
    File download(String file, Types.File fileType);

    List<Tag> downloadTagging(String file, Types.File fileType);

    ListObjectsV2Result readFiles(Types.File fileType);
}
