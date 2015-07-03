package org.synyx.sybil.brick.database;

import org.springframework.data.neo4j.repository.GraphRepository;

import org.springframework.stereotype.Repository;


/**
 * BrickRepository.
 *
 * @author  Tobias Theuer - theuer@synyx.de
 */

@Repository
public interface BrickRepository extends GraphRepository<BrickDomain> {

    BrickDomain findByName(String name);
}
