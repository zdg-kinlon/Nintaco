package nintaco.palettes;

public enum PalettePPU {

    _2C02("2C02", 0, new int[]{
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
            16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
            32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47,
            48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63,
    }),
    _2C03_2C05("2C03/2C05", 1, new int[]{
            0, 1, 2, 19, 4, 30, 29, 13, 15, 31, 9, 12, 14, 13, 14, 15,
            16, 17, 18, 3, 37, 5, 7, 8, 24, 26, 25, 10, 46, 29, 30, 31,
            32, 33, 34, 52, 35, 51, 23, 39, 40, 41, 58, 27, 28, 45, 46, 47,
            48, 49, 47, 61, 50, 21, 53, 54, 55, 56, 43, 59, 60, 44, 62, 63,
    }),
    RP2C04_0001("RP2C04-0001", 2, new int[]{
            9, 31, 45, 32, 12, 40, 27, 18, 26, 5, 56, 37, 59, 26, 26, 26,
            50, 57, 23, 53, 61, 11, 41, 14, 62, 58, 49, 51, 29, 26, 26, 26,
            15, 16, 3, 42, 1, 34, 55, 10, 13, 19, 63, 38, 4, 6, 47, 47,
            8, 33, 48, 47, 21, 7, 0, 22, 17, 60, 36, 25, 20, 28, 48, 48,
    }),
    RP2C04_0002("RP2C04-0002", 3, new int[]{
            42, 53, 39, 26, 32, 30, 22, 37, 0, 43, 16, 52, 49, 0, 0, 30,
            38, 36, 44, 31, 5, 61, 58, 29, 2, 15, 55, 62, 63, 30, 30, 30,
            12, 17, 23, 11, 13, 35, 51, 1, 46, 21, 4, 20, 6, 24, 45, 45,
            47, 7, 45, 56, 28, 54, 60, 33, 48, 10, 57, 27, 14, 19, 56, 56,
    }),
    RP2C04_0003("RP2C04-0003", 4, new int[]{
            24, 8, 27, 34, 41, 26, 49, 47, 9, 7, 33, 63, 36, 9, 9, 9,
            3, 44, 19, 25, 1, 18, 52, 11, 21, 4, 20, 56, 45, 26, 26, 26,
            39, 42, 16, 30, 32, 51, 22, 37, 54, 40, 2, 23, 17, 55, 46, 46,
            5, 6, 46, 57, 58, 12, 31, 10, 14, 59, 48, 61, 15, 13, 57, 57,
    }),
    RP2C04_0004("RP2C04-0004", 5, new int[]{
            38, 6, 35, 11, 28, 20, 17, 9, 4, 63, 43, 40, 44, 20, 20, 20,
            8, 42, 18, 1, 16, 32, 34, 50, 0, 14, 27, 33, 46, 29, 29, 29,
            54, 31, 26, 19, 58, 48, 7, 57, 3, 60, 30, 25, 2, 21, 56, 56,
            49, 59, 56, 29, 61, 39, 5, 12, 24, 45, 62, 53, 55, 41, 56, 56,
    });

    private final String name;
    private final int index;
    private final int[] map;    // 2C02 -> Other (palette conversion)

    PalettePPU(final String name, final int index, final int[] map) {
        this.name = name;
        this.index = index;
        this.map = map;
    }

    public int getIndex() {
        return index;
    }

    public int[] getMap() {
        return map;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}