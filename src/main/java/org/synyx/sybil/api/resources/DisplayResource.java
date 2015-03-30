package org.synyx.sybil.api.resources;

import org.springframework.hateoas.ResourceSupport;

import org.synyx.sybil.out.Color;

import java.util.List;


/**
 * DisplayResource.
 *
 * @author  Tobias Theuer - theuer@synyx.de
 */
public class DisplayResource extends ResourceSupport {

    private List<Color> pixels;
    private Double brightness;

    public List<Color> getPixels() {

        return pixels;
    }


    public void setPixels(List<Color> pixels) {

        this.pixels = pixels;
    }


    public Double getBrightness() {

        return brightness;
    }


    public void setBrightness(Double brightness) {

        this.brightness = brightness;
    }
}
