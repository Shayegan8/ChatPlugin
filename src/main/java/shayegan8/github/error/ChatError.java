package shayegan8.github.error;

public class ChatError extends Exception {
    public ChatError(String msg) {
        super(msg);
        this.printStackTrace();
    }
}
