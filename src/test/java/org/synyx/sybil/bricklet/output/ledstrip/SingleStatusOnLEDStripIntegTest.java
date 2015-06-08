package org.synyx.sybil.bricklet.output.ledstrip;

import com.tinkerforge.NotConnectedException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.synyx.sybil.brick.BrickRegistry;
import org.synyx.sybil.brick.database.BrickDomain;
import org.synyx.sybil.brick.database.BrickRepository;
import org.synyx.sybil.bricklet.output.ledstrip.database.LEDStripDomain;
import org.synyx.sybil.bricklet.output.ledstrip.database.LEDStripRepository;
import org.synyx.sybil.config.DevSpringConfig;
import org.synyx.sybil.jenkins.domain.Status;
import org.synyx.sybil.jenkins.domain.StatusInformation;

import static org.hamcrest.CoreMatchers.is;

import static org.hamcrest.MatcherAssert.assertThat;


@ContextConfiguration(classes = DevSpringConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class SingleStatusOnLEDStripIntegTest {

    private LEDStrip testOne;
    private LEDStrip testTwo;
    private SingleStatusOutput singleStatusOutputOne;
    private SingleStatusOutput singleStatusOutputTwo;

    @Autowired
    LEDStripRegistry outputLEDStripRegistry;

    @Autowired
    LEDStripRepository outputLEDStripRepository;

    @Autowired
    private BrickRepository brickRepository;

    @Autowired
    private BrickRegistry brickRegistry;

    @Before
    public void setup() {

        // define Bricks
        BrickDomain test1 = new BrickDomain("localhost", "6dLj52", 14223);
        BrickDomain test2 = new BrickDomain("localhost", "im666", 14224);

        // add them to the database
        brickRepository.save(test1);
        brickRepository.save(test2);

        // define LED Strips (bricklets)
        LEDStripDomain testOneDomain = new LEDStripDomain("testone", "abc", 30, test1);
        LEDStripDomain testTwoDomain = new LEDStripDomain("testtwo", "def", 30, test2);

        // add them to the database
        testOneDomain = outputLEDStripRepository.save(testOneDomain);
        testTwoDomain = outputLEDStripRepository.save(testTwoDomain);

        testOne = outputLEDStripRegistry.get(testOneDomain);
        testTwo = outputLEDStripRegistry.get(testTwoDomain);

        singleStatusOutputOne = new SingleStatusOnLEDStrip(testOne);
        singleStatusOutputTwo = new SingleStatusOnLEDStrip(testTwo);
    }


    @After
    public void close() throws NotConnectedException {

        testOne.setFill(Color.BLACK);
        testOne.updateDisplay();
        testTwo.setFill(Color.BLACK);
        testTwo.updateDisplay();

        LEDStripDomain testOneDomain = outputLEDStripRepository.findByName(testOne.getName());
        LEDStripDomain testTwoDomain = outputLEDStripRepository.findByName(testTwo.getName());

        brickRepository.delete(testOneDomain.getBrickDomain());
        brickRepository.delete(testTwoDomain.getBrickDomain());

        outputLEDStripRepository.delete(testOneDomain);
        outputLEDStripRepository.delete(testTwoDomain);

        brickRegistry.disconnectAll();
    }


    @Test
    public void testShowStatusWarning() throws Exception {

        StatusInformation statusInformation = new StatusInformation("Integration Test", Status.WARNING);

        singleStatusOutputOne.showStatus(statusInformation);

        Color pixel = testOne.getPixel(0);
        assertThat(pixel.getRedAsShort(), is((short) 127));
        assertThat(pixel.getGreenAsShort(), is((short) 127));
        assertThat(pixel.getBlueAsShort(), is((short) 0));
    }


    @Test
    public void testShowStatusCritical() throws Exception {

        StatusInformation statusInformation = new StatusInformation("Integration Test", Status.CRITICAL);

        singleStatusOutputTwo.showStatus(statusInformation);

        Color pixel = testTwo.getPixel(0);
        assertThat(pixel.getRedAsShort(), is((short) 127));
        assertThat(pixel.getGreenAsShort(), is((short) 0));
        assertThat(pixel.getBlueAsShort(), is((short) 0));
    }


    @Test
    public void testShowStatusOkay() throws Exception {

        StatusInformation statusInformation = new StatusInformation("Integration Test", Status.OKAY);

        singleStatusOutputOne.showStatus(statusInformation);

        Color pixel = testOne.getPixel(0);
        assertThat(pixel.getRedAsShort(), is((short) 0));
        assertThat(pixel.getGreenAsShort(), is((short) 16));
        assertThat(pixel.getBlueAsShort(), is((short) 0));
    }
}