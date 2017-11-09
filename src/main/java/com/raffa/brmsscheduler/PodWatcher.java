package com.raffa.brmsscheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;

public class PodWatcher {
	@Autowired
	KubernetesClient client;
	
	@Value("${scheduler-name:BMRSScheduler}")
	String schedulerName;
	
	public PodWatcher() {
		Watch watch=client.pods().withField("schduler", schedulerName).watch(new Watcher<Pod>() {
	        @Override
	        public void eventReceived(Action action, Pod pod) {
	          //TODO use brms to select node
	          String node;
	          //create v1Binding between pod and node: https://kubernetes.io/docs/api-reference/v1.8/#binding-v1-core
	        }

	        @Override
	        public void onClose(KubernetesClientException e) {
	          if (e != null) {
	            //TODO manage error
	          }
	        }
	      });
	
	
	}

}
