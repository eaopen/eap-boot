package org.openea.eap.extj.util;

import lombok.Data;
import lombok.experimental.Accessors;
import org.openea.eap.module.infra.dal.dataobject.file.FileDO;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class FileInfo extends FileDO implements Serializable {
    public void setFilename(String fileName) {
        this.setName(fileName);
    }

    public String getFilename() {
        return this.getName();
    }
}
