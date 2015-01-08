package team017;

// Exception representing unimplemented code.
// Snagged some from org.apache.commons.lang.NotImplementedException
public class NotImplementedException extends UnsupportedOperationException {
    private static final String DEFAULT_MESSAGE = "Code is not implemented";
    private static final long serialVersionUID = 1L;

    public NotImplementedException() {
        super(DEFAULT_MESSAGE);
    }


    public NotImplementedException(String msg) {
        super(msg == null ? DEFAULT_MESSAGE : msg);
    }
}
