package cucumber.screenplay.annotations;


public interface EmbeddingSerializer <T>{
    byte[] toByteArray(T object);
}
