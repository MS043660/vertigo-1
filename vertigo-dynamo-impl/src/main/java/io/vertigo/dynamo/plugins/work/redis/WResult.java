package io.vertigo.dynamo.plugins.work.redis;

import io.vertigo.kernel.lang.Assertion;

/**
 * @author pchretien
 * $Id: RedisDispatcherThread.java,v 1.8 2014/02/03 17:28:45 pchretien Exp $
 */
public final class WResult<WR> {
	private final String workId;
	private final Throwable error;
	private final WR result;

	public WResult(final String workId, final boolean succeeded, final WR result, final Throwable error) {
		Assertion.checkArgNotEmpty(workId);
		if (succeeded) {
			Assertion.checkArgument(result != null, "when succeeded,  a result is required");
			Assertion.checkArgument(error == null, "when succeeded, an error is not accepted");
		} else {
			Assertion.checkArgument(error != null, "when failed, an error is required");
			Assertion.checkArgument(result == null, "when failed, a result is not accepted");
		}
		//---------------------------------------------------------------------
		this.workId = workId;
		this.error = error;
		this.result = result;
	}

	public String getWorkId() {
		return workId;
	}

	public boolean hasSucceeded() {
		return error == null;
	}

	public WR getResult() {
		return result;
	}

	public Throwable getError() {
		return error;
	}

}