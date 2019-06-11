package com.myspace.currencyrules;

public class CurrentCheckResult {

	static final long serialVersionUID = 1L;
	
	private boolean valid;
	
	public CurrentCheckResult(boolean valid) {
		this.valid = valid;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}
}
