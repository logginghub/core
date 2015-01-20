package com.logginghub.logging.frontend.visualisations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import org.junit.Test;

import com.logginghub.logging.frontend.modules.ViewDetails;
import com.logginghub.logging.frontend.visualisations.Entity;
import com.logginghub.logging.frontend.visualisations.ParticleSettings;
import com.logginghub.logging.frontend.visualisations.VisualiserModel;

public class TestVisualiserModel {

    private VisualiserModel model = new VisualiserModel(3, new ViewDetails(0, 0));

    private Entity deadEntriesHead = model.getDeadEntitiesHead();
    private Entity liveEntriesHead = model.getLiveEntitiesHead();

    private Entity deadEntriesTail = model.getDeadEntitiesTail();
    private Entity liveEntriesTail = model.getLiveEntitiesTail();

    @Test public void test_initial_setup() throws Exception {

        assertThat(liveEntriesHead.getId(), is(VisualiserModel.liveEntitiesHeadID));
        assertThat(liveEntriesHead.previous, is(nullValue()));
        assertThat(liveEntriesHead.next, is(sameInstance(liveEntriesTail)));

        assertThat(deadEntriesHead.getId(), is(VisualiserModel.deadEntitiesHeadID));
        assertThat(deadEntriesHead.previous, is(nullValue()));
        assertThat(deadEntriesHead.next, is(notNullValue()));

        assertThat(deadEntriesHead.next.getId(), is(0));
        assertThat(deadEntriesHead.next.next.getId(), is(1));
        assertThat(deadEntriesHead.next.next.next.getId(), is(2));
        assertThat(deadEntriesHead.next.next.next.next, is(sameInstance(deadEntriesTail)));

        Entity entity0 = deadEntriesHead.next;
        Entity entity1 = deadEntriesHead.next.next;
        Entity entity2 = deadEntriesHead.next.next.next;

        assertThat(entity0.getId(), is(0));
        assertThat(entity1.getId(), is(1));
        assertThat(entity2.getId(), is(2));

        assertThat(entity0.previous, is(deadEntriesHead));
        assertThat(entity1.previous, is(entity0));
        assertThat(entity2.previous, is(entity1));

        assertThat(entity0.next, is(entity1));
        assertThat(entity1.next, is(entity2));
        assertThat(entity2.next, is(deadEntriesTail));

    }

    @Test public void test_natural_order() throws Exception {
        
        Entity entity0 = deadEntriesHead.next;
        Entity entity1 = deadEntriesHead.next.next;
        Entity entity2 = deadEntriesHead.next.next.next;
        
        assertThat(deadEntriesHead.naturalNext, is(entity0));
        assertThat(entity0.naturalNext, is(entity1));
        assertThat(entity1.naturalNext, is(entity2));
        assertThat(entity2.naturalNext, is(deadEntriesTail));

    }
    
    @Test public void test_spawn() throws Exception {

        Entity entity0 = deadEntriesHead.next;
        Entity entity1 = deadEntriesHead.next.next;
        Entity entity2 = deadEntriesHead.next.next.next;

        // Spawn the first
        Entity spawned1 = model.spawn(new ParticleSettings());
        assertThat(spawned1, is(entity0));

        // This should have been detached from the head of the dead list, and attached to the end of
        // the alive list
        assertThat(spawned1.previous, is(liveEntriesHead));
        assertThat(spawned1.next, is(liveEntriesTail));

        // Make sure the dead entries look ok
        assertThat(deadEntriesHead.next, is(entity1));
        assertThat(deadEntriesHead.next.next, is(entity2));
        assertThat(deadEntriesHead.next.next.next, is(deadEntriesTail));

        assertThat(deadEntriesTail.previous, is(entity2));
        assertThat(deadEntriesTail.previous.previous, is(entity1));
        assertThat(deadEntriesTail.previous.previous.previous, is(deadEntriesHead));

        // Make sure the live entries look ok
        assertThat(liveEntriesHead.next, is(entity0));
        assertThat(liveEntriesHead.next.next, is(liveEntriesTail));

        assertThat(liveEntriesTail.previous, is(entity0));
        assertThat(liveEntriesTail.previous.previous, is(liveEntriesHead));

        // Spawn the second
        Entity spawned2 = model.spawn(new ParticleSettings());
        assertThat(spawned2, is(entity1));

        assertThat(spawned2.previous, is(entity0));
        assertThat(spawned2.next, is(liveEntriesTail));
        
        // Make sure the dead entries look ok
        assertThat(deadEntriesHead.next, is(entity2));
        assertThat(deadEntriesHead.next.next, is(deadEntriesTail));

        assertThat(deadEntriesTail.previous, is(entity2));
        assertThat(deadEntriesTail.previous.previous, is(deadEntriesHead));

        // Make sure the live entries look ok
        assertThat(liveEntriesHead.next, is(entity0));
        assertThat(liveEntriesHead.next.next, is(entity1));
        assertThat(liveEntriesHead.next.next.next, is(liveEntriesTail));

        assertThat(liveEntriesTail.previous, is(entity1));
        assertThat(liveEntriesTail.previous.previous, is(entity0));
        assertThat(liveEntriesTail.previous.previous.previous, is(liveEntriesHead));

        // Spawn the third
        Entity spawned3 = model.spawn(new ParticleSettings());
        assertThat(spawned3, is(entity2));

        assertThat(spawned3.previous, is(entity1));
        assertThat(spawned3.next, is(liveEntriesTail));
        
        // Make sure the dead entries look ok
        assertThat(deadEntriesHead.next, is(deadEntriesTail));
        assertThat(deadEntriesTail.previous, is(deadEntriesHead));

        // Make sure the live entries look ok
        assertThat(liveEntriesHead.next, is(entity0));
        assertThat(liveEntriesHead.next.next, is(entity1));
        assertThat(liveEntriesHead.next.next.next, is(entity2));
        assertThat(liveEntriesHead.next.next.next.next, is(liveEntriesTail));

        assertThat(liveEntriesTail.previous, is(entity2));
        assertThat(liveEntriesTail.previous.previous, is(entity1));
        assertThat(liveEntriesTail.previous.previous.previous, is(entity0));
        assertThat(liveEntriesTail.previous.previous.previous.previous, is(liveEntriesHead));
    }
    
