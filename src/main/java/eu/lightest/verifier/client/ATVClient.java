package eu.lightest.verifier.client;

import eu.lightest.verifier.model.report.Report;

/**
 * Interface for clients for the Automated Trust Verifier library.
 */
public interface ATVClient {
    
    /**
     * Uses the ATV Client to verify the given transaction with respect to the given TPL trust policy.
     * <p>
     * If a {@link Report} was set (e.g. in the constructor), the verification report can be retrieved using it.
     *
     * @param pathPolicy      path to a trust policy (in TPL format).
     * @param pathTransaction path to a LIGHTest transaction (e.g. ASIC container, signed XML/PDF document, etc.).
     * @return result of the verification. <code>true</code> iff the given policy can be fulfilled.
     */
    public boolean verify(String pathPolicy, String pathTransaction);
}
