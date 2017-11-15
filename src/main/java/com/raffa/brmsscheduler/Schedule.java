package com.raffa.brmsscheduler;

import java.io.Serializable;

public class Schedule implements Serializable {

	private String nodeName;

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

}
