package com.skullraven.core.services;

/**
 * Test Service to demonstrate how Service Users can be used via
 * a ResourceResolverFactory in both a Read-Only context, and in
 * a Write context.
 */
public interface TestService {

    /**
     * Method to demonstrate the Read-Only context
     */
    void readResource();

    /**
     * Method to demonstrate the Write context
     */
    void writeResource();
}
