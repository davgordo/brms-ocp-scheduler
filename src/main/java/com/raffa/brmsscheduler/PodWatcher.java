package com.raffa.brmsscheduler;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;

import com.google.gson.reflect.TypeToken;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.util.Watch;

public class PodWatcher {
	@Inject
	ApiClient client;

	@Inject
	CoreV1Api v1Api;

	@Value("${scheduler-name:BMRSScheduler}")
	String schedulerName;

	public PodWatcher() throws ApiException {
		Watch<V1Pod> watch = Watch.createWatch(client,
				v1Api.listPodForAllNamespacesCall(null, "schedulerName="+schedulerName, Boolean.TRUE, null, null, null, null, null, Boolean.TRUE, null, null),
				new TypeToken<Watch.Response<V1Pod>>(){}.getType());
		
/*		v1Api.createNamespacedBinding(namespace, body, pretty)
	
       for (Watch.Response<V1Pod> item : watch) {
            System.out.printf("%s : %s%n", item.type, item.object.getMetadata().getName());
            //TODO use brms to identify node
            String node;
            client.buildCall(path, method, queryParams, collectionQueryParams, body, headerParams, formParams, authNames, progressRequestListener)
        }*/
	
	}

}
