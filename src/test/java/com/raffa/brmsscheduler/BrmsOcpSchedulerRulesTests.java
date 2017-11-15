package com.raffa.brmsscheduler;

import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BrmsOcpSchedulerRulesTests {

	@Autowired
	private KieSession kieSession;

	@After
	public void tearDown() throws Exception {
		kieSession.destroy();
	}

	@Test
	public void staticNodeIsChosen() {

		// Given
		Schedule schedule = new Schedule();

		Pod pod = new PodBuilder()
				.withNewStatus().withPhase("Pending").endStatus()
				.build();

		Node node1 = new NodeBuilder()
				.withNewMetadata().withName("app-node-1.env1.casl.raffa.com").endMetadata()
				.withNewStatus().withPhase("MemoryPressure").endStatus()
				.build();

		Node node2 = new NodeBuilder()
				.withNewMetadata().withName("app-node-2.env1.casl.raffa.com").endMetadata()
				.withNewStatus().withPhase("Ready").endStatus()
				.build();

		kieSession.insert(schedule);
		kieSession.insert(pod);
		kieSession.insert(node1);
		kieSession.insert(node2);

		// When
		int ruleFiredCount = kieSession.fireAllRules();

		// Then
		assertEquals(2, ruleFiredCount);
		assertEquals("app-node-2.env1.casl.raffa.com", schedule.getNodeName());

	}

}
