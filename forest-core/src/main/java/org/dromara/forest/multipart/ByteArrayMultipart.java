package org.dromara.forest.multipart;

import org.dromara.forest.exceptions.ForestNoFileNameException;
import org.dromara.forest.utils.StringUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

public class ByteArrayMultipart extends ForestMultipart<byte[], ByteArrayMultipart> {

    private byte[] bytes;

    @Override
    public String getOriginalFileName() {
        if (StringUtil.isBlank(fileName)) {
            throw new ForestNoFileNameException(byte[].class);
        }
        return fileName;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public ByteArrayMultipart setData(byte[] data) {
        this.bytes = data;
        return this;
    }


    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(bytes);
    }

    @Override
    public long getSize() {
        return bytes.length;
    }

    @Override
    public boolean isFile() {
        return false;
    }

    @Override
    public File getFile() {
        return null;
    }


    @Override
    public byte[] getBytes() {
        return bytes;
    }
}
