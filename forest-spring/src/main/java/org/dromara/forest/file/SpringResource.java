package org.dromara.forest.file;

import org.dromara.forest.exceptions.ForestRuntimeException;
import org.dromara.forest.multipart.ForestMultipart;
import org.dromara.forest.utils.StringUtil;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class SpringResource extends ForestMultipart<Resource, SpringResource> {

    private Resource resource;

    @Override
    public SpringResource setData(Resource data) {
        this.resource = data;
        return this;
    }

    @Override
    public String getOriginalFileName() {
        if (StringUtil.isNotBlank(fileName)) {
            return fileName;
        }
        return resource.getFilename();
    }

    @Override
    public InputStream getInputStream() {
        try {
            return resource.getInputStream();
        } catch (IOException e) {
            throw new ForestRuntimeException(e);
        }
    }

    @Override
    public long getSize() {
        try {
            return resource.contentLength();
        } catch (IOException e) {
            throw new ForestRuntimeException(e);
        }
    }

    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    public File getFile() {
        try {
            return resource.getFile();
        } catch (IOException e) {
            throw new ForestRuntimeException(e);
        }
    }
}
