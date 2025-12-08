package kr.co.opencraft.exception;

public class InitializeException extends OpencraftException{

    public InitializeException(String cause) {
        super("Exception while initializing : " + cause);
    }
}
