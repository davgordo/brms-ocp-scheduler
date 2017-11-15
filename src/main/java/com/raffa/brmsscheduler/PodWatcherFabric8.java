package com.raffa.brmsscheduler;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import io.fabric8.kubernetes.api.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Binding;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1ObjectReference;


@Component
public class PodWatcherFabric8 {

	private Log log = LogFactory.getLog(PodWatcherFabric8.class);

	@Inject
	CoreV1Api v1Api;


	@Inject
	KubernetesClient kclient;

	@Value("${scheduler.name:BRMSScheduler}")
	String schedulerName;

	@Inject
	KieSession ksession;

	@PostConstruct
	public void runWatcher() {
		KubernetesClient client = new DefaultKubernetesClient();
		MixedOperation<Pod, PodList, DoneablePod, PodResource<Pod, DoneablePod>> pods = client.pods();
		log.debug("scheduler name: " + schedulerName);
		Watch watch = pods.
				withField("status.phase", "Pending")
				.watch(new Watcher<Pod>() {
					@Override
					public void eventReceived(Action action, Pod pod) {

						log.debug("received request for pod: " + pod.getMetadata().getNamespace() + "/" + pod.getMetadata().getName() + " with scheduler: " + pod.getSpec().getSchedulerName());
						if (!pod.getSpec().getSchedulerName().equals(schedulerName)) {
							log.debug("not my scheduler, exiting");
							return;
						}

						log.debug("starting pod scheduling brms process for pod: " + pod.getMetadata().getNamespace() + "/" + pod.getMetadata().getName());
						NodeList nodes = (NodeList) kclient.nodes();
						Schedule schedule = new Schedule();

						try {
							ksession.insert(schedule);
							ksession.insert(pod);
							for (Node node : nodes.getItems()) {
								ksession.insert(node);
							}
							ksession.fireAllRules();
						} catch (Throwable t) {
							t.printStackTrace();
						} finally {
							ksession.dispose();
						}

						String nodeName = schedule.getNodeName();
						// create v1Binding between pod and node:
						// https://kubernetes.io/docs/api-reference/v1.8/#binding-v1-core
						try {
							createBinding(pod, nodeName);
						} catch (ApiException e) {
							// TODO Auto-generated catch block
							log.debug(e.getResponseHeaders());
							log.debug(e.getResponseBody());
							e.printStackTrace();
						}
					}

					@Override
					public void onClose(KubernetesClientException e) {
						if (e != null) {
							// TODO manage error
						}
					}
				});
	}


	private void createBinding(Pod pod, String nodeName) throws ApiException {
		V1ObjectReference or = new V1ObjectReference();
		or.setKind("Node");
		or.setName(nodeName);
		V1ObjectMeta meta = new V1ObjectMeta();
		meta.setName(pod.getMetadata().getName());
		V1Binding body = new V1Binding();
		body.target(or);
		body.setMetadata(meta);
		v1Api.createNamespacedBinding(pod.getMetadata().getNamespace(), body, null);
	}

}
