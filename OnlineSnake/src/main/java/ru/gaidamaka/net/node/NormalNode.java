package ru.gaidamaka.net.node;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.gaidamaka.config.Config;
import ru.gaidamaka.game.Direction;
import ru.gaidamaka.game.GameState;
import ru.gaidamaka.net.NetNode;
import ru.gaidamaka.net.NodeHandler;
import ru.gaidamaka.net.messagehandler.ErrorMessageHandler;
import ru.gaidamaka.net.messagehandler.RoleChangeMessageHandler;
import ru.gaidamaka.net.messagehandler.StateMessageHandler;
import ru.gaidamaka.net.messages.*;

import java.util.Objects;

public class NormalNode implements
        GameNode,
        ErrorMessageHandler,
        StateMessageHandler,
        RoleChangeMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(NormalNode.class);

    private NodeHandler nodeHandler;
    private int lastGameStateID = Integer.MIN_VALUE;

    public NormalNode(@NotNull Config config) {
    }


    @Override
    public void handleMessage(@NotNull NetNode sender, @NotNull Message message) {
        Objects.requireNonNull(message, "Message cant be null");
        Objects.requireNonNull(sender, "Sender cant be null");
        if (nodeHandler == null) {
            logger.warn("node handler is null when message received");
            return;
        }
        switch (message.getType()) {
            case ROLE_CHANGE:
                handle(sender, (RoleChangeMessage) message);
                break;
            case ERROR:
                handle(sender, (ErrorMessage) message);
                break;
            case STATE:
                handle(sender, (StateMessage) message);
                break;
            default:
                throw new IllegalStateException("Cant handle this message type = " + message.getType());
        }
    }

    @Override
    public void setNodeHandler(@NotNull NodeHandler nodeHandler) {
        this.nodeHandler = nodeHandler;

    }

    @Override
    public void handle(@NotNull NetNode sender, @NotNull ErrorMessage errorMsg) {
        nodeHandler.showError(errorMsg.getErrorMessage());
    }

    @Override
    public void handle(@NotNull NetNode sender, @NotNull StateMessage stateMsg) {
        GameState gameState = stateMsg.getGameState();
        if (lastGameStateID >= gameState.getStateID()) {
            logger.warn("Received state with id={} less then last game state id={}",
                    gameState.getStateID(),
                    lastGameStateID
            );
            return;
        }
        lastGameStateID = gameState.getStateID();
        nodeHandler.updateState(gameState);
    }


    @Override
    public void handle(@NotNull NetNode sender, @NotNull RoleChangeMessage roleChangeMsg) {
        if (roleChangeMsg.getFromRole() == Role.MASTER && roleChangeMsg.getToRole() == Role.DEPUTY) {
            nodeHandler.changeNodeRole(Role.DEPUTY);
        } else if (roleChangeMsg.getFromRole() == Role.DEPUTY && roleChangeMsg.getToRole() == Role.NORMAL) {
            nodeHandler.setMaster(sender);
        } else if (roleChangeMsg.getFromRole() == Role.MASTER && roleChangeMsg.getToRole() == Role.VIEWER) {
            nodeHandler.lose();
        } else {
            logger.warn("Unsupported roles at role change message={} from={}", roleChangeMsg, sender);
            throw new IllegalArgumentException("Unsupported roles at role change message=" + roleChangeMsg + " from=" + sender);
        }
    }

    @Override
    public void makeMove(@NotNull Direction direction) {
        nodeHandler.sendMessage(
                nodeHandler.getMaster(),
                new SteerMessage(direction)
        );
    }

    @Override
    public void stop() {
        logger.info("Normal node stop");
    }
}
