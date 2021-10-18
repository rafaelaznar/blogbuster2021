package net.ausiasmarch.blogbuster2021.Exception;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("ERROR: Unauthorized access attempt");
    }

}
