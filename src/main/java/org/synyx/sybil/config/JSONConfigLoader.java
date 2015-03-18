package org.synyx.sybil.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.env.Environment;

import org.springframework.stereotype.Component;

import org.synyx.sybil.common.jenkins.JenkinsConfig;
import org.synyx.sybil.database.BrickRepository;
import org.synyx.sybil.database.OutputLEDStripRepository;
import org.synyx.sybil.domain.BrickDomain;
import org.synyx.sybil.domain.OutputLEDStripDomain;
import org.synyx.sybil.out.OutputLEDStrip;
import org.synyx.sybil.out.OutputLEDStripRegistry;
import org.synyx.sybil.out.SingleStatusOnLEDStripRegistry;

import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.Map;


/**
 * JSONConfigLoader. Loads complete or partial configurations from JSON files.
 *
 * @author  Tobias Theuer - theuer@synyx.de
 */

@Component
public class JSONConfigLoader {

    private static final Logger LOG = LoggerFactory.getLogger(JSONConfigLoader.class); // Logger

    private ObjectMapper mapper = new ObjectMapper(); // JSON<->POJO Mapper

    private String configDir; // The place where the config files lie, taken from the injected environment (and thus ultimately a properties file)

    private String jenkinsConfigFile; // The file where the Jenkins servers are configured

    private BrickRepository brickRepository; // The Repository to save said configuration data

    private OutputLEDStripRepository outputLEDStripRepository; // The Repository to save said configuration data

    private OutputLEDStripRegistry outputLEDStripRegistry; // This fetches the actual LED Strip objects for given config data

    private SingleStatusOnLEDStripRegistry singleStatusOnLEDStripRegistry; // Fetches one SingleStatusOnLEDStrip for each LED Strip

    private JenkinsConfig jenkinsConfig;

    /**
     * Instantiates a new JSON config loader.
     *
     * @param  brickRepository  The Brick repository
     * @param  outputLEDStripRepository  The OutputLEDStrip repository
     * @param  env  The Environment (provided by Spring, contains the configuration read from config.properties)
     * @param  outputLEDStripRegistry  the OutputLEDStrip registry
     * @param  jenkinsConfig  the jenkins configuration
     * @param  singleStatusOnLEDStripRegistry  the SingleStatusOnLEDStrip registry
     */
    @Autowired
    public JSONConfigLoader(BrickRepository brickRepository, OutputLEDStripRepository outputLEDStripRepository,
        Environment env, OutputLEDStripRegistry outputLEDStripRegistry, JenkinsConfig jenkinsConfig,
        SingleStatusOnLEDStripRegistry singleStatusOnLEDStripRegistry) {

        this.brickRepository = brickRepository;
        this.outputLEDStripRepository = outputLEDStripRepository;
        configDir = env.getProperty("path.to.configfiles");
        jenkinsConfigFile = env.getProperty("jenkins.configfile");
        this.outputLEDStripRegistry = outputLEDStripRegistry;
        this.jenkinsConfig = jenkinsConfig;
        this.singleStatusOnLEDStripRegistry = singleStatusOnLEDStripRegistry;
    }

    /**
     * Load the complete configuration from JSON files.
     *
     * @throws  IOException  the iO exception
     */
    public void loadConfig() throws IOException {

        loadBricksConfig();

        loadLEDStripConfig();

        loadJenkinsServers();

        loadJenkinsConfig();
    }


    /**
     * Load bricks config.
     *
     * @throws  IOException  the iO exception
     */
    public void loadBricksConfig() throws IOException {

        LOG.info("Loading Brick configuration");

        List<BrickDomain> bricks = mapper.readValue(new File(configDir + "bricks.json"),
                new TypeReference<List<BrickDomain>>() {
                });

        brickRepository.deleteAll();

        brickRepository.save(bricks); // ... simply dump them into the database
    }


    /**
     * Load lED strip config.
     *
     * @throws  IOException  the iO exception
     */
    public void loadLEDStripConfig() throws IOException {

        LOG.info("Loading LED Strip configuration");

        List<Map<String, Object>> ledstrips = mapper.readValue(new File(configDir + "ledstrips.json"),
                new TypeReference<List<Map<String, Object>>>() {
                });

        outputLEDStripRepository.deleteAll();

        for (Map ledstrip : ledstrips) { // ... deserialize the data manually

            String name = ledstrip.get("name").toString();
            String uid = ledstrip.get("uid").toString();
            int length = (int) ledstrip.get("length"); // TODO: Error Handling
            BrickDomain brick = brickRepository.findByHostname(ledstrip.get("brick").toString()); // fetch the corresponding bricks from the repo

            if (brick != null) { // if there was corresponding brick found in the repo...
                outputLEDStripRepository.save(new OutputLEDStripDomain(name, uid, length, brick)); // ... save the LED Strip.
            } else { // if not...
                LOG.error("Brick " + ledstrip.get("brick").toString() + " does not exist."); // ... error! TODO: Error Handling
            }
        }
    }


    public void loadJenkinsServers() throws IOException {

        LOG.info("Loading Jenkins servers");

        List<Map<String, Object>> servers = mapper.readValue(new File(jenkinsConfigFile),
                new TypeReference<List<Map<String, Object>>>() {
                });

        for (Map server : servers) {
            jenkinsConfig.putServer(server.get("hostname").toString(), server.get("user").toString(),
                server.get("key").toString());
        }
    }


    /**
     * Load jenkins config.
     *
     * @throws  IOException  the iO exception
     */
    public void loadJenkinsConfig() throws IOException {

        LOG.info("Loading Jenkins configuration");

        Map<String, List<Map<String, Object>>> jenkinsConfigData = mapper.readValue(new File(
                    configDir + "jenkins.json"), new TypeReference<Map<String, List<Map<String, Object>>>>() {
                }); // fetch Jenkins configuration data...

        jenkinsConfig.reset();

        // ... deserialize the data manually
        for (String server : jenkinsConfigData.keySet()) { // iterate over all the servers

            for (Map line : jenkinsConfigData.get(server)) { // get each configuration line for each server

                String name = line.get("name").toString();
                String ledstrip = line.get("ledstrip").toString();

                OutputLEDStripDomain outputLEDStripDomain = outputLEDStripRepository.findByName(ledstrip.toLowerCase()); // names are always lowercase

                OutputLEDStrip outputLEDStrip = outputLEDStripRegistry.get(outputLEDStripDomain);

                if (outputLEDStrip != null) {
                    jenkinsConfig.put(server, name, singleStatusOnLEDStripRegistry.get(outputLEDStrip));
                } else {
                    LOG.error("Ledstrip " + ledstrip + " does not exist.");
                }
            }
        }
    }
}
