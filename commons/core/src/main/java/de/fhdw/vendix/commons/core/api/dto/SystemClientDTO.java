package de.fhdw.vendix.commons.core.api.dto;

public class SystemClientDTO extends AbstractDTO<String> {

    private String host;
    private int port;

    public SystemClientDTO() {
        super();
    }

    public SystemClientDTO(String instanceId, String host, int port) {
        super(instanceId);
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}