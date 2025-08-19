/**
 * 
 */
package com.strandls.traits;

import com.strandls.activity.controller.ActivityServiceApi;

import jakarta.ws.rs.core.HttpHeaders;

/**
 * @author Abhishek Rudra
 *
 */
public class Headers {

	public ActivityServiceApi addActivityHeader(ActivityServiceApi activityService, String token) {
		activityService.getApiClient().addDefaultHeader(HttpHeaders.AUTHORIZATION, token);
		return activityService;
	}

}
