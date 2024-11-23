package nintaco.netplay.protocol;

import java.io.*;

import nintaco.*;
import nintaco.files.*;
import nintaco.movie.MovieBlock;

public class FileResponse implements Serializable {

    private static final long serialVersionUID = 0;

    private final int fileRequestID;
    private final IFile file;
    private final Machine machine;
    private final int movieFrameIndex;
    private final boolean forwardTime;
    private final MovieBlock currentMovieBlock;
    private final MovieBlock movieBlock;
    private final boolean multitap;
    private final int console;
    private final String[] quickSaveStateMenuNames;
    private final String fileInfo;

    public FileResponse(
            final int fileRequestID,
            final IFile file,
            final Machine machine,
            final int movieFrameIndex,
            final boolean forwardTime,
            final MovieBlock currentMovieBlock,
            final MovieBlock movieBlock,
            final int console,
            final boolean multitap,
            final String[] quickSaveStateMenuNames,
            final String fileInfo) {
        this.fileRequestID = fileRequestID;
        this.file = file;
        this.machine = machine;
        this.movieFrameIndex = movieFrameIndex;
        this.forwardTime = forwardTime;
        this.currentMovieBlock = currentMovieBlock;
        this.movieBlock = movieBlock;
        this.console = console;
        this.multitap = multitap;
        this.quickSaveStateMenuNames = quickSaveStateMenuNames;
        this.fileInfo = fileInfo;
    }

    public MovieBlock getCurrentMovieBlock() {
        return currentMovieBlock;
    }

    public MovieBlock getMovieBlock() {
        return movieBlock;
    }

    public boolean isForwardTime() {
        return forwardTime;
    }

    public int getFileRequestID() {
        return fileRequestID;
    }

    public IFile getFile() {
        return file;
    }

    public Machine getMachine() {
        return machine;
    }

    public int getMovieFrameIndex() {
        return movieFrameIndex;
    }

    public boolean isMultitap() {
        return multitap;
    }

    public int getConsole() {
        return console;
    }

    public String[] getQuickSaveStateMenuNames() {
        return quickSaveStateMenuNames;
    }

    public String getFileInfo() {
        return fileInfo;
    }
}
