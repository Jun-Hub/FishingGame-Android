package rawfish.fishinggame;

public class ChatMessage {
    public boolean left;
    public boolean whisper;
    public String message;

    public ChatMessage(boolean left, boolean whisper, String message) {
        super();
        this.left = left;
        this.whisper = whisper;
        this.message = message;
    }

    public String get() {
        return message;
    }
}
