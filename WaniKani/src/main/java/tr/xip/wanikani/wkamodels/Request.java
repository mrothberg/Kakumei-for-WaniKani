package tr.xip.wanikani.wkamodels;

public class Request<T> {
    public User user_information;
    public T requested_information;
    public Error error;
}