package org.synyx.sybil.bricklet.output.ledstrip;

import com.tinkerforge.BrickletLEDStrip;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import org.neo4j.helpers.collection.IteratorUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.sybil.brick.BrickService;
import org.synyx.sybil.bricklet.BrickletService;
import org.synyx.sybil.bricklet.output.ledstrip.database.LEDStripDomain;
import org.synyx.sybil.bricklet.output.ledstrip.database.LEDStripRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * LEDStripService.
 *
 * @author  Tobias Theuer - theuer@synyx.de
 */

@Service // Annotated so Spring finds and injects it.
public class LEDStripService implements BrickletService {

    private static final Logger LOG = LoggerFactory.getLogger(LEDStripService.class);
    private static final int FRAME_DURATION = 10;
    private static final int CHIP_TYPE = 2812;

    private Map<LEDStripDomain, LEDStrip> outputLEDStrips = new HashMap<>();
    private BrickService brickService;
    private LEDStripRepository ledStripRepository;
    private GraphDatabaseService graphDatabaseService;

    // Constructor, called when Spring autowires it somewhere. Dependencies are injected.

    /**
     * Instantiates a new LEDStrip registry.
     *
     * @param  brickService  The brick registry
     * @param  ledStripRepository  LED strip database repository
     * @param  graphDatabaseService  Neo4j service
     */
    @Autowired
    public LEDStripService(BrickService brickService, LEDStripRepository ledStripRepository,
        GraphDatabaseService graphDatabaseService) {

        this.brickService = brickService;
        this.ledStripRepository = ledStripRepository;
        this.graphDatabaseService = graphDatabaseService;
    }

    /**
     * Gets domain.
     *
     * @param  name  the name
     *
     * @return  the domain
     */
    public LEDStripDomain getDomain(String name) {

        return ledStripRepository.findByName(name);
    }


    /**
     * Save domain.
     *
     * @param  ledStripDomain  the ledStrip domain
     *
     * @return  the illuminance sensor domain
     */
    public LEDStripDomain saveDomain(LEDStripDomain ledStripDomain) {

        return ledStripRepository.save(ledStripDomain);
    }


    /**
     * Gets all domains.
     *
     * @return  the all domains
     */
    public List<LEDStripDomain> getAllDomains() {

        List<LEDStripDomain> ledStripDomains;

        try(Transaction tx = graphDatabaseService.beginTx()) { // begin transaction

            // get all sensors from database and cast them into a list so that they're actually fetched
            ledStripDomains = new ArrayList<>(IteratorUtil.asCollection(ledStripRepository.findAll()));

            // end transaction
            tx.success();
        }

        return ledStripDomains;
    }


    /**
     * Delete all domains.
     */
    public void deleteAllDomains() {

        ledStripRepository.deleteAll();
    }


    /**
     * Get a LEDStrip object, instantiate a new one if necessary.
     *
     * @param  LEDStripDomain  The bricklet's domain from the database.
     *
     * @return  The actual LEDStrip object.
     */
    public LEDStrip getLEDStrip(LEDStripDomain LEDStripDomain) {

        if (LEDStripDomain == null) {
            return null;
        }

        // if there is no LED Strip with that id in the HashMap yet...
        if (!outputLEDStrips.containsKey(LEDStripDomain)) {
            BrickletLEDStrip brickletLEDStrip; // since there is a try, it might end up undefined

            try {
                // get the connecting to the Brick, passing the BrickDomain and the calling object
                IPConnection ipConnection = brickService.getIPConnection(LEDStripDomain.getBrickDomain(), this);

                if (ipConnection != null) {
                    // Create a new Tinkerforge brickletLEDStrip object with data from the database
                    brickletLEDStrip = new BrickletLEDStrip(LEDStripDomain.getUid(), ipConnection);
                    brickletLEDStrip.setFrameDuration(FRAME_DURATION); // Always go for the minimum (i.e. fastest) frame duration
                    brickletLEDStrip.setChipType(CHIP_TYPE); // We only use 2812 chips
                } else {
                    LOG.error("Error setting up LED Strip {}: Brick {} not available.", LEDStripDomain.getName(),
                        LEDStripDomain.getBrickDomain().getHostname());

                    brickletLEDStrip = null;
                }
            } catch (TimeoutException | NotConnectedException e) {
                LOG.error("Error setting up LED Strip {}: {}", LEDStripDomain.getName(), e.toString());
                brickletLEDStrip = null; // if there is an error, we don't want to use this
            }

            if (brickletLEDStrip != null) {
                // get a new LEDStrip object
                LEDStrip ledStrip = new LEDStrip(brickletLEDStrip, LEDStripDomain.getLength(),
                        LEDStripDomain.getName());

                // add it to the HashMap
                outputLEDStrips.put(LEDStripDomain, ledStrip);
            }
        }

        return outputLEDStrips.get(LEDStripDomain); // retrieve and return
    }


    /**
     * Remove all OutputLEDStrips from the registry.
     */
    @Override
    public void clear() {

        outputLEDStrips.clear();
    }
}
