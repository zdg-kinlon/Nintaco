package nintaco.palettes;

import java.io.*;
import java.util.*;

import nintaco.preferences.*;

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

    public boolean getOriginalPalette(final String name, final int[] palette) {
        synchronized (AppPrefs.class) {
            final int[] p = originalPalettes.get(name);
            if (p != null) {
                System.arraycopy(p, 0, palette, 0, 64);
                return true;
            }
        }
        return false;
    }

    public void setPalettes(final Map<String, int[]> palettes) {
        synchronized (AppPrefs.class) {
            for (final Map.Entry<String, int[]> entry : palettes.entrySet()) {
                setPalette(entry.getKey(), entry.getValue());
            }
            for (final Iterator<Map.Entry<String, int[]>> i = originalPalettes
                    .entrySet().iterator(); i.hasNext(); ) {
                final Map.Entry<String, int[]> entry = i.next();
                final String name = entry.getKey();
                if (!(PaletteUtil.isStandardPaletteName(name)
                        || palettes.containsKey(name))) {
                    i.remove();
                    modifiedPalettes.remove(name);
                }
            }
        }
    }

    public void getPalettes(final Map<String, int[]> palettes) {
        synchronized (AppPrefs.class) {
            palettes.clear();
            for (final String name : originalPalettes.keySet()) {
                final int[] palette = new int[64];
                getPalette(name, palette);
                palettes.put(name, palette);
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
