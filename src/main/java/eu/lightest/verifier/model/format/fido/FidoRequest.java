package eu.lightest.verifier.model.format.fido;

public class FidoRequest {
    
    public static final String QUERY_idMethod = "idMethod";
    public static final String QUERY_idType = "idType";
    public static final String QUERY_authMethod = "authMethod";
    public static final String QUERY_authType = "authType";
    
    public int idMethod;
    public int idType;
    public int authMethod;
    public String authType;
    
    @Override
    public String toString() {
        return "FidoRequest{" +
                "idMethod=" + this.idMethod +
                ", idType=" + this.idType +
                ", authMethod=" + this.authMethod +
                ", authType='" + this.authType + '\'' +
                '}';
    }
}
