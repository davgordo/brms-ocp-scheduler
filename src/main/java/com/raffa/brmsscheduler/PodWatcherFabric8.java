package com.raffa.brmsscheduler;

import javax.inject.Inject;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.Config;
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

	@Inject
	CoreV1Api v1Api;
	


	@Inject
	KubernetesClient kclient;

	@Value("${schedulerName:BMRSScheduler}")
	String schedulerName;
	
	@Inject
	KieSession ksession;

	public PodWatcherFabric8() {
		KubernetesClient client=new DefaultKubernetesClient();
		MixedOperation<Pod, PodList, DoneablePod, PodResource<Pod,DoneablePod>>pods=client.pods();
		Watch watch = pods.
				//withField("spec.scheduler", schedulerName).
				withField("status.phase", "Pending")
				.watch(new Watcher<Pod>() {
					@Override
					public void eventReceived(Action action, Pod pod) {
						// TODO use brms to select node
						ProcessInstance instance=ksession.startProcess("com.sample.bpmn.hello");
						// ?? not sure what do do here
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
