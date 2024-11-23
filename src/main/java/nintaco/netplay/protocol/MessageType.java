package nintaco.netplay.protocol;

public interface MessageType {
    int Heartbeat = 0;
    int ServerDescription = 1;
    int PasswordHash = 2;
    int WrongPasswordError = 3;
    int Authenticated = 4;
    int PlayerRequest = 5;
    int PlayerResponse = 6;
    int FileRequest = 7;
    int FileResponse = 8;
    int NoFileResponse = 9;
    int FileReceived = 10;
    int ControllerInput = 11;
    int SaveState = 12;
    int Play = 13;
    int Rewind = 14;
    int MovieBlock = 15;
    int FrameEnd = 16;
    int QuickSaveStateMenuNames = 17;
    int QuickLoad = 18;
    int QuickSave = 19;
    int ShowMessage = 20;
    int HighSpeed = 21;
}
