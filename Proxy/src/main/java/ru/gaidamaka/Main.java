package ru.gaidamaka;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.OptionalInt;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final int PORT_ARGUMENT_INDEX = 0;
    private static final int ARGUMENTS_NUMBER = 1;

    public static void main(String[] args) {
        if (args.length != ARGUMENTS_NUMBER) {
            logger.error("Wrong arguments number, expected={}, actual={}", ARGUMENTS_NUMBER, args.length);
            return;
        }
        parsePort(args[PORT_ARGUMENT_INDEX]).ifPresent(port -> {
            SOCKSProxy proxy = new SOCKSProxy(port);
            proxy.start();
        });
    }

    private static OptionalInt parsePort(@NotNull String portStr) {
        try {
            return OptionalInt.of(Integer.parseInt(portStr));
        } catch (NumberFormatException e) {
            logger.error("Port must be a integer, actual={}", portStr, e);
            return OptionalInt.empty();
        }
    }
}