    @Test public void test_spawn_overflow() throws Exception {

        Entity entity0 = deadEntriesHead.next;
        Entity entity1 = deadEntriesHead.next.next;
        Entity entity2 = deadEntriesHead.next.next.next;

        // Spawn them all
        Entity spawned1 = model.spawn(new ParticleSettings());
        Entity spawned2 = model.spawn(new ParticleSettings());
        Entity spawned3 = model.spawn(new ParticleSettings());
        
        assertThat(spawned1, is(entity0));
        assertThat(spawned2, is(entity1));
        assertThat(spawned3, is(entity2));
        
        // The next spawn should resuse the first item on the live list, and move it to the end of the live list
        Entity spawned4 = model.spawn(new ParticleSettings());
        assertThat(spawned4, is(entity0));
        
        // Make sure the dead entries look ok
        assertThat(deadEntriesHead.next, is(deadEntriesTail));
        assertThat(deadEntriesTail.previous, is(deadEntriesHead));

        // Make sure the live entries look ok
        assertThat(liveEntriesHead.next, is(entity1));
        assertThat(liveEntriesHead.next.next, is(entity2));
        assertThat(liveEntriesHead.next.next.next, is(entity0));
        assertThat(liveEntriesHead.next.next.next.next, is(liveEntriesTail));

        assertThat(liveEntriesTail.previous, is(entity0));
        assertThat(liveEntriesTail.previous.previous, is(entity2));
        assertThat(liveEntriesTail.previous.previous.previous, is(entity1));
        assertThat(liveEntriesTail.previous.previous.previous.previous, is(liveEntriesHead));

    }
    
    @Test public void test_kill() throws Exception {

        Entity entity0 = deadEntriesHead.next;
        Entity entity1 = deadEntriesHead.next.next;
        Entity entity2 = deadEntriesHead.next.next.next;

        // Spawn them all
        Entity spawned1 = model.spawn(new ParticleSettings());
        Entity spawned2 = model.spawn(new ParticleSettings());
        Entity spawned3 = model.spawn(new ParticleSettings());

        // Kill Spawned 2 / Entity 1
        model.killEntity(entity1);
        
        // Make sure the dead entries look ok
        assertThat(entity1.previous, is(deadEntriesHead));
        assertThat(entity1.next, is(deadEntriesTail));        
        
        assertThat(deadEntriesHead.next, is(entity1));
        assertThat(deadEntriesTail.previous, is(entity1));

        // Make sure the live entries look ok
        assertThat(liveEntriesHead.next, is(entity0));
        assertThat(liveEntriesHead.next.next, is(entity2));
        assertThat(liveEntriesHead.next.next.next, is(liveEntriesTail));

        assertThat(liveEntriesTail.previous, is(entity2));
        assertThat(liveEntriesTail.previous.previous, is(entity0));
        assertThat(liveEntriesTail.previous.previous.previous, is(liveEntriesHead));

    }

}
