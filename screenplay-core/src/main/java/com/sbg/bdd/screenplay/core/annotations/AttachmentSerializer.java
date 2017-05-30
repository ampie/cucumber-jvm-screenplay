package com.sbg.bdd.screenplay.core.annotations;


public interface AttachmentSerializer<T>{
    byte[] toByteArray(T object);
}
