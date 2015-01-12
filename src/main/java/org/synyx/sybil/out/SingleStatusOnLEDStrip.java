package org.synyx.sybil.out;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.synyx.sybil.in.Status;
import org.synyx.sybil.in.StatusInformation;


/**
 * Shows a status as a single color on an entire LED strip.
 *
 * @author  Tobias Theuer - theuer@synyx.de
 */
public class SingleStatusOnLEDStrip implements SingleStatusOutput {

    private static final Logger LOG = LoggerFactory.getLogger(SingleStatusOnLEDStrip.class);
    private final OutputLEDStrip outputLEDStrip;

    /**
     * Constructor.
     *
     * @param  outputLEDStrip  The output (in this case a LED strip) to show statuses on
     */
    public SingleStatusOnLEDStrip(OutputLEDStrip outputLEDStrip) {

        this.outputLEDStrip = outputLEDStrip;
    }

    /**
     * Show the status on the LED strip.
     *
     * @param  statusInformation  The StatusInformation to display.
     */
    @Override
    public void showStatus(StatusInformation statusInformation) {

        LOG.debug("Show status information: {}", statusInformation.getStatus());

        Color color;

        if (statusInformation.getStatus() == Status.CRITICAL) {
            color = new Color(127, 0, 0);
        } else if (statusInformation.getStatus() == Status.WARNING) {
            color = new Color(127, 127, 0);
        } else {
            color = new Color(0, 0, 0);
        }

        LOG.debug("Set color to: {}", color);

        outputLEDStrip.setColor(color);
        outputLEDStrip.updateDisplay();
    }
}
