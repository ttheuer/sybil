package org.synyx.sybil.out;

import com.tinkerforge.BrickletLEDStrip;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import java.util.Arrays;


/**
 * Controls a single LED strip.
 *
 * @author  Tobias Theuer - theuer@synyx.de
 */
public class OutputLEDStrip {

    private final BrickletLEDStrip ledStrip;
    private short[] pixelBufferRed;
    private short[] pixelBufferGreen;
    private short[] pixelBufferBlue;
    private double brightness;

    /**
     * Makes new OutputLEDStrip object.
     *
     * @param  ledStrip  The LED Strip we want to control.
     * @param  length  How many LEDs are on the LED Strip.
     */
    public OutputLEDStrip(BrickletLEDStrip ledStrip, int length) {

        brightness = 1.0;

        this.ledStrip = ledStrip;

        int differenceToMultipleOfSixteen = length % 16;

        if (differenceToMultipleOfSixteen > 0) {
            length = length + (16 - differenceToMultipleOfSixteen); // make sure the length is a multiple of sixteen
        }

        pixelBufferRed = new short[length];
        pixelBufferGreen = new short[length];
        pixelBufferBlue = new short[length];

        for (int i = 0; i < pixelBufferRed.length; i++) {
            pixelBufferRed[i] = (short) 0;
            pixelBufferGreen[i] = (short) 0;
            pixelBufferRed[i] = (short) 0;
        }
    }

    /**
     * Updates the LED Strip with the current content of the pixelbuffer. Needs to be called for changes made with a
     * setter to show.
     */
    public void updateDisplay() {

        short[] redArray;
        short[] greenArray;
        short[] blueArray;

        for (int i = 0; i < pixelBufferRed.length; i += 16) { // loop over the pixelbuffer in steps of 16
            redArray = Arrays.copyOfRange(pixelBufferRed, i, i + 16);
            greenArray = Arrays.copyOfRange(pixelBufferGreen, i, i + 16);
            blueArray = Arrays.copyOfRange(pixelBufferBlue, i, i + 16);

            for (int j = 0; j < 16; j++) {
                redArray[j] *= brightness;
                greenArray[j] *= brightness;
                blueArray[j] *= brightness;
            }

            try {
                ledStrip.setRGBValues(i, (short) 16, blueArray, redArray, greenArray);
            } catch (TimeoutException | NotConnectedException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Sets the brightness of the LEDs.
     *
     * @param  brightness  Brightness, between 0.0 (completely dark) and 2.0 (twice as bright as normal)
     */
    public void setBrightness(double brightness) {

        if (brightness < 0.0) {
            brightness = 0.0;
        }

        if (brightness > 2.0) {
            brightness = 2.0;
        }

        this.brightness = brightness;
    }


    /**
     * Sets a single pixel on the LED Strip to color.
     *
     * @param  position  The position of the pixel on the LED Strip, starting at 0 for the pixel closest to the
     *                   controller
     * @param  color  The color the pixel should be.
     */
    public void setPixel(int position, Color color) {

        pixelBufferRed[position] = color.getRed();
        pixelBufferGreen[position] = color.getGreen();
        pixelBufferBlue[position] = color.getBlue();
    }


    /**
     * Gets the color of the pixel at the specified position.
     *
     * @param  position  The position of the pixel on the LED Strip, starting at 0 for the pixel closest to the
     *                   controller
     *
     * @return  The color at the specified position.
     */
    Color getPixel(int position) {

        Color color = null;

        try {
            color = new Color(ledStrip.getRGBValues(position, (short) 1));
        } catch (TimeoutException | NotConnectedException e) {
            e.printStackTrace();
        }

        return color;
    }


    /**
     * Shows a single color on the whole LED Strip.
     *
     * @param  color  The color the strip should be
     */

    public void setColor(Color color) {

        Arrays.fill(pixelBufferRed, color.getRed());
        Arrays.fill(pixelBufferGreen, color.getGreen());
        Arrays.fill(pixelBufferBlue, color.getBlue());
    }
}
