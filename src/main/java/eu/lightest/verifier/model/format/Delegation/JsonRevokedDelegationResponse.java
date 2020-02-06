package eu.lightest.verifier.model.format.Delegation;

import com.google.gson.annotations.SerializedName;


public class JsonRevokedDelegationResponse  {
    @SerializedName("id")
    private int mId;

    @SerializedName("hash")
    private String mHash;

    @SerializedName("status")
    private String mStatus;

    public JsonRevokedDelegationResponse(int id, String hash, String status) {
        mId = id;
        mHash = hash;
        mStatus = status;
    }

    public int getId() {
        return mId;
    }

    public String getHash() {
        return mHash;
    }

    public String getStatus() {
        return mStatus;
    }
}
