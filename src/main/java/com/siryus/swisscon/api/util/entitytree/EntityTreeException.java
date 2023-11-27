package com.siryus.swisscon.api.util.entitytree;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class EntityTreeException extends ResponseStatusException {
    EntityTreeException(HttpStatus status) {
        super(status);
    }

    EntityTreeException(HttpStatus status, String reason) {
        super(status, reason);
    }


    EntityTreeException(HttpStatus status, String reason, Throwable cause) {
        super(status, reason, cause);
    }

    public static EntityTreeException nodeIsNotEmpty() {
        return new NodeIsNotEmpty();
    }

    public static EntityTreeException nodeNotFound(Integer nodeId) {
        return new NodeNotFound(nodeId);
    }

    static class NodeIsNotEmpty extends EntityTreeException {
        NodeIsNotEmpty() {
            super(HttpStatus.EXPECTATION_FAILED);
        }
    }

    static class NodeNotFound extends EntityTreeException {
        NodeNotFound(Integer nodeId ) {
            super(HttpStatus.BAD_REQUEST, "Node does not exist : " + nodeId);
        }
    }
}
