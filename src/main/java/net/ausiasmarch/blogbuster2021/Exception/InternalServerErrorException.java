package net.ausiasmarch.blogbuster2021.Exception;

public class InternalServerErrorException extends RuntimeException {

    public InternalServerErrorException(String errorMessage) {
        super(errorMessage);
    }

}
