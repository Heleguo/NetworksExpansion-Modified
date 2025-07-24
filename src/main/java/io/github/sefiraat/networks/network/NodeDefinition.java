package io.github.sefiraat.networks.network;

import lombok.Getter;
import lombok.Setter;

public class NodeDefinition {

    @Getter
    private final NodeType type;

    private final long timeRegistered;

    @Getter
    private final int charge;

    @Setter
    @Getter
    private NetworkNode node;

    public NodeDefinition(NodeType type) {
        this(type, 0);
    }

    public NodeDefinition(NodeType type, int charge) {
        this.type = type;
        this.timeRegistered = System.currentTimeMillis();
        this.charge = charge;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > this.timeRegistered + 3000L;
    }
}
