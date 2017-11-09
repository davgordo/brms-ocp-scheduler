package com.raffa.brmsscheduler;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.Watcher.Action;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Binding;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1ObjectReference;

public class PodWatcherFabric8 {

	@Inject
	CoreV1Api v1Api;

	@Inject
	KubernetesClient client;

	@Value("${scheduler-name:BMRSScheduler}")
	String schedulerName;

	public PodWatcherFabric8() {
		Watch watch = client.pods().withField("spec.scheduler", schedulerName).withField("status.phase", "Pending")
				.watch(new Watcher<Pod>() {
					@Override
					public void eventReceived(Action action, Pod pod) {
						// TODO use brms to select node
						String nodeName = null;
						// create v1Binding between pod and node:
						// https://kubernetes.io/docs/api-reference/v1.8/#binding-v1-core

						try {
							createBinding(pod, nodeName);
						} catch (ApiException e) {
							// TODO Auto-generated catch block
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
		or.setKind("node");
		or.setName(nodeName);
		V1ObjectMeta meta = new V1ObjectMeta();
		meta.setName(pod.getMetadata().getName());
		V1Binding body = new V1Binding();
		body.target(or);
		body.setMetadata(meta);
		v1Api.createNamespacedBinding(pod.getMetadata().getNamespace(), body, null);
	}

}
