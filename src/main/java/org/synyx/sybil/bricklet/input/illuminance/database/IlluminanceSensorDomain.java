package org.synyx.sybil.bricklet.input.illuminance.database;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import org.springframework.hateoas.core.Relation;

import org.synyx.sybil.brick.database.BrickDomain;

import java.util.List;


/**
 * InputSensor domain. Persistence for the sensor configuration data, but not the actual objects.
 *
 * @author  Tobias Theuer - theuer@synyx.de
 */

@NodeEntity
@Relation(collectionRelation = "illuminancesensors")
public class IlluminanceSensorDomain {

    @GraphId
    private Long id;

    private String name;

    private String uid;

    private int threshold;

    private double multiplier;

    private List<String> outputs;

    @Fetch
    @RelatedTo(type = "IS_PART_OF")
    @JsonProperty("brick")
    private BrickDomain brickDomain;

    public IlluminanceSensorDomain() {
    }


    public IlluminanceSensorDomain(String name, String uid, int threshold, double multiplier, List<String> outputs,
        BrickDomain brickDomain) {

        this.name = name;
        this.uid = uid;
        this.threshold = threshold;
        this.multiplier = multiplier;
        this.outputs = outputs;
        this.brickDomain = brickDomain;
    }

    public String getName() {

        return name;
    }


    public String getUid() {

        return uid;
    }


    public int getThreshold() {

        return threshold;
    }


    public double getMultiplier() {

        return multiplier;
    }


    public List<String> getOutputs() {

        return outputs;
    }


    public BrickDomain getBrickDomain() {

        return brickDomain;
    }


    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IlluminanceSensorDomain that = (IlluminanceSensorDomain) o;

        return Double.compare(that.multiplier, multiplier) == 0 && threshold == that.threshold
            && brickDomain.equals(that.brickDomain) && id.equals(that.id) && name.equals(that.name)
            && outputs.equals(that.outputs) && uid.equals(that.uid);
    }


    @Override
    public int hashCode() {

        int result;
        long temp;
        result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + uid.hashCode();
        result = 31 * result + threshold;
        temp = Double.doubleToLongBits(multiplier);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + outputs.hashCode();
        result = 31 * result + brickDomain.hashCode();

        return result;
    }
}