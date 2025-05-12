package cn.kinlon.emu.input.familybasic.transformer;

import cn.kinlon.emu.input.DeviceMapper;
import cn.kinlon.emu.input.InputDevices;
import cn.kinlon.emu.input.icons.InputIcons;

import java.io.Serializable;

public class TransformerMapper extends DeviceMapper implements Serializable {

    private static final long serialVersionUID = 0;

    private static final int[][] KEYS = {

            {-1, -1, 0}, // 0x00 void

            {2, 2, 0}, // 0x01 Esc
            {5, 6, 0}, // 0x02 1
            {8, 6, 0}, // 0x03 2
            {11, 6, 0}, // 0x04 3
            {14, 6, 0}, // 0x05 4
            {17, 6, 0}, // 0x06 5
            {20, 6, 0}, // 0x07 6
            {23, 6, 0}, // 0x08 7
            {26, 6, 0}, // 0x09 8
            {29, 6, 0}, // 0x0A 9
            {32, 6, 0}, // 0x0B 0
            {35, 6, 0}, // 0x0C -
            {38, 6, 0}, // 0x0D =
            {41, 6, 3}, // 0x0E Backspace

            {2, 9, 3}, // 0x0F Tab
            {6, 9, 0}, // 0x10 Q
            {9, 9, 0}, // 0x11 W
            {12, 9, 0}, // 0x12 E
            {15, 9, 0}, // 0x13 R
            {18, 9, 0}, // 0x14 T
            {21, 9, 0}, // 0x15 Y
            {24, 9, 0}, // 0x16 U
            {27, 9, 0}, // 0x17 I
            {30, 9, 0}, // 0x18 O
            {33, 9, 0}, // 0x19 P
            {36, 9, 0}, // 0x1A [
            {39, 9, 0}, // 0x1B ]

            {40, 12, 4}, // 0x1C Enter
            {2, 18, 0}, // 0x1D Left Control

            {7, 12, 0}, // 0x1E A
            {10, 12, 0}, // 0x1F S
            {13, 12, 0}, // 0x20 D
            {16, 12, 0}, // 0x21 F
            {19, 12, 0}, // 0x22 G
            {22, 12, 0}, // 0x23 H
            {25, 12, 0}, // 0x24 J
            {28, 12, 0}, // 0x25 K
            {31, 12, 0}, // 0x26 L
            {34, 12, 0}, // 0x27 ;
            {37, 12, 0}, // 0x28 '

            {2, 7, 0}, // 0x29 `
            {2, 15, 5}, // 0x2A Left Shift
            {42, 9, 0}, // 0x2B \

            {8, 15, 0}, // 0x2C Z
            {11, 15, 0}, // 0x2D X
            {14, 15, 0}, // 0x2E C
            {17, 15, 0}, // 0x2F V
            {20, 15, 0}, // 0x30 B
            {23, 15, 0}, // 0x31 N
            {26, 15, 0}, // 0x32 M
            {29, 15, 0}, // 0x33 ,
            {32, 15, 0}, // 0x34 .
            {35, 15, 0}, // 0x35 /
            {39, 15, 5}, // 0x36 Right Shift

            {62, 6, 0}, // 0x37 Num *
            {8, 18, 0}, // 0x38 Left Alt
            {11, 18, 2}, // 0x39 Space
            {2, 12, 4}, // 0x3A Caps Lock

            {7, 2, 0}, // 0x3B F1
            {10, 2, 0}, // 0x3C F2
            {13, 2, 0}, // 0x3D F3
            {16, 2, 0}, // 0x3E F4
            {20, 2, 0}, // 0x3F F5
            {23, 2, 0}, // 0x40 F6
            {26, 2, 0}, // 0x41 F7
            {29, 2, 0}, // 0x42 F8
            {33, 2, 0}, // 0x43 F9
            {36, 2, 0}, // 0x44 F10

            {56, 6, 0}, // 0x45 Num Lock
            {49, 2, 0}, // 0x46 Scroll Lock

            {56, 9, 0}, // 0x47 Num 7
            {59, 9, 0}, // 0x48 Num 8
            {62, 9, 0}, // 0x49 Num 9

            {65, 6, 0}, // 0x4A Num -

            {56, 12, 0}, // 0x4B Num 4
            {59, 12, 0}, // 0x4C Num 5
            {62, 12, 0}, // 0x4D Num 6 

            {65, 9, 6}, // 0x4E Num +

            {56, 15, 0}, // 0x4F Num 1
            {59, 15, 0}, // 0x50 Num 2
            {62, 15, 0}, // 0x51 Num 3

            {56, 18, 5}, // 0x52 Num 0
            {62, 18, 0}, // 0x53 Num .

            {-1, -1, 0}, // 0x54 void
            {-1, -1, 0}, // 0x55 void
            {-1, -1, 0}, // 0x56 void

            {39, 2, 0}, // 0x57 F11
            {42, 2, 0}, // 0x58 F12

            {-1, -1, 0}, // 0x59 void
            {-1, -1, 0}, // 0x5A void
            {-1, -1, 0}, // 0x5B void
            {-1, -1, 0}, // 0x5C void
            {-1, -1, 0}, // 0x5D void
            {-1, -1, 0}, // 0x5E void
            {-1, -1, 0}, // 0x5F void
            {-1, -1, 0}, // 0x60 void
            {-1, -1, 0}, // 0x61 void
            {-1, -1, 0}, // 0x62 void
            {-1, -1, 0}, // 0x63 void

            {-1, -1, 0}, // 0x64 F13
            {-1, -1, 0}, // 0x65 F14
            {-1, -1, 0}, // 0x66 F15  

            {-1, -1, 0}, // 0x67 void
            {-1, -1, 0}, // 0x68 void
            {-1, -1, 0}, // 0x69 void
            {-1, -1, 0}, // 0x6A void
            {-1, -1, 0}, // 0x6B void
            {-1, -1, 0}, // 0x6C void
            {-1, -1, 0}, // 0x6D void
            {-1, -1, 0}, // 0x6E void
            {-1, -1, 0}, // 0x6F void

            {-1, -1, 0}, // 0x70 Kana

            {-1, -1, 0}, // 0x71 void
            {-1, -1, 0}, // 0x72 void
            {-1, -1, 0}, // 0x73 void 
            {-1, -1, 0}, // 0x74 void
            {-1, -1, 0}, // 0x75 void
            {-1, -1, 0}, // 0x76 void
            {-1, -1, 0}, // 0x77 void
            {-1, -1, 0}, // 0x78 void 

            {-1, -1, 0}, // 0x79 Convert

            {-1, -1, 0}, // 0x7A void

            {-1, -1, 0}, // 0x7B No Convert

            {-1, -1, 0}, // 0x7C void

            {-1, -1, 0}, // 0x7D Yen

            {-1, -1, 0}, // 0x7E void
            {-1, -1, 0}, // 0x7F void
            {-1, -1, 0}, // 0x80 void 
            {-1, -1, 0}, // 0x81 void
            {-1, -1, 0}, // 0x82 void
            {-1, -1, 0}, // 0x83 void
            {-1, -1, 0}, // 0x84 void
            {-1, -1, 0}, // 0x85 void 
            {-1, -1, 0}, // 0x86 void 
            {-1, -1, 0}, // 0x87 void
            {-1, -1, 0}, // 0x88 void
            {-1, -1, 0}, // 0x89 void
            {-1, -1, 0}, // 0x8A void
            {-1, -1, 0}, // 0x8B void   
            {-1, -1, 0}, // 0x8C void 

            {-1, -1, 0}, // 0x8D Num =

            {-1, -1, 0}, // 0x8E void
            {-1, -1, 0}, // 0x8F void

            {-1, -1, 0}, // 0x90 ^
            {-1, -1, 0}, // 0x91 @
            {-1, -1, 0}, // 0x92 :
            {-1, -1, 0}, // 0x93 _
            {-1, -1, 0}, // 0x94 Kanji
            {-1, -1, 0}, // 0x95 Stop
            {-1, -1, 0}, // 0x96 Ax
            {-1, -1, 0}, // 0x97 Unlabeled 

            {-1, -1, 0}, // 0x98 void
            {-1, -1, 0}, // 0x99 void
            {-1, -1, 0}, // 0x9A void
            {-1, -1, 0}, // 0x9B void   

            {65, 15, 6}, // 0x9C Num Enter

            {41, 18, 3}, // 0x9D Right Control

            {-1, -1, 0}, // 0x9E void
            {-1, -1, 0}, // 0x9F void
            {-1, -1, 0}, // 0xA0 void
            {-1, -1, 0}, // 0xA1 void
            {-1, -1, 0}, // 0xA2 void
            {-1, -1, 0}, // 0xA3 void
            {-1, -1, 0}, // 0xA4 void
            {-1, -1, 0}, // 0xA5 void
            {-1, -1, 0}, // 0xA6 void
            {-1, -1, 0}, // 0xA7 void
            {-1, -1, 0}, // 0xA8 void
            {-1, -1, 0}, // 0xA9 void
            {-1, -1, 0}, // 0xAA void
            {-1, -1, 0}, // 0xAB void 
            {-1, -1, 0}, // 0xAC void
            {-1, -1, 0}, // 0xAD void
            {-1, -1, 0}, // 0xAE void
            {-1, -1, 0}, // 0xAF void
            {-1, -1, 0}, // 0xB0 void
            {-1, -1, 0}, // 0xB1 void
            {-1, -1, 0}, // 0xB2 void

            {-1, -1, 0}, // 0xB3 Num ,

            {59, 6, 0}, // 0xB3 Num /

            {-1, -1, 0}, // 0xB4 void
            {-1, -1, 0}, // 0xB5 void
            {-1, -1, 0}, // 0xB6 void    

            {46, 2, 0}, // 0xB7 SysRq

            {32, 18, 0}, // 0xB8 Right Alt

            {-1, -1, 0}, // 0xB9 void
            {-1, -1, 0}, // 0xBA void
            {-1, -1, 0}, // 0xBB void 
            {-1, -1, 0}, // 0xBC void
            {-1, -1, 0}, // 0xBD void
            {-1, -1, 0}, // 0xBE void
            {-1, -1, 0}, // 0xBF void
            {-1, -1, 0}, // 0xC0 void
            {-1, -1, 0}, // 0xC1 void
            {-1, -1, 0}, // 0xC2 void  
            {-1, -1, 0}, // 0xC3 void  
            {-1, -1, 0}, // 0xC4 void

            {52, 2, 0}, // 0xC5 Pause

            {-1, -1, 0}, // 0xC6 void

            {49, 6, 0}, // 0xC7 Home

            {49, 15, 0}, // 0xC8 Up

            {52, 6, 0}, // 0xC9 Page Up

            {-1, -1, 0}, // 0xCA void

            {46, 18, 0}, // 0xCB Left

            {-1, -1, 0}, // 0xCC void

            {52, 18, 0}, // 0xCD Right

            {-1, -1, 0}, // 0xCE void

            {49, 9, 0}, // 0xCF End

            {49, 18, 0}, // 0xD0 Down

            {52, 9, 0}, // 0xD1 Page Down

            {46, 6, 0}, // 0xD2 Insert

            {46, 9, 0}, // 0xD3 Delete

            {-1, -1, 0}, // 0xD4 void
            {-1, -1, 0}, // 0xD5 void
            {-1, -1, 0}, // 0xD6 void
            {-1, -1, 0}, // 0xD7 void
            {-1, -1, 0}, // 0xD8 void
            {-1, -1, 0}, // 0xD9 void
            {-1, -1, 0}, // 0xDA void  

            {5, 18, 0}, // 0xDB Left Windows

            {35, 19, 0}, // 0xDC Right Windows    

            {-1, -1, 0}, // 0xDD void  
            {-1, -1, 0}, // 0xDE void  
            {-1, -1, 0}, // 0xDF void    
    };

//public static final Component.Identifier.Key PAGEDOWN    = new Component.Identifier.Key("Pg Down"); // MS 0xD1 UNIX 0xFF56
//public static final Component.Identifier.Key INSERT      = new Component.Identifier.Key("Insert"); // MS 0xD2 UNIX 0xFF63
//public static final Component.Identifier.Key DELETE      = new Component.Identifier.Key("Delete"); // MS 0xD3 UNIX 0xFFFF

