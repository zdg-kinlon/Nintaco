package nintaco.files;

import java.io.Serializable;

public interface IFile extends Serializable {

    long serialVersionUID = 0;

    int getFileType();
}
