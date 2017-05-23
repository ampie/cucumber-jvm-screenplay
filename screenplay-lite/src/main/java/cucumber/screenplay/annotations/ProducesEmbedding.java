package cucumber.screenplay.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.FIELD})
public @interface ProducesEmbedding {
    String mimeType() default "text/plain";
    Class<? extends EmbeddingSerializer> serializer() default EmbeddingSerializer.class;
}