    private int scanCodes;

    @Override
    public int getInputDevice() {
        return InputDevices.TransformerKeyboard;
    }

    @Override
    public void update(final int buttons) {
        scanCodes = buttons >>> 8;
    }

    public int getScanCodes() {
        return scanCodes;
    }

    @Override
    public void writePort(final int value) {
    }

    @Override
    public int readPort(final int portIndex) {
        return 0;
    }

    @Override
    public int peekPort(final int portIndex) {
        return 0;
    }

    private void render(final int[] screen, final int x, final int y,
                        final int[] K) {

        final InputIcons icon;
        switch (K[2]) {
            case 2:
                icon = InputIcons.SuborKeyboardSpace;
                break;
            case 3:
                icon = InputIcons.SuborKeyboard3;
                break;
            case 4:
                icon = InputIcons.FamilyBasicKeyboardShift;
                break;
            case 5:
                icon = InputIcons.SuborKeyboard5;
                break;
            case 6:
                icon = InputIcons.SuborKeyboardVertical;
                break;
            default:
                icon = InputIcons.FamilyBasicKeyboardKey;
                break;
        }
        icon.render(screen, x + K[0], y + K[1]);
    }

    @Override
    public void render(final int[] screen) {
        final int x = 158;
        final int y = 205;
        InputIcons.Transformer.render(screen, x, y);
        int codes = scanCodes;
        for (int i = 2; i >= 0; i--) {
            if (codes == 0) {
                break;
            }
            final int scanCode = codes & 0xFF;
            if (scanCode < 0x80) {
                final int[] K = KEYS[scanCode];
                if (K[0] >= 0) {
                    render(screen, x, y, K);
                }
            }
            codes >>>= 8;
        }
    }
}
