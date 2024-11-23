package nintaco.gui.hexeditor;

import nintaco.MessageException;

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class CharTable {

    private final char[] chars = new char[256];
    private final String[] strings = new String[256];
    private final int[] values = new int[256];

    public CharTable() {
        for (int i = 0; i < 256; i++) {
            if (i >= 0x20 && i <= 0x7E) {
                chars[i] = (char) i;
                values[i] = i;
            } else {
                chars[i] = '.';
                values[i] = -1;
            }
        }
        createStrings();
    }

    public CharTable(String fileName) throws Throwable {
        this(new File(fileName));
    }

    public CharTable(File file) throws Throwable {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            loadFile(br);
        }
    }

    public CharTable(BufferedReader br) throws Throwable {
        loadFile(br);
    }

    private void createStrings() {
        for (int i = 0; i < 256; i++) {
            strings[i] = Character.toString(chars[i]);
        }
    }

    private void loadFile(BufferedReader br) throws Throwable {
        for (int i = 0; i < 256; i++) {
            values[i] = -1;
            chars[i] = (char) -1;
        }
        String input = null;
        int line = 0;
        while ((input = br.readLine()) != null) {
            line++;
            input = input.trim();
            if (input.isEmpty()) {
                continue;
            }
            String[] tokens = input.split("=");
            if (tokens.length != 2 || tokens[0].length() < 2
                    || tokens[1].length() < 1) {
                throwWrongForm(line);
            }
            int value = 0;
            try {
                value = Integer.parseInt(tokens[0].substring(0, 2), 16);
            } catch (Throwable t) {
                throwWrongForm(line);
            }
            char c = tokens[1].toLowerCase().startsWith("ret") ? KeyEvent.VK_ENTER
                    : tokens[1].charAt(0);
            if (values[c & 0xFF] != -1 || chars[value] != (char) -1) {
                throw new MessageException(
                        "Error on line %d: Duplicate character mapping.", line);
            }
            chars[value] = c;
            values[c & 0xFF] = value;
        }
        for (int i = 0; i < 256; i++) {
            if (chars[i] == (char) -1) {
                chars[i] = '.';
            }
        }
        createStrings();
    }

    private void throwWrongForm(int line) throws MessageException {
        throw new MessageException("Error on line %d: Expected xx=c.", line);
    }

    public String getString(int value) {
        return strings[value & 0xFF];
    }

    public char getChar(int value) {
        return chars[value & 0xFF];
    }

    public int getValue(char c) {
        return values[c & 0xFF];
    }
}
