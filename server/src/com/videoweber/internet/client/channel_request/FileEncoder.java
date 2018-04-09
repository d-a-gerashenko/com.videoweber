package com.videoweber.internet.client.channel_request;

import com.videoweber.lib.common.BytesConverter;
import com.videoweber.lib.common.Encryptor;
import java.io.File;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class FileEncoder {

    private final Encryptor encryptor;

    public FileEncoder(String encryptionKey) {
        encryptor = new Encryptor(encryptionKey);
    }

    public String encode(File file) {
        return BytesConverter.bytesToString(
                encryptor.encrypt(
                        BytesConverter.fileToBytes(file)
                )
        );
    }

    public void decode(String string, File file) {
        BytesConverter.fileFromBytes(
                encryptor.decrypt(
                        BytesConverter.bytesFromString(string)
                ),
                file
        );
    }

}
