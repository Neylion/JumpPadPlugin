package se.fredsfursten.plugintools;

import java.time.LocalDateTime;

class Alarm {

	private LocalDateTime _when;
	private Runnable _task;
	private boolean _hasBeenSetOff;

	Alarm(LocalDateTime when, Runnable task)
	{
		this._when = when;
		this._task = task;
		this._hasBeenSetOff = false;
	}
	
	LocalDateTime getTime()
	{
		return this._when;
	}

	boolean maybeSetOff() 
	{
		synchronized(this) {
			if (this._hasBeenSetOff) return false;
			if (this._when.isBefore(LocalDateTime.now())) {
				this._hasBeenSetOff = true;
				this._task.run();
				return true;
			}
			return false;
		}
	}
}
