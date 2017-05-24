package cucumber.screenplay.annotations;


public interface AttachmentSerializer<T>{
    byte[] toByteArray(T object);
}
