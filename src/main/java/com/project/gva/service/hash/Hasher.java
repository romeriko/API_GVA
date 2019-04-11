package com.project.gva.service.hash;

public interface Hasher {
    long hash(byte[] in, int off, int len);

    String hexHash(byte[] in, int off, int len);
}