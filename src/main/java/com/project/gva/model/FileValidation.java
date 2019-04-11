package com.project.gva.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.gva.exception.NotFoundException;
import com.project.gva.service.hash.JacksumHasher;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.CRC32;

@Data
public class FileValidation {

    private int byteCount;

    private String file;

    @JsonIgnore
    private byte[] bytes;

    private FileValidation(byte[] bytes) {
        this.bytes = bytes;
        this.byteCount = bytes.length;
    }

    public static FileValidation of(File file) {
        try {
            return new FileValidation(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new NotFoundException(e.getMessage());
        }
    }

    public String getCrc32() {
        CRC32 crc32 = new CRC32();
        crc32.update(bytes);
        return Long.toString(crc32.getValue());
    }

    public String getCksum() {
        JacksumHasher hasher = new JacksumHasher("cksum");
        return Long.toString(hasher.hash(bytes, 0, bytes.length));
    }

    public String getSha256() {
        JacksumHasher hasher = new JacksumHasher("sha256");
        return hasher.hexHash(bytes, 0, bytes.length);
    }
}
