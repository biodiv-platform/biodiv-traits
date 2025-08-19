/**
 * 
 */
package com.strandls.traits.services.Impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.activity.controller.ActivityServiceApi;
import com.strandls.activity.pojo.ActivityLoggingData;
import com.strandls.activity.pojo.MailData;
import com.strandls.activity.pojo.SpeciesActivityLogging;
import com.strandls.traits.Headers;

import jakarta.inject.Inject;

/**
 * @author Abhishek Rudra
 *
 */
public class LogActivities {

	private final Logger logger = LoggerFactory.getLogger(LogActivities.class);

	@Inject
	private ActivityServiceApi activityService;

	@Inject
	private Headers headers;

	public void logActivity(String authToken, String activityDescription, Long rootObjectId, Long subRootObjectId,
			String rootObjectType, Long activityId, String activityType, MailData mailData) {

		try {
			ActivityLoggingData activityLogging = new ActivityLoggingData();
			activityLogging.setActivityDescription(activityDescription);
			activityLogging.setActivityId(activityId);
			activityLogging.setActivityType(activityType);
			activityLogging.setRootObjectId(rootObjectId);
			activityLogging.setRootObjectType(rootObjectType);
			activityLogging.setSubRootObjectId(subRootObjectId);
			activityLogging.setMailData(mailData);

			activityService = headers.addActivityHeader(activityService, authToken);
			activityService.logActivity(activityLogging);

		} catch (Exception e) {
			logger.error(e.getMessage());
		}

	}

	public void logSpeciesActivity(String authToken, String activityDescription, Long rootObjectId,
			Long subRootObjectId, String rootObjectType, Long activityId, String activityType, MailData mailData) {
		try {
			SpeciesActivityLogging activityLogging = new SpeciesActivityLogging();
			activityLogging.setActivityDescription(activityDescription);
			activityLogging.setActivityId(activityId);
			activityLogging.setActivityType(activityType);
			activityLogging.setRootObjectId(rootObjectId);
			activityLogging.setRootObjectType(rootObjectType);
			activityLogging.setSubRootObjectId(subRootObjectId);
			activityLogging.setMailData(mailData);

			activityService = headers.addActivityHeader(activityService, authToken);
			activityService.logSpeciesActivities(activityLogging);

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

}
