package nintaco.cheats;

import nintaco.App;
import nintaco.files.CartFile;
import nintaco.input.InputUtil;
import nintaco.input.other.SetCheats;
import nintaco.preferences.AppPrefs;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nintaco.files.FileUtil.createCheatFile;
import static nintaco.files.FileUtil.mkdir;
import static nintaco.util.CollectionsUtil.isBlank;
import static nintaco.util.StringUtil.isBlank;

public final class GameCheats {

    private static final Pattern ADDRESS_PATTERN = Pattern.compile(
            "^(.*)(\\p{XDigit}{4}:)(.*)$");

    private static String fileName;
    private static List<Cheat> cheatsList;

    private GameCheats() {
    }

    private static String getText(final Element element, final String tagName) {
        final NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList != null && nodeList.getLength() > 0) {
            final Element e = (Element) nodeList.item(0);
            if (e != null) {
                final Node node = e.getFirstChild();
                if (node != null) {
                    final String text = node.getNodeValue();
                    if (text != null) {
                        return text.trim();
                    }
                }
            }
        }
        return null;
    }

    private static Integer getInt(final Element element, final String tagName) {
        return getInt(getText(element, tagName));
    }

    private static Integer getInt(final String text) {
        if (text == null || text.length() < 4) {
            return null;
        }
        try {
            return Integer.parseInt(text.substring(2), 16);
        } catch (Throwable t) {
            return null;
        }
    }

    private static Cheat getCheat(final Element element) {
        final Integer address = getInt(element, "address");
        final Integer dataValue = getInt(element, "value");
        if (address == null || dataValue == null || address < 0x0000
                || address > 0xFFFF || dataValue < 0x00 || dataValue > 0xFF) {
            return null;
        }
        Integer compareValue = getInt(element, "compare");
        if (compareValue != null && (compareValue < 0x00 || compareValue > 0xFF)) {
            return null;
        }
        if (compareValue == null) {
            compareValue = -1;
        }
        final String description = getText(element, "description");
        boolean enabled = true;
        String enabledStr = element.getAttribute("enabled");
        if (!isBlank(enabledStr)) {
            enabledStr = enabledStr.trim();
            if ("0".equals(enabledStr)) {
                enabled = false;
            } else if (!"1".equals(enabledStr)) {
                return null;
            }
        }

        final Cheat cheat = new Cheat(address, dataValue, compareValue);
        cheat.setEnabled(enabled);
        if (description != null) {
            cheat.setDescription(description);
        }
        return cheat;
    }

    public static List<Cheat> loadXML(final File file) throws Throwable {

        final List<Cheat> cheats = new ArrayList<>();
        if (!file.exists()) {
            return cheats;
        }

        final Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(file);
        final Element root = doc.getDocumentElement();
        if (root != null) {
            final NodeList cs = root.getElementsByTagName("cheat");
            if (cs != null) {
                for (int i = 0; i < cs.getLength(); i++) {
                    final Cheat cheat = getCheat((Element) cs.item(i));
                    if (cheat != null) {
                        cheats.add(cheat);
                    }
                }
            }
        }

        return cheats;
    }

    private static String escapeString(final String s) {
        if (s == null) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            switch (c) {
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&apos;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }

    public static void saveXML(File file, List<Cheat> cheats) throws Throwable {
        saveXML(file, cheats, false, 0);
    }

    public static void saveXML(File file, List<Cheat> cheats, boolean isNesFile,
                               int nesFileCRC) throws Throwable {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(
                new FileWriter(file)))) {
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            out.println("<cheats version=\"1.0\">");
            for (Cheat cheat : cheats) {
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("    <cheat enabled=\"%d\"",
                        cheat.isEnabled() ? 1 : 0));
                if (isNesFile) {
                    sb.append(String.format(" game=\"0x%08X\"", nesFileCRC));
                }
                sb.append('>');
                out.println(sb);
                out.format("        <address>0x%04X</address>%n", cheat.getAddress());
                out.format("        <value>0x%02X</value>%n", cheat.getDataValue());
                if (cheat.hasCompareValue()) {
                    out.format("        <compare>0x%02X</compare>%n",
                            cheat.getCompareValue());
                }
                if (!isBlank(cheat.getDescription())) {
                    out.format("        <description>%s</description>%n",
                            escapeString(cheat.getDescription()));
                }
                out.println("    </cheat>");
            }
            out.println("</cheats>");
        }
    }

    public static List<Cheat> loadCHT(final File file) throws Throwable {

        final List<Cheat> cheats = new ArrayList<>();
        if (!file.exists()) {
            return cheats;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String input = null;
            while ((input = br.readLine()) != null) {
                if (isBlank(input)) {
                    continue;
                }
                Cheat cheat = new Cheat();
                input = input.trim();

                final Matcher matcher = ADDRESS_PATTERN.matcher(input);
                if (!matcher.find()) {
                    continue;
                }
                final String prefix = matcher.group(1);

                int sIndex = -1;
                int cIndex = -1;
                int colonIndex = -1;
                for (int i = 0, length = Math.min(3, prefix.length()); i < length; i++) {
                    switch (prefix.charAt(i)) {
                        case 's':
                        case 'S':
                            if (sIndex < 0) {
                                sIndex = i;
                            }
                            break;
                        case 'c':
                        case 'C':
                            if (cIndex < 0) {
                                cIndex = i;
                            }
                            break;
                        case ':':
                            if (colonIndex < 0) {
                                colonIndex = i;
                            }
                            break;
                    }
                }
                if ((cIndex >= 0 && sIndex > cIndex)
                        || (colonIndex >= 0 && sIndex > colonIndex)
                        || (colonIndex >= 0 && cIndex > colonIndex)) {
                    continue;
                }
                cheat.setEnabled(colonIndex < 0);
                int i = Math.max(Math.max(sIndex, cIndex), colonIndex) + 1;
                try {
                    cheat.setAddress(Integer.parseInt(input.substring(i, i + 4), 16));
                    if (input.charAt(i + 4) != ':') {
                        continue;
                    }
                } catch (Throwable t) {
                    continue;
                }
                i += 5;
                try {
                    cheat.setDataValue(Integer.parseInt(input.substring(i, i + 2), 16));
                    if (input.charAt(i + 2) != ':') {
                        continue;
                    }
                } catch (Throwable t) {
                    continue;
                }
                i += 3;
                if (cIndex >= 0) {
                    try {
                        cheat.setCompareValue(Integer.parseInt(input.substring(i, i + 2),
                                16));
                        if (input.charAt(i + 2) != ':') {
                            continue;
                        }
                    } catch (Throwable t) {
                        continue;
                    }
                    i += 3;
                }
                if (i < input.length()) {
                    cheat.setDescription(input.substring(i));
                }
                cheats.add(cheat);
            }
        }

        return cheats;
    }

    public static void saveCHT(final File file, final List<Cheat> cheats)
            throws Throwable {
        if (isBlank(cheats)) {
            file.delete();
        } else {
            try (PrintWriter out = new PrintWriter(new BufferedWriter(
                    new FileWriter(file)))) {
                for (Cheat cheat : cheats) {
                    StringBuilder sb = new StringBuilder();
                    sb.append('S');
                    if (cheat.hasCompareValue()) {
                        sb.append('C');
                    }
                    if (!cheat.isEnabled()) {
                        sb.append(':');
                    }
                    sb.append(String.format("%04x:%02x:", cheat.getAddress(),
                            cheat.getDataValue()));
                    if (cheat.hasCompareValue()) {
                        sb.append(String.format("%02x:", cheat.getCompareValue()));
                    }
                    if (cheat.getDescription() != null) {
                        sb.append(cheat.getDescription());
                    }
                    out.println(sb);
                }
            }
        }
    }

    public static void addCheat(final Cheat cheat, final boolean overwrite) {
        addCheat(cheat, overwrite, getCheats());
    }

    public static void addCheat(final Cheat cheat, final boolean overwrite,
                                final List<Cheat> cheats) {

        if (overwrite) {
            for (final Cheat c : cheats) {
                if (c.effectivelyEquals(cheat)) {
                    c.setDescription(cheat.getDescription());
                    c.setEnabled(cheat.isEnabled());
                    return;
                }
            }
        }

        cheats.add(cheat);
    }

    public static boolean removeCheat(final Cheat cheat) {
        return removeCheat(cheat, getCheats());
    }

    public static boolean removeCheat(final Cheat cheat,
                                      final List<Cheat> cheats) {
        boolean cheatRemoved = false;
        for (final Iterator<Cheat> i = cheats.iterator(); i.hasNext(); ) {
            final Cheat c = i.next();
            if (c.effectivelyEquals(cheat)) {
                i.remove();
                cheatRemoved = true;
            }
        }
        return cheatRemoved;
    }

    public synchronized static List<Cheat> getCopy() {
        final List<Cheat> cheats = new ArrayList<>();
        if (cheatsList != null) {
            for (final Cheat cheat : cheatsList) {
                cheats.add(new Cheat(cheat));
            }
        }
        return cheats;
    }

    public synchronized static List<Cheat> getCheats() {
        return cheatsList;
    }

    public synchronized static void setCheats(final List<Cheat> cheatsList) {
        GameCheats.cheatsList = cheatsList;
    }

    public synchronized static void updateMachine() {
        if (App.getMachine() != null) {
            InputUtil.addOtherInput(new SetCheats(getEnabledCheats()));
        }
    }

    public synchronized static Cheat[] getEnabledCheats() {
        if (cheatsList == null) {
            return null;
        }
        int size = 0;
        for (final Cheat cheat : cheatsList) {
            if (cheat.isEnabled()) {
                size++;
            }
        }
        Cheat[] cheats = null;
        if (size > 0) {
            cheats = new Cheat[size];
            int i = 0;
            for (Cheat cheat : cheatsList) {
                if (cheat.isEnabled()) {
                    cheats[i++] = new Cheat(cheat);
                }
            }
        }
        return cheats;
    }

    public synchronized static void load(final String entryFileName) {

        fileName = createCheatFile(entryFileName);
        List<Cheat> cheats = null;
        try {
            final File file = new File(fileName);
            if (file.exists()) {
                cheats = loadCHT(file);
            }
        } catch (Throwable t) {
            //t.printStackTrace();
        }
        if (cheats == null) {
            cheats = new ArrayList<>();
        }
        setCheats(cheats);
        updateMachine();
    }

    public static void save() {
        final String file = fileName;
        if (isBlank(file)) {
            return;
        }
        final List<Cheat> cheats = cheatsList;
        if (cheats == null) {
            return;
        }
        new Thread(() -> {
            synchronized (GameCheats.class) {
                mkdir(AppPrefs.getInstance().getPaths().getCheatsDir());
                try {
                    saveCHT(new File(file), cheats);
                } catch (Throwable t) {
                    //t.printStackTrace();
                }
            }
        }).run();
    }

    public static List<Cheat> queryCheatsDB() {
        final CartFile cartFile = App.getCartFile();
        if (cartFile != null) {
            final CheatsDBEntry[] entries = CheatsDB.getCheats(cartFile.getFileCRC());
            if (entries != null) {
                final List<Cheat> cheats = new ArrayList<>();
                for (final CheatsDBEntry entry : entries) {
                    final String[][] gameGenieCodes = entry.getGameGenieCodes();
                    for (int j = 0; j < gameGenieCodes.length; j++) {
                        final String[] codes = gameGenieCodes[j];
                        for (int k = 0; k < codes.length; k++) {
                            final StringBuilder sb = new StringBuilder();
                            sb.append(entry.getDescription());
                            if (j > 0) {
                                sb.append(String.format(" [a%d]", j));
                            }
                            if (codes.length > 1) {
                                sb.append(String.format(" [%d of %d]", k + 1, codes.length));
                            }
                            final Cheat cheat = GameGenie.convert(codes[k]);
                            cheat.setEnabled(false);
                            cheat.setDescription(sb.toString());
                            cheats.add(cheat);
                        }
                    }
                }
                return cheats;
            }
        }
        return null;
    }
}