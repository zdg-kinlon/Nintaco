package cn.kinlon.emu.palettes;

import java.io.*;
import java.util.*;

import cn.kinlon.emu.preferences.AppPrefs;


public class Palettes implements Serializable {

    private static final long serialVersionUID = 0;

    final Map<PalettePPU, String> ppuPaletteMapping = new HashMap<>();
    final Map<String, int[]> originalPalettes = new HashMap<>();
    final Map<String, int[]> modifiedPalettes = new HashMap<>();

    public void setPpuPaletteMapping(
            final Map<PalettePPU, String> ppuPaletteMapping) {
        synchronized (AppPrefs.class) {
            this.ppuPaletteMapping.clear();
            this.ppuPaletteMapping.putAll(ppuPaletteMapping);
        }
    }

    public void getPpuPaletteMapping(
            final Map<PalettePPU, String> ppuPaletteMapping) {
        synchronized (AppPrefs.class) {
            ppuPaletteMapping.clear();
            ppuPaletteMapping.putAll(this.ppuPaletteMapping);
        }
    }

    public void setPalette(final String name, final int[] palette) {
        synchronized (AppPrefs.class) {
            {
                final int[] p = new int[64];
                System.arraycopy(palette, 0, p, 0, 64);
                modifiedPalettes.put(name, p);
            }
            if (!originalPalettes.containsKey(name)) {
                final int[] p = new int[64];
                System.arraycopy(palette, 0, p, 0, 64);
                originalPalettes.put(name, p);
            }
        }
    }

    public boolean getPalette(final PalettePPU palettePPU, final int[] palette) {
        return getPalette(ppuPaletteMapping.get(palettePPU), palette);
    }

    public boolean getPalette(final String name, final int[] palette) {
        if (name == null || palette == null || palette.length < 64) {
            return false;
        }
        synchronized (AppPrefs.class) {
            int[] p = modifiedPalettes.get(name);
            if (p == null) {
                p = originalPalettes.get(name);
            }
            if (p != null) {
                System.arraycopy(p, 0, palette, 0, 64);
                return true;
            }
        }
        return false;
    }

    public void getPaletteNames(final List<String> names) {
        synchronized (AppPrefs.class) {
            names.clear();
            names.addAll(originalPalettes.keySet());
            Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
        }
    }
}
