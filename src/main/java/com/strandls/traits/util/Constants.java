/**
 * 
 */
package com.strandls.traits.util;

/**
 * @author Abhishek Rudra
 *
 * 
 */
public class Constants {

	public enum OBJECTTYPE {
		OBSERVATION("species.participation.Observation"), SPECIES("species.Species");

		private String action;

		private OBJECTTYPE(String action) {
			this.action = action;
		}

		public String getValue() {
			return action;
		}
	}

	public enum DATATYPE {

		COLOR("COLOR"), NUMERIC("NUMERIC"), DATE("DATE");

		private String action;

		private DATATYPE(String action) {
			this.action = action;
		}

		public String getValue() {
			return action;
		}
	}

	public enum TRAITTYPE {
		SINGLECATEGORICAL("SINGLE_CATEGORICAL");

		private String action;

		private TRAITTYPE(String action) {
			this.action = action;
		}

		public String getValue() {
			return action;
		}
	}

	public enum TRAITMSG {

		ADDEDFACT("Added a fact"), UPDATEDFACT("Updated fact");

		private String action;

		private TRAITMSG(String action) {
			this.action = action;
		}

		public String getValue() {
			return action;
		}
	}

}
