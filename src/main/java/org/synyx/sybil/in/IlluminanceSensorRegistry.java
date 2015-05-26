package org.synyx.sybil.in;

import com.tinkerforge.BrickletAmbientLight;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.sybil.common.BrickRegistry;
import org.synyx.sybil.common.BrickletRegistry;
import org.synyx.sybil.database.OutputLEDStripRepository;
import org.synyx.sybil.domain.InputSensorDomain;
import org.synyx.sybil.out.OutputLEDStripRegistry;

import java.util.HashMap;
import java.util.Map;


/**
 * OutputLEDStripRegistry.
 *
 * @author  Tobias Theuer - theuer@synyx.de
 */

@Service // Annotated so Spring finds and injects it.
public class IlluminanceSensorRegistry implements BrickletRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(IlluminanceSensorRegistry.class);

    private Map<InputSensorDomain, BrickletAmbientLight> illuminanceSensors = new HashMap<>();
    private BrickRegistry brickRegistry;
    private OutputLEDStripRegistry outputLEDStripRegistry;
    private OutputLEDStripRepository outputLEDStripRepository;

    // Constructor, called when Spring autowires it somewhere. Dependencies are injected.

    /**
     * Instantiates a new Illuminance sensor registry.
     *
     * @param  brickRegistry  the brick registry
     * @param  outputLEDStripRegistry  the output lED strip registry
     * @param  outputLEDStripRepository  the output lED strip repository
     */
    @Autowired
    public IlluminanceSensorRegistry(BrickRegistry brickRegistry, OutputLEDStripRegistry outputLEDStripRegistry,
        OutputLEDStripRepository outputLEDStripRepository) {

        this.brickRegistry = brickRegistry;
        this.outputLEDStripRegistry = outputLEDStripRegistry;
        this.outputLEDStripRepository = outputLEDStripRepository;
    }

    /**
     * Get a BrickletAmbientLight object, instantiate a new one if necessary.
     *
     * @param  inputSensorDomain  The bricklet's domain from the database.
     *
     * @return  The actual BrickletAmbientLight object.
     */
    public BrickletAmbientLight get(InputSensorDomain inputSensorDomain) {

        if (inputSensorDomain == null) {
            return null;
        }

        LOG.debug("Setting up sensor {}.", inputSensorDomain.getName());

        if (!illuminanceSensors.containsKey(inputSensorDomain)) {
            BrickletAmbientLight brickletAmbientLight;

            try {
                // get the connection to the Brick, passing the BrickDomain and the calling object
                IPConnection ipConnection = brickRegistry.get(inputSensorDomain.getBrickDomain(), this);

                if (ipConnection != null) {
                    // Create a new Tinkerforge BrickletAmbientLight object with data from the database
                    brickletAmbientLight = new BrickletAmbientLight(inputSensorDomain.getUid(), ipConnection);

                    brickletAmbientLight.setIlluminanceCallbackPeriod(5000);

                    brickletAmbientLight.addIlluminanceListener(new IlluminanceListener(inputSensorDomain,
                            outputLEDStripRegistry, outputLEDStripRepository));
                } else {
                    LOG.error("Error setting up illuminance sensor {}: Brick {} not available.",
                        inputSensorDomain.getName(), inputSensorDomain.getBrickDomain().getHostname());

                    brickletAmbientLight = null;
                }
            } catch (TimeoutException | NotConnectedException e) {
                LOG.error("Error setting up illuminance sensor {}: {}", inputSensorDomain.getName(), e.toString());
                brickletAmbientLight = null; // if there is an error, we don't want to use this
            }

            if (brickletAmbientLight != null) {
                // add it to the HashMap
                illuminanceSensors.put(inputSensorDomain, brickletAmbientLight);
            }
        }

        LOG.debug("Finished setting up sensor {}.", inputSensorDomain.getName());

        return illuminanceSensors.get(inputSensorDomain); // retrieve and return
    }


    /**
     * Clear the registry.
     */
    @Override
    public void clear() {

        illuminanceSensors.clear();
    }
}