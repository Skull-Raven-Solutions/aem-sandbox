package com.skullraven.core.services.impl;

import com.day.cq.commons.jcr.JcrConstants;
import com.skullraven.core.services.TestService;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Component(
        service = TestService.class,
        immediate = true
)
public class TestServiceImpl implements TestService {

    private static final Logger logger = LoggerFactory.getLogger(TestServiceImpl.class);

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    public void readResource() {

        // Map of readService to the factory subservice
        final Map<String, Object> rrProperties = new HashMap<>();
        rrProperties.put(ResourceResolverFactory.SUBSERVICE, "readService");

        try {
            // Create the read-only ResourceResolver object
            ResourceResolver readResourceResolver = resourceResolverFactory.getServiceResourceResolver(rrProperties);

            // Get a specific resource and log it
            Resource asset = readResourceResolver.getResource("/content/dam/sandbox/asset.jpg");
            logger.info("retrieved the asset: " + asset.getName());

        } catch (LoginException e) {
            logger.error("Error creating read resource resolver.", e);
        }
    }

    @Override
    public void writeResource() {

        // Map of writeService to the factory subservice
        final Map<String, Object> rrProperties = new HashMap<>();
        rrProperties.put(ResourceResolverFactory.SUBSERVICE, "writeService");

        // Create the write ResourceResolver object
        try (ResourceResolver writeResourceResolver = resourceResolverFactory.getServiceResourceResolver(rrProperties)) {

            // Get the Sandbox folder in the DAM
            Resource sandboxResource = writeResourceResolver.getResource("/content/dam/sandbox");
            if (sandboxResource != null) {

                // Define a Map of properties for the Folder we will create
                Map<String, Object> folderProperties = new HashMap<>();
                folderProperties.put(JcrConstants.JCR_PRIMARYTYPE, JcrResourceConstants.NT_SLING_FOLDER);

                // Using ResourceUtil, either get or create a new folder
                Resource testFolder =
                        ResourceUtil.getOrCreateResource(writeResourceResolver, "/content/dam/sandbox/testFolder", folderProperties, null, false);

                // If the folder has no children (meaning, it will not have a jcr:content below it), then it must be a new folder
                if (!testFolder.hasChildren()) {
                    // So create the jcr:content and set the primary type and title
                    Map<String, Object> jcrContentProperties = new HashMap<>();
                    jcrContentProperties.put(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_UNSTRUCTURED);
                    jcrContentProperties.put(JcrConstants.JCR_TITLE, "Test Folder");
                    writeResourceResolver.create(testFolder, JcrConstants.JCR_CONTENT, jcrContentProperties);
                }
            }

            if (writeResourceResolver.hasChanges()) {
                // Only commit when there are changes (so only when a new folder was created)
                logger.info("Successfully created some content, so now saving it");
                writeResourceResolver.commit();
            }

        } catch (LoginException e) {
            logger.error("Error creating write resource resolver.", e);
        } catch (PersistenceException e) {
            logger.error("Error saving or creating resource.", e);
        }

    }
}
