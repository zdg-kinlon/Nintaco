package nintaco.gui.hexeditor;

import nintaco.files.FdsFile;
import nintaco.files.NesFile;
import nintaco.files.UnifFile;
import nintaco.mappers.Mapper;

public class FileDataSource extends DataSource {

    private final int prgRomAddress;
    private final int chrRomAddress;
    private final Mapper mapper;

    public FileDataSource(final NesFile nesFile, final Mapper mapper) {
        super(nesFile.getFileContents());
        this.mapper = mapper;
        prgRomAddress = nesFile.getHeaderSize() + nesFile.getTrainerSize();
        chrRomAddress = prgRomAddress + nesFile.getPrgRomSize();
    }

    public FileDataSource(final FdsFile fdsFile) {
        super(fdsFile.getFileContents());
        mapper = null;
        prgRomAddress = -1;
        chrRomAddress = -1;
    }

    public FileDataSource(final UnifFile unifFile) {
        super(unifFile.getFileContents());
        mapper = null;
        prgRomAddress = -1;
        chrRomAddress = -1;
    }

    @Override
    public int peek(final int address) {
        return address >= 0 && address < cache.length
                ? cache[address] : 0;
    }

    @Override
    public void write(final int address, final int value) {
        if (address >= 0 && address < cache.length) {
            cache[address] = value;
            if (mapper != null) {
                if (address >= chrRomAddress) {
                    mapper.writeChrRom(address - chrRomAddress, value);
                } else if (address >= prgRomAddress) {
                    mapper.writePrgRom(address - prgRomAddress, value);
                }
            }
        }
    }

    @Override
    public void refreshCache() {
    }

    @Override
    public int getSize() {
        return cache.length;
    }

    @Override
    public int getIndex() {
        return FileContents;
    }
}