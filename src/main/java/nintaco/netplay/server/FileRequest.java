package nintaco.netplay.server;

public class FileRequest {

    private final RemoteClient remoteClient;
    private final int fileRequestID;

    public FileRequest(final RemoteClient remoteClient, final int fileRequestID) {
        this.remoteClient = remoteClient;
        this.fileRequestID = fileRequestID;
    }

    public RemoteClient getRemoteClient() {
        return remoteClient;
    }

    public int getFileRequestID() {
        return fileRequestID;
    }

    @Override
    public boolean equals(final Object obj) {
        final FileRequest fileRequest = (FileRequest) obj;
        return fileRequest.remoteClient == remoteClient
                && fileRequest.fileRequestID == fileRequestID;
    }

    @Override
    public String toString() {
        return "FileRequest{" + "remoteClient=" + remoteClient
                + ", fileRequestID=" + fileRequestID + '}';
    }
}
